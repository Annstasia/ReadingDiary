package com.example.readingdiary.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.readingdiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
        emailView = (EditText) findViewById(R.id.emailView);
        passwordView = (EditText) findViewById(R.id.passwordView);
        findViewById(R.id.registrationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkFormat()){
                    registration();
                }
            }
        });
        findViewById(R.id.authorizationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkFormat()){
                    authorization();
                }
            }
        });

        if (auth.getCurrentUser() != null){
            Intent intent = new Intent(AuthorizationActivity.this, CatalogActivity.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        updateUI(user);
    }

    public boolean checkFormat(){
        String email = emailView.getText().toString().trim();
        String password = passwordView.getText().toString();
        if (email.isEmpty()  && password.isEmpty())
        {
            Toast.makeText(AuthorizationActivity.this,"Введите логин и пароль",Toast.LENGTH_SHORT).show();
            return false;
        }

       if (email.isEmpty())
        {
            Toast.makeText(AuthorizationActivity.this,"Введите логин",Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty())
        {
            Toast.makeText(AuthorizationActivity.this,"Введите пароль",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() > 100){
            Toast.makeText(AuthorizationActivity.this,"Слишком длинный пароль. Введите менее 100 знаков",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6){
            Toast.makeText(AuthorizationActivity.this,"Слишком короткий пароль. Введите более 5 знаков",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (email.indexOf('@') < 1 || email.indexOf('.') < 1 ||  email.indexOf('@') > email.lastIndexOf('.')){
            Toast.makeText(AuthorizationActivity.this,"Неверный формат почты",Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

//        else
//        {
//            if (view.getId() == R.id.btn_ForgPsw)
//            {
//                signin(ETemail.getText().toString(), ETpassword.getText().toString());
//            }
//            else if (view.getId() == R.id.btn_registration)
//            {
//                registration(ETemail.getText().toString(), ETpassword.getText().toString());
//            }
//        }
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
                            updateUI(user);
                            Toast.makeText(AuthorizationActivity.this, "Письмо с подтверждением отправлено вам на почту",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            if (task.getException().getMessage() == "The email address is already in use by another account."){
                                Toast.makeText(AuthorizationActivity.this, "Пользователь с таким адресом уже существует",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(AuthorizationActivity.this, task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }

                            Log.w("firebaseAuthorization", "signInWithEmail:failure", task.getException());
                            updateUI(null);
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
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("firebaseAuthorization", "signInWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
                            updateUI(user);
                            if (user.isEmailVerified()){
                                Toast.makeText(AuthorizationActivity.this, "Авторизация", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AuthorizationActivity.this, CatalogActivity.class);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(AuthorizationActivity.this, "Подтвердите свою почту, чтобы продолжить", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("firebaseAuthorization", "signInWithEmail:failure", task.getException());
                            Toast.makeText(AuthorizationActivity.this, "Неверный адрес или пароль",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                            // ...
                        }

                    }
                });
    }

    public void updateUI(FirebaseUser user){

    }

    public void addUser(FirebaseUser user){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, String> map = new HashMap<>();
//        map.put("фантастика", false);
//        map.put("приключения", false);
//        map.put("детективы", false);
//        map.put("фэнтези", false);
//        map.put("наука", false);
//        map.put("классика", false);
//        map.put("научно-популярное", false);
//        db.collection("genres").document(user.getUid()).set(map);

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
