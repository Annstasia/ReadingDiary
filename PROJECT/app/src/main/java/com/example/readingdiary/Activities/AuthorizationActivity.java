package com.example.readingdiary.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.readingdiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AuthorizationActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    EditText emailView;
    EditText passwordView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);
        auth = FirebaseAuth.getInstance();
        emailView = findViewById(R.id.emailView);
        passwordView = findViewById(R.id.passwordView);
        findViewById(R.id.registrationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkEmail(emailView.getText().toString().trim(), emailView) && checkPassword(passwordView.getText().toString().trim())){
                    registration();
                }
            }
        });
        findViewById(R.id.authorizationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkEmail(emailView.getText().toString().trim(), emailView) && checkPassword(passwordView.getText().toString().trim())){
                    authorization();
                }
            }
        });

        if (auth.getCurrentUser() != null){
            Intent intent = new Intent(AuthorizationActivity.this, CatalogActivity.class);
            startActivity(intent);
        }

        MaterialTextView forgotPassword = findViewById(R.id.forgotPasswordButton);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();

            }
        });


    }

    private void resetPassword(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Введите адрес почты");
        final EditText email = new EditText(getApplicationContext());
        email.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        email.setHint("Email");
        builder.setView(email);
        builder.setPositiveButton("Восстановить", null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkEmail(email.getText().toString().trim(), email)){
                    auth.sendPasswordResetEmail(email.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "Вам отправлено письмо для восстановления пароля. Поверьте почту",
                                    Toast.LENGTH_LONG).show();
                            dialog.cancel();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Ошибка. Проверьте правильность почты", Toast.LENGTH_LONG).show();
                            email.setError("Почта не найдена");
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
    }

    public boolean checkEmail(String email, EditText emailView){
        if (email.isEmpty())
        {

            Toast.makeText(AuthorizationActivity.this,"Введите логин",Toast.LENGTH_SHORT).show();
            emailView.setError("Введите логин");
            return false;
        }

        if (email.indexOf('@') < 1 || email.indexOf('.') < 1 ||  email.indexOf('@') > email.lastIndexOf('.')){
            Toast.makeText(AuthorizationActivity.this,"Неверный формат почты",Toast.LENGTH_SHORT).show();
            emailView.setError("Неверный формат");
            return false;
        }
        return true;

    }

    public boolean checkPassword(String password){
        if (password.isEmpty())
        {
            Toast.makeText(AuthorizationActivity.this,"Введите пароль",Toast.LENGTH_SHORT).show();
            passwordView.setError("Введите пароль");
            return false;
        }
        if (password.length() > 30){
            Toast.makeText(AuthorizationActivity.this,"Слишком длинный пароль. Введите менее 30 знаков",Toast.LENGTH_SHORT).show();
            passwordView.setError("Введите менее 30 знаков");
            return false;
        }
        if (password.length() < 6){
            Toast.makeText(AuthorizationActivity.this,"Слишком короткий пароль. Введите более 5 знаков",Toast.LENGTH_SHORT).show();
            passwordView.setError("Введите более 5 знаков");
            return false;
        }

        return true;
    }

    public void registration(){
        String email = emailView.getText().toString().trim();
        String password = passwordView.getText().toString();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();
                            user.sendEmailVerification();
                            addUser(user);
                            Toast.makeText(AuthorizationActivity.this, "Письмо с подтверждением отправлено вам на почту",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            if (task.getException().getMessage() == "The email address is already in use by another account."){
                                Toast.makeText(AuthorizationActivity.this, "Пользователь с таким адресом уже существует",
                                        Toast.LENGTH_SHORT).show();
                                emailView.setError("Пользователь с таким адресом уже существует");
                            }
                            else{
                                Toast.makeText(AuthorizationActivity.this, task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

    }

    public void authorization(){
        String email = emailView.getText().toString().trim();
        String password = passwordView.getText().toString();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user.isEmailVerified()){
                                Toast.makeText(AuthorizationActivity.this, "Авторизация", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AuthorizationActivity.this, CatalogActivity.class);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(AuthorizationActivity.this, "Подтвердите свою почту, чтобы продолжить", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AuthorizationActivity.this, "Неверный адрес или пароль",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }



    public void addUser(FirebaseUser user){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, String> map = new HashMap<>();
        map.put(System.currentTimeMillis()+"a", "фантастика");
        map.put(System.currentTimeMillis()+"b", "приключения");
        map.put(System.currentTimeMillis()+"c", "детективы");
        map.put(System.currentTimeMillis()+"d", "фэнтези");
        map.put(System.currentTimeMillis()+"f", "наука");
        map.put(System.currentTimeMillis()+"g", "классика");
        map.put(System.currentTimeMillis()+"h", "научно-популярное");
        db.collection("genres").document(user.getUid()).set(map);
    }
}
