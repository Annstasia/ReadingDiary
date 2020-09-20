package com.example.readingdiary.Activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.readingdiary.Classes.DeleteNote;
import com.example.readingdiary.Classes.DeleteUser;
import com.example.readingdiary.Classes.SaveImage;
//import com.example.readingdiary.Fragments.ChooseDataDialogFragment;
import com.example.readingdiary.Fragments.AddShortNameFragment;
import com.example.readingdiary.Fragments.CreateWithoutNoteDialogFragment;
import com.example.readingdiary.Fragments.DeleteDialogFragment;
import com.example.readingdiary.Fragments.DeleteTitleAndAuthorDialogFragment;
import com.example.readingdiary.Fragments.SaveDialogFragment;
import com.example.readingdiary.Fragments.SettingsDialogFragment;
import com.example.readingdiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditNoteActivity extends AppCompatActivity implements DeleteDialogFragment.DeleteDialogListener,
        CreateWithoutNoteDialogFragment.CreateWithoutNoteDialogListener,
        SaveDialogFragment.SaveDialogListener, SettingsDialogFragment.SettingsDialogListener {
// класс отвечает за активность с каталогами
private String TAG_DARK = "dark_theme";
        SharedPreferences sharedPreferences;
    EditText pathView;
    EditText titleView;
    EditText authorView;
    RatingBar ratingView;
    EditText genreView;
    EditText timeView;
    EditText placeView;
    EditText shortCommentView;
    ImageView coverView;
    String imagePath="";
    String id;
    String path;
    boolean change = false;
    private ImageView imageView;
    private final int Pick_image = 1;
    private final int EDIT_REQUEST_CODE = 123;
    private String[] beforeChanging;
    private final int GALERY_REQUEST_CODE = 124;
    FloatingActionButton acceptButton;
    FloatingActionButton cancelButton;
    Toolbar toolbar;
    boolean isNoteNew;
    private String user = "user0";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference imageStorage;
    private DocumentReference imagePathsDoc;
    long time;
    Bitmap cover;
    CheckBox privacyView;
    boolean isPrivate;
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
        setContentView(R.layout.activity_edit_note);
        user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViews();
        findViews();
        toolbar.inflateMenu(R.menu.base_menu);
        Bundle args = getIntent().getExtras();

        if (args != null && args.get("id") != null){
            isNoteNew=false;
            id = args.get("id").toString();
            select(id);
            Log.d("qwerty71", "startNote");
        }
        else if (args != null && args.get("path") != null){
            isNoteNew=true;
            id = db.collection("Notes").document(user).collection("userNotes").document().getId();
            path = args.get("path").toString();
            beforeChanging = new String[]{path, "", "", "0.0", "", "", "", "", "", "0"};
            isPrivate=false;
            setViews();
        }
        else{
            isNoteNew=true;
            id = db.collection("Notes").document(user).collection("userNotes").document().getId();
            path = "./";
            beforeChanging = new String[]{"./", "", "", "0.0", "", "", "", "", "", "0"};
            isPrivate = false;
            setViews();
        }
        imagePathsDoc = FirebaseFirestore.getInstance().collection("Common").document(user).collection(id).document("Images");
        imageStorage = FirebaseStorage.getInstance().getReference(user).child(id).child("Images");
        Log.d("putExtra", "start");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setButtons();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN){
////            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//            InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
////            findText.clearFocus();
//            setCursorsVisible(false);
//        }
//
//        return super.dispatchTouchEvent(event);
//    }

    @Override
    public void onDeleteClicked() {
        deleteNote();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("deleted", "true");
        returnIntent.putExtra("id", id);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onCreateWithoutNoteClicked() {
        String path1 = pathView.getText().toString();
        path1 = fixPath(path1);
        if (!beforeChanging[0].equals(path1)){
            beforeChanging[0] = path1;
            savePaths();
        }
        if (!imagePath.equals("")){
            new DeleteNote().deleteImages(user, id);
        }
        Intent returnIntent = new Intent();
        returnIntent.putExtra("noNote", "true");
        returnIntent.putExtra("path", path);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onSaveClicked() {
        saveChanges();
//        if (saveChanges()){
//            finish();
//        }
    }

    @Override
    public void onNotSaveClicked() {
        Log.d("qwerty44", imagePath + " " + beforeChanging[8] + " " + isNoteNew);
        if (!imagePath.equals(beforeChanging[8])){
            if (isNoteNew){
                new DeleteNote().deleteImages(user, id);
            }
            else{
                cancelImageChange();
            }
        }

        finish();
    }

    @Override
    public void onChangeThemeClick(boolean isChecked) {
        Toast.makeText(this, "На нас напали светлые маги. Темная тема пока заперта", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onExitClick()
    {
//        int ext =0;
//        ext =1;
        MainActivity MainActivity = new MainActivity();
        MainActivity.currentUser=null;
        MainActivity.mAuth.signOut();
        Intent intent = new Intent(EditNoteActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public void onDelete()
    {
        String currentUser =  FirebaseAuth.getInstance().getCurrentUser().getUid();
        DeleteUser.deleteUser(this, currentUser);
        db.collection("PublicID").document(currentUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot == null || documentSnapshot.getString("id")==null){
                    Toast.makeText(getApplicationContext(),"Аккаунт удалён",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditNoteActivity.this, MainActivity.class);
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
        Intent intent = new Intent(EditNoteActivity.this, ForgotPswActivity.class);
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


    public void findViews(){
        pathView = (EditText) findViewById(R.id.editPath);
        titleView = (EditText) findViewById(R.id.editTitleNoteActivity);
        authorView = (EditText) findViewById(R.id.editAuthorNoteActivity);
        ratingView = (RatingBar) findViewById(R.id.editRatingBar);
        genreView = (EditText) findViewById(R.id.editGenre);
        timeView = (EditText) findViewById(R.id.editTime);
        placeView = (EditText) findViewById(R.id.editPlace);
        shortCommentView = (EditText) findViewById(R.id.editShortComment);
        coverView = (ImageView) findViewById(R.id.editCoverImage);
        acceptButton = (FloatingActionButton) findViewById(R.id.acceptAddingNote2);
        cancelButton = (FloatingActionButton) findViewById(R.id.cancelAddingNote2);
        privacyView = (CheckBox) findViewById(R.id.privacyCheckBox);
 }

    public void setViews(){
        this.pathView.setText(beforeChanging[0].substring(beforeChanging[0].indexOf('/')+1));
        this.authorView.setText(beforeChanging[1]);
        this.titleView.setText(beforeChanging[2]);
        if (!beforeChanging[3].equals("")){
            this.ratingView.setRating(Float.parseFloat(beforeChanging[3]));
        }
        this.genreView.setText(beforeChanging[4]);
        this.timeView.setText(beforeChanging[5]);
        this.placeView.setText(beforeChanging[6]);
        this.shortCommentView.setText(beforeChanging[7]);
        if (!beforeChanging[8].equals("")){
            this.coverView.setImageBitmap(BitmapFactory.decodeFile(beforeChanging[8]));
            this.imagePath = imagePath;
        }
        this.privacyView.setChecked(!isPrivate);
    }

    private void setButtons(){
        FloatingActionButton accept =  (FloatingActionButton) findViewById(R.id.acceptAddingNote2);
        FloatingActionButton cancel =  (FloatingActionButton) findViewById(R.id.cancelAddingNote2);
        Button deleteButton = (Button) findViewById(R.id.deleteNoteButton);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (saveChanges()){
//                    finish();
//                }
                saveChanges();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!imagePath.equals(beforeChanging[8])){
                    if (isNoteNew){
                        DeleteNote.deleteImages(user, id);
                    }
                    else{
                        cancelImageChange();
                    }
                }

                finish();
            }
        });

        Button bAddObl = (Button) findViewById(R.id.bAddObl);
        bAddObl.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                if (isNoteNew == false) {
                    Intent intent = new Intent(EditNoteActivity.this, GaleryActivity.class);
                    intent.putExtra("id", id);
                    startActivityForResult(intent, GALERY_REQUEST_CODE);
                }
                else
                {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, Pick_image);
                }
            }
        });



        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDeletDialog();
            }
        });
    }

    private void openDeletDialog(){
        DeleteDialogFragment dialog = new DeleteDialogFragment();
        dialog.show(getSupportFragmentManager(), "deleteDialog");
    }

    public void select(String id) {
        db.collection("Notes").document(user).collection("userNotes").document(id).get().
                addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d("qwerty71", (documentSnapshot==null)+"");
                        String s = documentSnapshot.get("author").toString();
                        HashMap<String, Object> map = (HashMap<String, Object>) documentSnapshot.getData();
                        if (map != null){
                            imagePath = map.get("imagePath").toString();
                            isPrivate = (boolean)map.get("private");
                            beforeChanging = new String[]{
                                    map.get("path").toString().replace("\\", "/"), map.get("author").toString(),
                                    map.get("title").toString(), map.get("rating").toString(),
                                    map.get("genre").toString(), map.get("time").toString(),
                                    map.get("place").toString(), map.get("short_comment").toString(),
                                    map.get("imagePath").toString(), map.get("timeAdd").toString()};
                            setViews();
                        }
                        else{
                            Log.d("qwerty71", "map == null");
                        }

                    }
                });
    }


    public boolean saveChanges(){
        if (authorView.getText().toString().equals("") && titleView.getText().toString().equals(""))
        {
            showNoTitleAndAuthorDialog();
            return false;
        }


        if (authorView.length()>50){Toast.makeText(EditNoteActivity.this,"Введено слишком большое имя автора ",Toast.LENGTH_SHORT).show(); return false;}
        else if (titleView.length()>50){Toast.makeText(EditNoteActivity.this,"Ведено слишком большое название книги ",Toast.LENGTH_SHORT).show();return false;}
        else if (genreView.length()>50){ Toast.makeText(EditNoteActivity.this,"Введено слишком большое название жанра ",Toast.LENGTH_SHORT).show();return false;}
        else if (timeView.length()>50){Toast.makeText(EditNoteActivity.this,"Введен слишком большой текст для периода прочтения",Toast.LENGTH_SHORT).show();return false;}
        else if (placeView.length()>50){Toast.makeText(EditNoteActivity.this,"Введено слишком большое название места прочтения",Toast.LENGTH_SHORT).show();return false;}
        else if (shortCommentView.length()>50){Toast.makeText(EditNoteActivity.this,"Введен слишком большой краткий комментарий",Toast.LENGTH_SHORT).show();return false;}
        else if (pathView.getText().toString().contains("\\")) {Toast.makeText(EditNoteActivity.this, "Введен недопустимы символ: \\", Toast.LENGTH_LONG).show();return false;}
        String time = (beforeChanging[9].equals("0"))?System.currentTimeMillis()+"":beforeChanging[9];
        String path1 = pathView.getText().toString();
        path1 = fixPath(path1);
        Map<String, Object> note = new HashMap<String, Object>();
        note.put("path", path1.replace("/", "\\"));
        note.put("author", authorView.getText().toString());
        note.put("title", titleView.getText().toString());
        note.put("imagePath", imagePath);
        note.put("rating", String.valueOf(ratingView.getRating()));
        note.put("genre", genreView.getText().toString());
        note.put("time", timeView.getText().toString());
        note.put("place", placeView.getText().toString());
        note.put("short_comment", shortCommentView.getText().toString());
        note.put("timeAdd", time);
        if (privacyView.isChecked() && (isPrivate || isNoteNew)){
            Map<String, String> map = new HashMap<>();
//            List<String> list = new ArrayList<>();
            map.put(time, id);
            db.collection("Publicly").document(user).set(map, SetOptions.merge());
        }
        else if (!privacyView.isChecked() && !isPrivate){
            db.collection("Publicly").document(user).update(time, FieldValue.delete());
        }
        note.put("private", !privacyView.isChecked());

        if (!beforeChanging[0].equals(path1)){
            beforeChanging[0] = path1;
            savePaths();
        }
        if (isNoteNew == true){
            note.put("publicRatingSum", 0.0);
            note.put("publicRatingCount", 0);
            db.collection("Notes").document(user).collection("userNotes").document(id).set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    insertIntent();
                    closeActivity();
//                    retut
                }
            });
