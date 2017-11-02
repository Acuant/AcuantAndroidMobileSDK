package com.cssn.samplesdk.util;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by marcosambrosi on 9/18/14.
 */
public class CssnAlertDialog extends DialogFragment {
    private String mTitle;
    private String mMessage;
    private YesNoListener mYesNoListener;

    public interface YesNoListener {
        void onYes(DialogInterface dialog);

        void onNo(DialogInterface dialog);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void show(FragmentManager manager, String title, String message, YesNoListener yesNoListener) {
        this.mTitle = title;
        this.mMessage = message;
        this.mYesNoListener = yesNoListener;
        super.show(manager, CssnAlertDialog.class.getName());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(this.mTitle)
                .setMessage(this.mMessage)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mYesNoListener != null) mYesNoListener.onYes(dialog);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mYesNoListener != null) mYesNoListener.onNo(dialog);
                    }
                })
                .create();
    }
}
