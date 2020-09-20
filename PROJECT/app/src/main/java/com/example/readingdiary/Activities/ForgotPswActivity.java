package com.example.readingdiary.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.readingdiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;



public class ForgotPswActivity extends AppCompatActivity
{
    MainActivity mein = new MainActivity();
    EditText  etForg;
    Button btn_ForgPsw;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_psw);

        etForg = (EditText) findViewById(R.id.etForgPsw);
        btn_ForgPsw = (Button)  findViewById(R.id.btn_ForgPsw);

        btn_ForgPsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onForgot();
            }
        });
        progressBar =findViewById(R.id.progressBar3);
    }

    public void onForgot()
        {
        if (!etForg.getText().equals(""))
        {
        mein.mAuth.sendPasswordResetEmail(etForg.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.VISIBLE);
                if (task.isSuccessful())
                {
                    progressBar.setVisibility(View.GONE);
                    if (mein.currentUser==null)
                    {
                        Intent intent = new Intent( ForgotPswActivity .this, MainActivity.class);
                        startActivity(intent);
                    }
                    Toast.makeText(ForgotPswActivity.this,"На вашу почто отправлено письмо. \nДля сброса пароля перейдите по ссылке.",Toast.LENGTH_LONG).show();
                }
                else
                {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ForgotPswActivity.this, "Ошибка: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
        }
        else
            {
                Toast.makeText(ForgotPswActivity.this,"Введите ваш email",Toast.LENGTH_LONG).show();
            }
    }
}
