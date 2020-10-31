package com.example.readingdiary.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readingdiary.Classes.DeleteUser;
import com.example.readingdiary.Classes.ImageClass;
import com.example.readingdiary.Classes.SaveImage;
import com.example.readingdiary.Fragments.AddShortNameFragment;
import com.example.readingdiary.Fragments.SettingsDialogFragment;
import com.example.readingdiary.R;
import com.example.readingdiary.adapters.GaleryFullViewAdapter;
import com.example.readingdiary.adapters.GaleryRecyclerViewAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

public class GaleryActivity extends AppCompatActivity implements SettingsDialogFragment.SettingsDialogListener {
    // класс отвечает за активность с каталогами
    private String TAG_DARK = "dark_theme";
    SharedPreferences sharedPreferences;
    private ImageView imageView;
    private final int Pick_image = 1;
    private RecyclerView galeryView;;
    private GaleryRecyclerViewAdapter adapter;
    private GaleryFullViewAdapter adapter1;
    private List<ImageClass> images;
    private List<Long> names;
    private final int FULL_GALERY_CODE = 8800;
    Toolbar toolbar;
    private int count = 3;
    String id;
    String user;
    private StorageReference imageStorage;
    private DocumentReference imagePathsDoc;
    long time;
    boolean editAccess;
    private String idUser; // в неё передаём id текущего пользовтеля

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
        setContentView(R.layout.galery_activity);
        toolbar = findViewById(R.id.base_toolbar);
        setSupportActionBar(toolbar);
        Bundle args = getIntent().getExtras();
        id = args.get("id").toString();
        if (args.get("owner")==null){
            user = FirebaseAuth.getInstance().getUid();
            editAccess = true;
        }
        else{

            user = args.get("owner").toString();
            Log.d("qwerty49", "owner " + user);
            editAccess = false;
        }





//        idUser = user; // только для тестов, потом обязательно удали



        imagePathsDoc = FirebaseFirestore.getInstance().collection("Common").document(user).collection(id).document("Images");
        imageStorage = FirebaseStorage.getInstance().getReference(user).child(id).child("Images");
        images = new ArrayList<>(); // список bitmap изображений
        names = new ArrayList<Long>(); // список путей к изображениями в файловой системе
//        newImages = new ArrayList<>();

        galeryView = (RecyclerView) findViewById(R.id.galery_recycle_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3); // отображение изображений в 3 колонки
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        adapter = new GaleryRecyclerViewAdapter(images, getApplicationContext());
        galeryView.setAdapter(adapter);
        galeryView.setItemAnimator(itemAnimator);
        galeryView.setLayoutManager(layoutManager);
        Button pickImage = (Button) findViewById(R.id.button);

        if (!editAccess){pickImage.setVisibility(View.GONE);} //проверка на автора

        imagePathsDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null){
                 //   Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    HashMap<String, Boolean> hashMap = (HashMap) documentSnapshot.getData();
                    if (hashMap != null){
                        for (String key : hashMap.keySet()){
                            final Long l = Long.parseLong(key);
                            if (names.contains(l) && hashMap.get(key) == true && images.get(names.indexOf(l)).getType()==0){
                                imageStorage.child(key).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        images.set(names.indexOf(l), new ImageClass(uri));
                                        adapter.notifyItemChanged(names.indexOf(l));
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                             //   Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });

                            }
                            else if (!names.contains(l)){
                                if (hashMap.get(key)==false){
                                    int index = -1;
                                    for (int i = 0; i < names.size(); i++){
                                        if (names.get(i) > l){
                                            index = i;
                                            break;
                                        }
                                    }
                                    if (index==-1){
                                        names.add(l);
                                        images.add(new ImageClass(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_image)));
                                        adapter.notifyItemInserted(images.size()-1);
                                    }
                                    else{
                                        names.add(index, l);
                                        images.add(index, new ImageClass(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_image)));
                                        adapter.notifyItemInserted(index);
                                    }
                                }
                                else{
                                    imageStorage.child(key).getDownloadUrl().
                                            addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                   // Toast.makeText(getApplicationContext(), "uri ", Toast.LENGTH_LONG).show();
                                                    int index = -1;
                                                    for (int i = 0; i < names.size(); i++){
                                                        if (names.get(i) > l){
                                                            index = i;
                                                            break;
                                                        }
                                                    }
                                                    if (index==-1){
                                                        names.add(l);
                                                        images.add(new ImageClass(uri));
                                                        adapter.notifyItemInserted(images.size()-1);
                                                    }
                                                    else{
                                                        names.add(index, l);
                                                        images.add(index, new ImageClass(uri));
                                                        adapter.notifyItemInserted(index);
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                  //  Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            }
                        }
                    }
                }
            }
        });

        // при нажатии на изображение переходим в активность с полным изображением
        adapter.setOnItemClickListener(new GaleryRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(GaleryActivity.this, GaleryFullViewActivity.class);
                intent.putExtra("id", id);
                if (!editAccess){
                    intent.putExtra("owner", user);
                }
                intent.putExtra("position", names.get(position));
                startActivityForResult(intent, FULL_GALERY_CODE);
            }
        });





        // Выбор изображений из галереи
        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, Pick_image);
            }
        });
        setResultChanged();
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
        Intent intent = new Intent(GaleryActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public void onDelete()
    {
        DeleteUser.deleteUser(this, user);
        FirebaseFirestore.getInstance().collection("PublicID").document(user).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable DocumentSnapshot documentSnapshot, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                if (documentSnapshot == null || documentSnapshot.getString("id")==null){
                    Toast.makeText(getApplicationContext(),"Аккаунт удалён",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(GaleryActivity.this, MainActivity.class);
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
    }

    @Override
    public void onForgot() {
        Intent intent = new Intent(GaleryActivity.this, ForgotPswActivity.class);
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

    public void setResultChanged(){
        // создание возвращаемого интента
        Intent returnIntent = new Intent();
//        returnIntent.putExtra("changed", "changed);
        setResult(RESULT_OK, returnIntent);
    }


    // методы проверки размера изображения до открытия. Если размер слишком большой - сжимаем
    private void saveAndOpenImage(Uri imageUri) throws Exception{
        int px = 600;
        time = System.currentTimeMillis();
//
        Bitmap bitmap = SaveImage.saveImage(user, id, imageUri, time, getApplicationContext());

        int pos = names.size();
        int n = names.size();
        for (int i = 0; i < n; i++){
            if (names.get(i) > time){
                pos = i;
                break;
            }
        }
        images.add(pos, new ImageClass(bitmap));
        names.add(pos, time);
        adapter.notifyItemInserted(pos);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Pick_image)
        {
            if (resultCode == RESULT_OK) {
                try {
                    saveAndOpenImage(data.getData());
                    // Обработка выбранного изображения из галереи
                } catch (Exception e) {
                    Log.d("IMAGE1", e.toString());
                }
            }
        }

        else if (requestCode == FULL_GALERY_CODE) {
            Log.d("DELETEIMAGE123", "request");

            // Вернулись из показа полных изображений. Если там удалили изображение, то меняем список имен и изображений
            if (resultCode == RESULT_OK) {

                imagePathsDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null){
                            List<Integer> index = new ArrayList<>();
                            HashMap<String, Boolean> map = (HashMap) documentSnapshot.getData();
                            if (map!=null){
                                for (int i = 0; i < names.size(); i++) {
                                    if (!map.containsKey(names.get(i).toString())){
                                        index.add(i);
                                    }
                                }
                                int minus = 0;
                                for (int i : index) {
                                    names.remove(i - minus);
                                    images.remove(i - minus);
                                    adapter.notifyItemRemoved(i - minus);
                                    minus++;
                                }
                            }
                        }
                    }
                });
            }
        }


    }



}
