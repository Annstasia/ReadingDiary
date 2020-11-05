package com.example.readingdiary.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.readingdiary.Activities.AuthorizationActivity;
import com.example.readingdiary.Classes.DeleteUser;
import com.example.readingdiary.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialTextInputPicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DeleteAccountFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_delete_account, null);
        ((Toolbar)getActivity().findViewById(R.id.toolbar_navigation)).getMenu().clear();
        ((MaterialButton)root.findViewById(R.id.button_delete_account)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseUser currentUser =  FirebaseAuth.getInstance().getCurrentUser();
                AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(),
                        ((TextInputEditText)root.findViewById(R.id.delete_account_password)).getText().toString().trim());
                currentUser.reauthenticate(credential)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "Аккаунт удален", Toast.LENGTH_LONG).show();
                                DeleteUser.deleteUser(getContext(), currentUser.getUid());
                                Intent intent = new Intent(getActivity(), AuthorizationActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                ((TextInputEditText)root.findViewById(R.id.delete_account_password))
                                        .setError("Неверный пароль");
                                Toast.makeText(getContext(), "Ошибка. Проверьте пароль", Toast.LENGTH_LONG).show();
                            }
                        });





            }
        });
        return root;
    }

}