//            HashMap<String, Boolean> map = new HashMap<String, Boolean>();
//            insertIntent();
        }
        else
        {
            db.collection("Notes").document(user).collection("userNotes").document(id).set(note, SetOptions.merge());
            changedIntent();
            closeActivity();
        }
        Log.d("qwerty71", "save");

        return true;
    }

    private void closeActivity(){
        finish();
    }

    private void showNoTitleAndAuthorDialog(){
        if (isNoteNew){
            CreateWithoutNoteDialogFragment createDialog = new CreateWithoutNoteDialogFragment();
            createDialog.show(getSupportFragmentManager(), "createWithoutNoteDialog");
        }
        else{
            DeleteTitleAndAuthorDialogFragment dialog = new DeleteTitleAndAuthorDialogFragment();
            dialog.show(getSupportFragmentManager(), "deleteTitleAndAuthorDialog");
        }
    }

    public boolean checkChanges(){
        Log.d("putExtra", ratingView.getRating() +"");
        if (beforeChanging[0].equals(fixPath(pathView.getText().toString())) &&
                beforeChanging[1].equals(authorView.getText().toString()) &&
                beforeChanging[2].equals(titleView.getText().toString()) &&
                beforeChanging[3].equals(ratingView.getRating()+"")  &&
                beforeChanging[4].equals(genreView.getText().toString()) &&
                beforeChanging[5].equals(timeView.getText().toString()) &&
                beforeChanging[6].equals(placeView.getText().toString()) &&
                beforeChanging[7].equals(shortCommentView.getText().toString()) &&
                beforeChanging[8].equals(imagePath) && isPrivate==privacyView.isChecked())
        {
            return false;
        }
        return true;
    }

    public void savePaths(){
        final String pathTokens[] = ((String) beforeChanging[0]).split("/");
        String prev="";
        for (int i = 0; i < pathTokens.length - 1; i++) {
            if (pathTokens[i].equals("")) {
                continue;
            }
            final String prev0 = prev;
            final String doc = prev + pathTokens[i] + "\\";
            final String toAdd = prev + pathTokens[i] + "\\" + pathTokens[i+1]+"\\";
            db.collection("User").document(user).collection("paths").document(doc)
                    .update("paths", FieldValue.arrayUnion(toAdd))
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Map<String, Object> map = new HashMap<>();
                            List<String> list = new ArrayList<>();
                            list.add(toAdd);
                            map.put("parent", prev0);
                            map.put("paths", list);
                            db.collection("User").document(user).collection("paths").document(doc)
                                    .set(map);

                        }
                    });
