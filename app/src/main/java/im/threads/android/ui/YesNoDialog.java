package im.threads.android.ui;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import im.threads.android.R;


/**
 * Диалог с вопросом и кнопками Да, Нет
 */
public class YesNoDialog extends DialogFragment {

    public static final String TAG = "MessageDialog";

    public static final String ARG_TEXT = "ARG_TEXT";
    public static final String ARG_REQUEST_CODE = "ARG_REQUEST_CODE";
    public static final String ARG_REQUEST_YES_CODE = "ARG_REQUEST_YES_CODE";
    public static final String ARG_REQUEST_NO_CODE = "ARG_REQUEST_NO_CODE";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        if(window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View view = inflater.inflate(R.layout.dialog_yes_no, container, false);

        ((TextView) view.findViewById(R.id.text)).setText(
                Html.fromHtml(getArguments().getString(ARG_TEXT, ""))
        );

        ((Button) view.findViewById(R.id.yesButton)).setText(
                Html.fromHtml(getArguments().getString(ARG_REQUEST_YES_CODE, ""))
        );

        ((Button) view.findViewById(R.id.noButton)).setText(
                Html.fromHtml(getArguments().getString(ARG_REQUEST_NO_CODE, ""))
        );

        setCancelable(false);
        view.findViewById(R.id.yesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Fragment targetFragment = getTargetFragment();
                if (targetFragment != null) {
                    Fragment fragment = getTargetFragment();
                    if (fragment instanceof YesNoDialogActionListener) {
                        ((YesNoDialogActionListener) fragment).onOkClicked(getArguments().getInt(ARG_REQUEST_CODE));
                    }
                } else {
                    Activity activity = getActivity();
                    if (activity instanceof YesNoDialogActionListener) {
                        ((YesNoDialogActionListener) activity).onOkClicked(getArguments().getInt(ARG_REQUEST_CODE));
                    }
                }
                dismiss();
            }
        });

        view.findViewById(R.id.noButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Fragment targetFragment = getTargetFragment();
                if (targetFragment != null) {
                    Fragment fragment = getTargetFragment();
                    if (fragment instanceof YesNoDialogActionListener) {
                        ((YesNoDialogActionListener) fragment).onCancelClicked(getArguments().getInt(ARG_REQUEST_CODE));
                    }
                } else {
                    Activity activity = getActivity();
                    if (activity instanceof YesNoDialogActionListener) {
                        ((YesNoDialogActionListener) activity).onCancelClicked(getArguments().getInt(ARG_REQUEST_CODE));
                    }
                }
                dismiss();
            }
        });

        return view;
    }

    public interface YesNoDialogActionListener {
        void onOkClicked(int requestCode);
        void onCancelClicked(int requestCode);
    }

    public static void open(AppCompatActivity activity, String text, String yesButton, String noButton, int requestCode) {
        newInstance(text, yesButton, noButton, requestCode).show(activity.getSupportFragmentManager(), TAG);
    }

    public static void open(Fragment fragment, String text, String yesButton, String noButton, int requestCode) {
        YesNoDialog dialog = newInstance(text, yesButton, noButton, requestCode);
        dialog.setTargetFragment(fragment, requestCode);
        dialog.show(fragment.getActivity().getSupportFragmentManager(), TAG);
    }

    protected static YesNoDialog newInstance(String text, String yesButton, String noButton, int requestCode) {
        YesNoDialog dialog = new YesNoDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        args.putString(ARG_REQUEST_YES_CODE, yesButton);
        args.putString(ARG_REQUEST_NO_CODE, noButton);
        args.putInt(ARG_REQUEST_CODE, requestCode);
        dialog.setArguments(args);
        return dialog;
    }
}
