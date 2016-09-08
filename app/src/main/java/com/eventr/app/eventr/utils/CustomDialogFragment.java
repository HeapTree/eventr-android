package com.eventr.app.eventr.utils;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eventr.app.eventr.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Suraj on 05/09/16.
 */
public class CustomDialogFragment extends DialogFragment {
    @BindView(R.id.title_view) public TextView titleView;
    @BindView(R.id.positive_button) public TextView positiveButtonView;
    @BindView(R.id.negative_button) public TextView negativeButtonView;
    @BindView(R.id.edit_text) public EditText editText;
    @BindView(R.id.message_view) public TextView messageView;
    @BindView(R.id.progress_bar) public ProgressBar progressBar;
    @BindView(R.id.new_group_form) public LinearLayout newGroupForm;
    @BindView(R.id.error_view) public TextView errorView;
    @BindView(R.id.cancel_button) public TextView cancelButton;

    private String mTitle;
    private String mMessage;
    private String mPositiveText;
    private String mNegativeText;
    private View.OnClickListener mPositiveClickListener;
    private View.OnClickListener mNegativeClickListener;
    private static final String CONFIRM = "confirm";
    private static final String CANCEL_BUTTON_TEXT = "Cancel";
    private String dialogType = "edit_text";

    private boolean renderCancelButton = false;

    private static final  String RETRY_TEXT = "Retry";

    public CustomDialogFragment() {}

    public CustomDialogFragment(String dialogType) {
        this.dialogType = dialogType;
    }

    public CustomDialogFragment(String dialogType, boolean renderCancelButton) {
        this.dialogType = dialogType;
        this.renderCancelButton = renderCancelButton;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        return dialog;
    }

    public static CustomDialogFragment newInstance() {
        CustomDialogFragment nf = new CustomDialogFragment();
        return nf;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) {
            return;
        }
        setDialogSize();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setDialogSize() {
        if (getActivity() == null)
            return;
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int dialogWidth = width - 20;
        int dialogHeight = getDialog().getWindow().getAttributes().height;
        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
        getDialog().getWindow().setGravity(Gravity.CENTER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_dialog_fragment, container);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateDialogContent();
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public void setPositiveButton(String buttonText, View.OnClickListener clickListener) {
        mPositiveText = buttonText;
        mPositiveClickListener = clickListener;
    }

    public void setNegativeButton(String buttonText, View.OnClickListener clickListener) {
        mNegativeText = buttonText;
        mNegativeClickListener = clickListener;
    }

    public void updateDialogContent() {
        if (mTitle != null) {
            titleView.setText(mTitle);
        }

        if (mMessage != null) {
            messageView.setText(mMessage);
        }

        if (mPositiveText != null && mPositiveClickListener instanceof View.OnClickListener) {
            positiveButtonView.setText(mPositiveText);
            positiveButtonView.setOnClickListener(mPositiveClickListener);
        }

        if (mNegativeText != null && mNegativeClickListener instanceof View.OnClickListener) {
            negativeButtonView.setText(mNegativeText);
            negativeButtonView.setOnClickListener(mNegativeClickListener);
        }

        switch (dialogType) {
            case CONFIRM: {
                messageView.setVisibility(View.VISIBLE);
                break;
            }
            default: {
                editText.setVisibility(View.VISIBLE);
                break;
            }
        }

        if (renderCancelButton) {
            cancelButton.setText(CANCEL_BUTTON_TEXT);
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
        } else {
            cancelButton.setVisibility(View.GONE);
        }
    }

    public String getEditTextValue() {
        return editText.getText().toString();
    }

    public void showProgressBar() {
        closeKeyboard();
        progressBar.setVisibility(View.VISIBLE);
        newGroupForm.setVisibility(View.GONE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        newGroupForm.setVisibility(View.VISIBLE);
    }

    public void closeKeyboard() {
        if (getDialog() != null)
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void showError(String error) {
        errorView.setText(error);
        errorView.setVisibility(View.VISIBLE);
        positiveButtonView.setText(RETRY_TEXT);
    }

    public void hideError() {
        errorView.setText("");
        errorView.setVisibility(View.GONE);
        positiveButtonView.setText(mPositiveText);
    }
}
