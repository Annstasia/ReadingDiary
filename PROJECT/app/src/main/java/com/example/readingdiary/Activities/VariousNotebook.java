package com.example.readingdiary.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.readingdiary.Classes.DeleteUser;
import com.example.readingdiary.Fragments.AddShortNameFragment;
import com.example.readingdiary.Fragments.SaveDialogFragment;
import com.example.readingdiary.Fragments.SettingsDialogFragment;
import com.example.readingdiary.Fragments.WrongLengthDialogFragment;
import com.example.readingdiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;


public class VariousNotebook extends AppCompatActivity implements SaveDialogFragment.SaveDialogListener,
        SettingsDialogFragment.SettingsDialogListener, WrongLengthDialogFragment.WrongLengthDialogListener {
    // класс отвечает за активность с каталогами
    private String TAG_DARK = "dark_theme";
    SharedPreferences sharedPreferences;
    private boolean shouldSave = true;
    private String id;
    private String type;
    public TextInputEditText text;
    private String path;
    private String position;
    MaterialToolbar toolbar;
    String user;
    private DocumentReference variousNotePaths;
    private CollectionReference variousNoteStorage;
    MainActivity mein = new MainActivity();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = this.getSharedPreferences(TAG_DARK, Context.MODE_PRIVATE);
        boolean dark = sharedPreferences.getBoolean(TAG_DARK, false);
        if (dark){
            setTheme(R.style.DarkTheme);
        }
        else{
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coments);
        toolbar = findViewById(R.id.base_toolbar);
        user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Bundle args = getIntent().getExtras();
        id = args.get("id").toString();
        type = args.get("type").toString();
        variousNoteStorage = FirebaseFirestore.getInstance().collection("VariousNotes").document(user).collection(id);
        variousNotePaths = variousNoteStorage.document(type);

        if (type.equals("description")){
            TextView textView12 = (TextView) findViewById(R.id.textView12);
        }
        text = (TextInputEditText) findViewById(R.id.editTextComments);
        if (args.get("path") != null){
            path = args.get("path").toString();
            try{
                openText();
                position= args.get("position").toString();
            }
            catch (Exception e){
                Log.e("openTextException", e.toString());
            }
        }
    }

    @Override
    public void onBackPressed() {
        dialogSaveOpen();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveClicked() {
        returnResult(saveText());
        super.onBackPressed();
    }


    @Override
    public void onChangeThemeClick(boolean isChecked) {
        Toast.makeText(this, "На нас напали светлые маги. Темная тема пока заперта", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onExitClick() {
//        ext =1;
        MainActivity MainActivity = new MainActivity();
        MainActivity.currentUser=null;
        MainActivity.mAuth.signOut();
        Intent intent = new Intent(VariousNotebook.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public void onDelete()
    {
        DeleteUser.deleteUser(this, user);
        FirebaseFirestore.getInstance().collection("PublicID").document(user).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot == null || documentSnapshot.getString("id")==null){
                    Toast.makeText(getApplicationContext(),"Аккаунт удалён",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VariousNotebook.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onChangeIdClick(String userID) {
        AddShortNameFragment saveDialogFragment = new AddShortNameFragment(true, userID, user);
        saveDialogFragment.setCancelable(false);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        saveDialogFragment.show(transaction, "dialog");
//        this.userID = userID;
    }


    @Override
    public void onForgot()
    {
        Intent intent = new Intent(VariousNotebook.this, ForgotPswActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_settings) {
            int location[] = new int[2];
            toolbar.getLocationInWindow(location);
            int y = getResources().getDisplayMetrics().heightPixels;
            int x = getResources().getDisplayMetrics().widthPixels;

            SettingsDialogFragment settingsDialogFragment = new SettingsDialogFragment(y, x, sharedPreferences.getBoolean(TAG_DARK, false));
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            settingsDialogFragment.show(transaction, "dialog");
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }


    private void openText() throws Exception{
        variousNoteStorage.document(path).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                text.setText(documentSnapshot.get("text").toString());
            }
        });
    }

    private long saveText(){

        try{
            final long time = (path==null)?System.currentTimeMillis():Long.parseLong(path);
//            final long time = System.currentTimeMillis();
            Map<String, Boolean> map = new HashMap<>();
            map.put(time+"", false);
            variousNotePaths.set(map, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Map<String, String> map1 = new HashMap<String, String>();
                            map1.put("text", text.getText().toString());
                            variousNoteStorage.document(time+"").set(map1)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                           // Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_LONG).show();
                                            variousNotePaths.update(time+"", true);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("qwerty40", e.toString());
                                            variousNotePaths.update(time+"", FieldValue.delete());
                                        }
                                    });
                        }});
            return (path==null)?time:-2;

        }

        catch (Exception e){
            Log.e("openException", e.toString());
        }
        return -1;
    }

    private void returnResult(long time){
        if (time == -1) return;
        Intent resultIntent = new Intent();
        if (time == -2) {
            resultIntent.putExtra("updatePath", path);
            resultIntent.putExtra("position", position);
        }
        else{
            resultIntent.putExtra("time", time+"");
        }
        setResult(RESULT_OK, resultIntent);

    }

    private void dialogSaveOpen(){
        if (text.getText().toString().toString().length() == 0 || text.getText().toString().toString().length() > 5000){
            WrongLengthDialogFragment dialog = new WrongLengthDialogFragment(getApplicationContext(),
                    text.getText().toString().length());
            dialog.show(getSupportFragmentManager(), "wrongLengthDialog");
        }
        else{
                    SaveDialogFragment dialog = new SaveDialogFragment(getApplicationContext());
            dialog.show(getSupportFragmentManager(), "saveNoteDialog");
        }

    }

    @Override
    public void onNotSaveClicked() {
        finish();
    }
}