//                    .set(map, SetOptions.merge());
            prev += pathTokens[i] + "\\";
        }
        Map<String, Object> map = new HashMap<>();
//        map.put("paths", new ArrayList<String>());
        map.put("parent", prev);
        db.collection("User").document(user).collection("paths").document(
                prev+pathTokens[pathTokens.length - 1]+"\\").set(map, SetOptions.merge());

    }

    public String fixPath(String path){
        if (path.equals("") || path.equals("/")) path = "./";
        else{
            if (path.charAt(path.length() - 1) != '/'){
                path = path + "/";
            }
            if (path.charAt(0) == '/'){
                path = "." + path;
            }
            if (path.charAt(0) != '.'){
                path = "./" + path;
            }
        }
        return path;
    }

    private void deleteNote(){
        if (!isNoteNew) {
            DeleteNote.deleteNote(user, id);
        }
        else{
            DeleteNote.deleteImages(user, id);
            if (!isPrivate){
                DeleteNote.deletePublicly(user, id);
            }
        }
    }
    public void changedIntent(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("changed", "true");
        setResult(RESULT_OK, returnIntent);
    }

    public void insertIntent()
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("id", id);
        returnIntent.putExtra("path", path);
        setResult(RESULT_OK, returnIntent);
    }

    private void saveDialog(){
        SaveDialogFragment saveDialogFragment = new SaveDialogFragment(this);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        saveDialogFragment.show(transaction, "dialog");
        FirebaseFirestore.getInstance().collection("Common").document(user).collection(id).document("Images").get().
                addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.getData()!=null){
                            Log.d("qwerty43", "saveD1");
                        }
                        else{
                            Log.d("qwerty43", "saveD1Null");
                        }
                    }
                });
    }


    private void saveAndOpenImage(final Uri imageUri){
        time = System.currentTimeMillis();
        imagePath = time+"";
        cover = SaveImage.saveImage(user, id, imageUri, time, getApplicationContext());
        coverView.setImageBitmap(cover);
    }

    private void cancelImageChange(){
        db.collection("Notes").document(user).collection("userNotes").document(id).
                update("imagePath", beforeChanging[8]);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Pick_image){
          //  Toast.makeText(getApplicationContext(), "pick_image", Toast.LENGTH_LONG).show();
            if (data != null){
                try{
                    saveAndOpenImage(data.getData());
                }
                catch (Exception e){
                    Log.e("EditNoteResult", e.toString());
                }

            }
        }
        else if (requestCode==GALERY_REQUEST_CODE){
            db.collection("Notes").document(user).collection("userNotes").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null && documentSnapshot.get("imagePath")!= null && !documentSnapshot.get("imagePath").equals("")){
                        imagePath = documentSnapshot.get("imagePath").toString();
                        imageStorage.child(imagePath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).into(coverView);
                            }
                        });
                    }
                }
            });
         //   Toast.makeText(getApplicationContext(), "galery_request", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Toast.makeText(getApplicationContext(), "backPressed!1 " + keyCode , Toast.LENGTH_LONG).show();
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            Toast.makeText(getApplicationContext(), "backPressed!1", Toast.LENGTH_LONG).show();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onBackPressed() {
   //     Toast.makeText(getApplicationContext(), "backPressed", Toast.LENGTH_LONG).show();
        Log.d("QWERTY", "backPressed");
        if (checkChanges()){
            saveDialog();
        }
        else{
            finish();
        }

//        acceptButton.setVisibility(View.VISIBLE);
//        cancelButton.setVisibility(View.VISIBLE);
//        super.onBackPressed();

//        saveChanges();
//        super.onBackPressed();
//        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

