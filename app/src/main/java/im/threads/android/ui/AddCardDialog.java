package im.threads.android.ui;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import im.threads.android.data.Card;
import im.threads.android.databinding.DialogAddCardBinding;


/**
 * Диалог добавления пользователя
 */
public class AddCardDialog extends DialogFragment {

    public static final String TAG = "AddCardDialog";

    DialogAddCardBinding binding;

    public static void open(AppCompatActivity activity) {
        new AddCardDialog().show(activity.getSupportFragmentManager(), TAG);
    }

    public static void open(Fragment fragment, int requestCode) {
        AddCardDialog dialog = new AddCardDialog();
        dialog.setTargetFragment(fragment, requestCode);
        dialog.show(fragment.getActivity().getSupportFragmentManager(), TAG);
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

        binding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Card newCard = new Card(binding.clientId.getText().toString(), binding.clientName.getText().toString());
                addButtonClicked(newCard);
                dismiss();
            }
        });

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                cancelButtonClicked();
                dismiss();
            }
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

    public interface AddCardDialogActionsListener {
        void onCardAdded(Card newCard);
        void onCancel();
    }
}
