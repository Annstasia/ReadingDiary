package com.example.readingdiary.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.readingdiary.Classes.DeleteUser;
import com.example.readingdiary.Classes.Note;
import com.example.readingdiary.Fragments.AddShortNameFragment;
import com.example.readingdiary.Fragments.SettingsDialogFragment;
import com.example.readingdiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class NoteActivity extends AppCompatActivity implements SettingsDialogFragment.SettingsDialogListener {
    // класс отвечает за активность с каталогами
    private String TAG_DARK = "dark_theme";
    SharedPreferences sharedPreferences;
    TextView pathView;
    TextView titleView;
    TextView authorView;
    RatingBar ratingView;
    TextView genreView;
    TextView timeView;
    TextView placeView;
    TextView shortCommentView;
    ImageView coverView;
    String imagePath;
    Uri imageUri;
    String id;
    String path;
    boolean changed = false;
    private ImageView imageView;
    private final int Pick_image = 1;
    private final int EDIT_REQUEST_CODE = 123;
    private final int GALERY_REQUEST_CODE = 124;
    private final int COMENTS_REQUEST_CODE = 125;
    Toolbar toolbar;
    ImageButton bUpload;
    private String user = "user0";
    private String currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean editAccess;
    MainActivity mein = new MainActivity();
    private boolean exists=true;


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
        setContentView(R.layout.activity_note);
//        user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        findViews();
        ratingView.setEnabled(false);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle args = getIntent().getExtras();
        id = args.get("id").toString();
        if (args.get("changed") != null && args.get("changed").equals("true")){
            changed = true;
        }
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (args.get("owner")!= null){
            user = args.get("owner").toString();
            editAccess = false;
        }
        else{
            user=currentUser;
            editAccess = true;
        }
        select(id); // Заполнение полей из бд
        setButtons();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        if (editAccess){
            getMenuInflater().inflate(R.menu.menu_note, menu);
        }
        getMenuInflater().inflate(R.menu.base_menu, menu);

        return true;
    }

    @Override
    public void onChangeThemeClick(boolean isChecked) {
        Toast.makeText(this, "На нас напали светлые маги. Темная тема пока заперта", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onExitClick() {
        MainActivity MainActivity = new MainActivity();
        MainActivity.currentUser=null;
        MainActivity.mAuth.signOut();
        Intent intent = new Intent(NoteActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDelete()
    {
        DeleteUser.deleteUser(this, user);
        db.collection("PublicID").document(user).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot == null || documentSnapshot.getString("id")==null){
                    Toast.makeText(getApplicationContext(),"Аккаунт удалён",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(NoteActivity.this, MainActivity.class);
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
        Intent intent = new Intent(NoteActivity.this, ForgotPswActivity.class);
        startActivity(intent);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.item_settings) {
            int location[] = new int[2];
            toolbar.getLocationInWindow(location);
            int y = getResources().getDisplayMetrics().heightPixels;
            int x = getResources().getDisplayMetrics().widthPixels;

            SettingsDialogFragment settingsDialogFragment = new SettingsDialogFragment(y, x, sharedPreferences.getBoolean(TAG_DARK, false));
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            settingsDialogFragment.show(transaction, "dialog");
        }
//        int id = item.getItemId();
        int it = item.getItemId();
        if (it == R.id.edit_note) {
            Intent intent = new Intent(NoteActivity.this, EditNoteActivity.class);
            intent.putExtra("id", id);
            startActivityForResult(intent, EDIT_REQUEST_CODE);
            return super.onOptionsItemSelected(item);
        }
        return true;
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (changed){
            changedIntent();
        }
        super.onBackPressed();
        // нужно сделать проверку ответа
        finish();
    }

    @Override
    protected void onDestroy() {
        exists=false;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==EDIT_REQUEST_CODE){
            if (data != null && data.getExtras() != null)
            {
                if (data.getExtras().get("deleted") != null)
                {
                    setResult(RESULT_OK, data);
                    finish();
                }
                if (data.getExtras().get("changed") != null)
                {
                    changed = true;
                    changedIntent();
                    select(id);
                }
            }
            else{
                select(id);
            }

        }

        if (requestCode==GALERY_REQUEST_CODE){
            select(id);
        }

    }

    private void setViews(String path, String author, String title, String rating, String genre,
                          String time, String place, String shortComment){
        this.path = path;
        this.authorView.setText(author);
       // Toast.makeText(this, "!" + author + " " + author.equals(""), Toast.LENGTH_SHORT).show();
        if (author.equals("")){
            this.authorView.setVisibility(View.GONE);
        }
        this.titleView.setText(title);
        if (title.equals("")){
            this.titleView.setVisibility(View.GONE);
        }
        if (rating != null){
            this.ratingView.setRating(Float.parseFloat(rating));
        }
        if (rating == null || rating.equals("0.0")){
            this.ratingView.setVisibility(View.GONE);
        }

        this.genreView.setText(genre);
        if (title.equals("")){
            this.titleView.setVisibility(View.GONE);
        }
        this.timeView.setText(time);
        if (time.equals("")){
            this.timeView.setVisibility(View.GONE);
        }
        this.placeView.setText(place);
        if (place.equals("")){
            this.placeView.setVisibility(View.GONE);
        }
        this.shortCommentView.setText(shortComment);
        if (shortComment.equals("")){
            this.shortCommentView.setVisibility(View.GONE);
        }
//        File file = new File(imagePath);
        Log.d("IMAGE1", imagePath +" !");
    }

    private void setDefaultImage(){
        coverView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.no_image));
    }

    private void setImage(Uri imageUri){
        if (imageUri != null) {
            Log.d("qwerty123456", imageUri.toString());
            DisplayMetrics metricsB = getResources().getDisplayMetrics();
            float size = metricsB.widthPixels / 3;
            Picasso.get()
                    .load(imageUri)
                    .resize(metricsB.widthPixels, metricsB.heightPixels)
                    .centerInside()
                    .into(this.coverView);
        }
        else{
            this.coverView.setVisibility(View.GONE);
        }
    }


    private void setButtons(){
        Button pickImage = (Button) findViewById(R.id.galeryButton); // переход в галерею
        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NoteActivity.this, GaleryActivity.class);
                intent.putExtra("id", id);
                Log.d("qwerty49", "access" + editAccess);
                if (!editAccess){
                    intent.putExtra("owner", user);
                }
                startActivityForResult(intent, GALERY_REQUEST_CODE);
            }
        });

        ImageButton bUpload = (ImageButton) findViewById(R.id.bUpload); // переход в галерею
        if (editAccess){
            bUpload.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //загрузка записи в сеть
                    Toast.makeText(NoteActivity.this,"Запись опубликована \n(допиши добавление в бд NoteActivity стр 207)",Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            bUpload.setVisibility(View.GONE);
            findViewById(R.id.textView11).setVisibility(View.GONE);
        }

        Button coments = (Button) findViewById(R.id.comentsButton);
        coments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NoteActivity.this, VariousShow.class);
                intent.putExtra("id", id);
                if (!editAccess){
                    intent.putExtra("owner", user);
                }
                intent.putExtra("type", getResources().getString(R.string.commentDir));
                startActivityForResult(intent, COMENTS_REQUEST_CODE);
            }
        });

        Button description = (Button) findViewById(R.id.descriptionButton);
        description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NoteActivity.this, VariousShow.class);
                intent.putExtra("id", id);
                intent.putExtra("type", getResources().getString(R.string.descriptionDir));
                if (!editAccess){
                    intent.putExtra("owner", user);
                }
                startActivityForResult(intent, COMENTS_REQUEST_CODE);
            }
        });

        Button quotes = (Button) findViewById(R.id.quoteButton);
        quotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NoteActivity.this, VariousShow.class);
                intent.putExtra("id", id);
                intent.putExtra("type", getResources().getString(R.string.quoteDir));
                if (!editAccess){
                    intent.putExtra("owner", user);
                }
                startActivityForResult(intent, COMENTS_REQUEST_CODE);
            }
        });

    }

    private void findViews(){
//        TextView path;
        titleView = (TextView) findViewById(R.id.titleNoteActivity);
        authorView = (TextView) findViewById(R.id.authorNoteActivity);
        ratingView = (RatingBar) findViewById(R.id.ratingBar);
        genreView = (TextView) findViewById(R.id.genre);
        timeView = (TextView) findViewById(R.id.time);
        placeView = (TextView) findViewById(R.id.place);
        shortCommentView = (TextView) findViewById(R.id.shortComment);
        coverView = (ImageView) findViewById(R.id.coverImage);


    }

    private void select(String id){
        // Выбор полей из бд
        // Сейчас тут выбор не всех полей
        final String id0 = id;
        Log.d("qwerty71", "hiSelect");
        db.collection("Notes").document(user).collection("userNotes").document(id).get().
                addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d("qwerty71", "doc " + (documentSnapshot==null));
                        String s = documentSnapshot.get("author").toString();

                        final HashMap<String, Object> map = (HashMap<String, Object>) documentSnapshot.getData();
                        imagePath = "";
                        if (map != null){
                            setViews(
                                    map.get("path").toString(), map.get("author").toString(),
                                    map.get("title").toString(), map.get("rating").toString(),
                                    map.get("genre").toString(), map.get("time").toString(),
                                    map.get("place").toString(), map.get("short_comment").toString()
                            );
                            Log.d("qwerty71", "mapNotNull");
                            if (map.get("imagePath") != null && !map.get("imagePath").equals("")){
                                Log.d("qwerty71", "image1");
                                if (NoteActivity.this == null){

                                    Log.d("qwerty73", "nullActivity");
                                }
                                if (!exists) return;
                                db.collection("Common").document(user).collection(id0).document("Images").addSnapshotListener(NoteActivity.this, new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                        if (e == null){
                                            Log.d("qwerty71", "event" + (documentSnapshot==null));
                                            HashMap<String, Boolean> imagesLinks= (HashMap) documentSnapshot.getData();
                                            Log.d("qwerty71", "imageLinks " + (imagesLinks==null));
                                            if (imagesLinks != null && imagesLinks.get(map.get("imagePath")) == true){
                                                Log.d("qwerty71", "imagePath: " + map.get("imagePath").toString());
                                                FirebaseStorage.getInstance().getReference(user).child(id0).child("Images").child(map.get("imagePath").toString()).getDownloadUrl()
                                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                Log.d("qwerty123456", "hiStart " + uri);
                                                                imageUri = uri;
                                                                setImage(imageUri);
                                                            }
                                                        });
                                            }
                                            else if (imagesLinks != null && imagesLinks.get(map.get("imagePath")) == false){
                                                setDefaultImage();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                        else{
                            Log.d("qwerty71", "map == null");
                        }
                    }
                });
    }

    private void changedIntent(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("path", path);
        returnIntent.putExtra("id", id);
        setResult(RESULT_OK, returnIntent);
        Log.d("qwertyu", "changeIntent");
    }


}
