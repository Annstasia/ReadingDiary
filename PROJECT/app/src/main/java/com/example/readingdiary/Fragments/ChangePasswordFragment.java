package com.example.readingdiary.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.readingdiary.Activities.CatalogActivity;
import com.example.readingdiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordFragment extends Fragment {
    View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.fragment_change_password, null);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar_navigation);
        toolbar.getMenu().clear();
        ((TextView)toolbar.findViewById(R.id.counter_text)).setText("Сменить пароль");
        MaterialButton button = (MaterialButton)root.findViewById(R.id.button_change_password);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword(((TextInputEditText)root.findViewById(R.id.edit_old_password)).getText().toString().trim(),
                        ((TextInputEditText)root.findViewById(R.id.edit_new_password)).getText().toString().trim());
            }
        });
        return root;
    }

    private void changePassword(String oldPassword, final String newPassword){
        Log.d("qwerty1", oldPassword + " " + newPassword);
        if (oldPassword.equals("") || newPassword.equals("")){
            Toast.makeText(getContext(), "Пароль не может быть пустым", Toast.LENGTH_LONG).show();
            return;
        }
        if (oldPassword.length() < 6 || oldPassword.length() > 30 || newPassword.length() < 6 || newPassword.length() > 30){
            Toast.makeText(getContext(), "Длина пароля должна быть от 6 до 30 символов", Toast.LENGTH_LONG).show();
            return;
        }

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "Пароль обновлен", Toast.LENGTH_LONG).show();
                            ((TextInputEditText)root.findViewById(R.id.edit_old_password)).setText("");
                            ((TextInputEditText)root.findViewById(R.id.edit_new_password)).setText("");
                        }
                    });
                }
                else{
                    Toast.makeText(getContext(), "Ошибка авторизации. Проверьте введенный пароль", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
