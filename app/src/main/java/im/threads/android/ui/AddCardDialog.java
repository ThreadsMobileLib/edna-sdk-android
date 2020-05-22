package im.threads.android.ui;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import im.threads.android.R;
import im.threads.android.data.Card;
import im.threads.android.databinding.DialogAddCardBinding;
import im.threads.android.network.AuthProvider;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Диалог добавления пользователя
 */
public class AddCardDialog extends DialogFragment {

    private static final String TAG = "AddCardDialog";

    DialogAddCardBinding binding;
    private Disposable signatureDisposable;

    public static void open(AppCompatActivity activity) {
        new AddCardDialog().show(activity.getSupportFragmentManager(), TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        if(window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        binding = DialogAddCardBinding.inflate(inflater);
        setCancelable(false);
        updateAddButtonEnabled();
        binding.clientId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                updateAddButtonEnabled();
            }
        });

        binding.addButton.setOnClickListener(v -> {
            String clientId = binding.clientId.getText().toString();
            String clientIdSignature = binding.clientIdSignature.getText().toString();
            if (TextUtils.isEmpty(clientId)) {
                binding.clientIdInputLayout.setErrorEnabled(true);
                binding.clientIdInputLayout.setError("ClientId is empty");
            } else {
                binding.clientIdInputLayout.setErrorEnabled(false);
                binding.clientIdInputLayout.setError(null);
                if (TextUtils.isEmpty(clientIdSignature)) {
                    if (signatureDisposable != null && !signatureDisposable.isDisposed()) {
                        signatureDisposable.dispose();
                    }
                    signatureDisposable = AuthProvider.getSignature(clientId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    signature -> {
                                        Card newCard = new Card(binding.clientId.getText().toString(),
                                                binding.clientName.getText().toString(),
                                                binding.appMarker.getText().toString(),
                                                signature);
                                        addButtonClicked(newCard);
                                        dismiss();
                                    },
                                    throwable -> {
                                        showError(R.string.get_signature_error);
                                        Card newCard = new Card(binding.clientId.getText().toString(),
                                                binding.clientName.getText().toString(),
                                                binding.appMarker.getText().toString(),
                                                "");
                                        addButtonClicked(newCard);
                                        dismiss();
                                    });

                } else {
                    Card newCard = new Card(binding.clientId.getText().toString(),
                            binding.clientName.getText().toString(),
                            binding.appMarker.getText().toString(),
                            clientIdSignature);
                    addButtonClicked(newCard);
                    dismiss();
                }
            }
        });
        binding.cancelButton.setOnClickListener(v -> {
            cancelButtonClicked();
            dismiss();
        });
        return binding.getRoot();
    }

    private void updateAddButtonEnabled() {
        binding.addButton.setEnabled(binding.clientId.length() > 0);
    }

    private void addButtonClicked(Card newCard) {
        Fragment targetFragment = getTargetFragment();
        if(targetFragment != null) {
            Fragment fragment = getTargetFragment();
            if(fragment instanceof AddCardDialogActionsListener) {
                ((AddCardDialogActionsListener) fragment).onCardAdded(newCard);
            }
        } else {
            Activity activity = getActivity();
            if(activity instanceof AddCardDialogActionsListener) {
                ((AddCardDialogActionsListener) activity).onCardAdded(newCard);
            }
        }
    }

    private void cancelButtonClicked() {
        Fragment targetFragment = getTargetFragment();
        if(targetFragment != null) {
            Fragment fragment = getTargetFragment();
            if(fragment instanceof AddCardDialog.AddCardDialogActionsListener) {
                ((AddCardDialog.AddCardDialogActionsListener) fragment).onCancel();
            }
        } else {
            Activity activity = getActivity();
            if(activity instanceof AddCardDialog.AddCardDialogActionsListener) {
                ((AddCardDialog.AddCardDialogActionsListener) activity).onCancel();
            }
        }
    }

    private void showError(@StringRes int errorMessageResId) {
        Toast.makeText(getContext(), errorMessageResId, Toast.LENGTH_SHORT).show();
    }

    public interface AddCardDialogActionsListener {
        void onCardAdded(Card newCard);
        void onCancel();
    }
}
