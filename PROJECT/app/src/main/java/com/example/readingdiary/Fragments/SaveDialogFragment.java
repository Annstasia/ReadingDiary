package com.example.readingdiary.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.readingdiary.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SaveDialogFragment extends DialogFragment {
    SaveDialogListener listener;
    Context context;
    int length;

    public SaveDialogFragment(Context context, int length){
        this.context=context;
        this.length = length;
    }
    public SaveDialogFragment(Context context){
        this.context=context;
        this.length = -1;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String title = getResources().getString(R.string.saveDialogTitle);
        String message = getResources().getString(R.string.saveDialogMessage);
        final String saveButtonString = getResources().getString(R.string.saveDialogSaveButton);
        final String notSaveButtonString = getResources().getString(R.string.saveDialogNotSaveButton);
        final String returnButtonString = getResources().getString(R.string.saveDialogReturnButton);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(title);  // заголовок
        builder.setMessage(message); // сообщение

        if (length == 0){
            Toast.makeText(getActivity(), "length=0", Toast.LENGTH_LONG).show();
        }
        builder.setPositiveButton(saveButtonString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(getActivity(), saveButtonString,
                        Toast.LENGTH_LONG).show();
                listener.onSaveClicked();
//                ((EditNoteActivity)getActivity()).saveChanges();
//                ((EditNoteActivity)getActivity()).finish();

            }
        });
        builder.setNeutralButton(returnButtonString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), returnButtonString, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setCancelable(true);
        builder.setNegativeButton(notSaveButtonString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(getActivity(), notSaveButtonString, Toast.LENGTH_LONG)
                        .show();
                listener.onNotSaveClicked();
            }
        });



        return builder.create();
    }

    public interface SaveDialogListener {
        void onSaveClicked();
        void onNotSaveClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (SaveDialogFragment.SaveDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + "must implement ExampleDialogListener");
        }
    }
}
