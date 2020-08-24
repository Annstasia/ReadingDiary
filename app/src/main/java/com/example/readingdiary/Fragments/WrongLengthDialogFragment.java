package com.example.readingdiary.Fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.readingdiary.Activities.EditNoteActivity;
import com.example.readingdiary.Activities.MainActivity;
import com.example.readingdiary.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class WrongLengthDialogFragment extends DialogFragment {
    WrongLengthDialogListener listener;
    Context context;
    int length;

    public WrongLengthDialogFragment(Context context, int length){
        this.context=context;
        this.length = length;
    }
    public WrongLengthDialogFragment(Context context){
        this.context=context;
        this.length = -1;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        String title = getResources().getString(R.string.saveDialogTitle);
//        String message = getResources().getString(R.string.saveDialogMessage);
        String title;
        String message;
        if (length > 0 && length <= 5000){
            getActivity().finish();
        }
        if (length == 0){
            title = "Пустая запись";
            message = "Запись не может быть пустой. Вернитесь к редактированию записи или не сохраняйте изменения";
        }
        else{
            title = "Слишком много символов";
            message = "Максимальное число символов - 5000. Вам небходимо вернуться к редактированию и убрать " + (length - 5000) + " или не сохранятть изменения";

        }

        final String notSaveButtonString = "Не сохранять";
        final String returnButtonString = "Вернуться";
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(title);  // заголовок
        builder.setMessage(message); // сообщение
        builder.setPositiveButton(returnButtonString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(getActivity(), returnButtonString,
                        Toast.LENGTH_LONG).show();
//                ((EditNoteActivity)getActivity()).saveChanges();
//                ((EditNoteActivity)getActivity()).finish();

            }
        });
        builder.setNegativeButton(notSaveButtonString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), returnButtonString, Toast.LENGTH_SHORT).show();
                listener.onNotSaveClicked();
            }
        });
        builder.setCancelable(true);
        return builder.create();
    }

    public interface WrongLengthDialogListener {
        void onNotSaveClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (WrongLengthDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + "must implement ExampleDialogListener");
        }
    }
}
