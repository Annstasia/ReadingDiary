package com.example.readingdiary.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.readingdiary.Fragments.SaveDialogFragment;
import com.example.readingdiary.Fragments.WrongLengthDialogFragment;
import com.example.readingdiary.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;


public class VariousNotebook extends AppCompatActivity implements SaveDialogFragment.SaveDialogListener,
        WrongLengthDialogFragment.WrongLengthDialogListener {
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
