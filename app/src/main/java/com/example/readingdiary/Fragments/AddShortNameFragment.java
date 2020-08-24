package com.example.readingdiary.Fragments;

import androidx.appcompat.app.AlertDialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.readingdiary.Activities.MainActivity;
import com.example.readingdiary.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class AddShortNameFragment extends AppCompatDialogFragment {
//    private AddShortNameDialogListener listener;
    private Context context;
    private String error;
    private Activity activity;
    private boolean changeID;
    private String userID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String user;
    private TextView errorTextView;
    private TextView editTextView;


    public AddShortNameFragment(Context context){
        this.context=context;
    }



    public AddShortNameFragment(Context context, String error){
        this.context = context;
        this.error = error;
    }

    public AddShortNameFragment(boolean changeID, String userID, String user){
        this.changeID = changeID;
        this.userID = userID;
        this.user = user;
    }



    public AddShortNameFragment(Context context, String error, boolean changeID){
        this.context = context;
        this.error = error;
        this.changeID = changeID;
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String title = "Введите никнейм";
        final String acceptButtonString = "Ввод";
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_short_name, null);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setView(view);
        builder.setTitle(title);  // заголовок
        editTextView = view.findViewById(R.id.editText2);
        errorTextView = view.findViewById(R.id.textView16);
        editTextView.setText("");
        if (error == null) errorTextView.setVisibility(View.GONE);
        builder.setPositiveButton(acceptButtonString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (editTextView != null){
                    checkAndSaveName(editTextView.getText().toString().trim());
                }

            }
        });
        if (changeID){
            builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
//                    listener.onEnterClicked(editTextView.getText().toString().trim());
                    checkAndSaveName(editTextView.getText().toString().trim());
                    Boolean wantToCloseDialog = false;
                    if(wantToCloseDialog)
                        d.dismiss();

                }
            });
        }
    }

    public void checkAndSaveName(final String name){
        if (name.contains(" ") || name.length() > 20 || name.equals("") || android.text.TextUtils.isDigitsOnly(name)){
            AddShortNameFragment saveDialogFragment;
            if (name.contains(" ")) setError("space");
            else if (name.length() > 20) setError("long");
            else if (name.equals("")) setError("null");
            else setError("number");
            return;
        }

        db.runTransaction(new Transaction.Function<String>() {
            @Nullable
            @Override
            public String apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference documentReference = db.collection("allNames").document("allNames");
                DocumentSnapshot allNamesShapshot = transaction.get(documentReference);
                if (allNamesShapshot != null && allNamesShapshot.getString("names")!=null){
                    String names = allNamesShapshot.getString("names");
                    if (Arrays.asList(names.split(" ")).contains(name)){
                        HashMap<String, String> map = new HashMap<>();
                        map.put("names", names);
                        transaction.set(db.collection("allNames").document("allNames"), map);
                        return "";
                    }
                    else{
                        HashMap<String, String> map = new HashMap<>();
                        String ans;
                        ArrayList<String> namesList=new ArrayList<>();
                        Collections.addAll(namesList, allNamesShapshot.getString("names").split(" "));
                        if (userID != null && namesList.contains(userID)){
                            namesList.set(namesList.indexOf(userID), name);
                            ans = namesList.stream().collect(Collectors.joining(" "));
                        }
                        else{
                            ans = names + " " + name;
                        }
                        map.put("names", ans);
                        transaction.set(db.collection("allNames").document("allNames"), map);
                        return name;
                    }
                }
                else{
                    HashMap<String, String> map = new HashMap<>();
                    map.put("names", name);
                    transaction.set(db.collection("allNames").document("allNames"), map);
                    return name;
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String str) {
                if (str.equals("")){
                    setError("exists");
                }
                else{
                    Map<String, String> map= new HashMap<>();
                    map.put("id", str);
                    db.collection("PublicID").document(user).set(map);
                    if (getDialog() != null){
                        getDialog().dismiss();
                    }
                }
            }
        });
    }

    private void setError(String error){
        if (error != null){
            errorTextView.setVisibility(View.VISIBLE);
            errorTextView.setError(error);
            if (error.equals("space")) errorTextView.setText("Никнейм не может содержать пробелы");
            else if (error.equals("long")) errorTextView.setText("Длина никнейма не может превышать 20 символов");
            else if (error.equals("null")) errorTextView.setText("Никнейм не должен быть пустым");
            else if (error.equals("number")) errorTextView.setText("Никнейм не должен быть числом");
            else if (error.equals("exists")) errorTextView.setText("Введенный никнейм уже существует");
        }
    }

}
