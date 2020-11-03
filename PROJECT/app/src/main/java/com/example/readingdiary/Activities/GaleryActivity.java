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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readingdiary.Classes.DeleteNote;
import com.example.readingdiary.Classes.ImageClass;
import com.example.readingdiary.Classes.SaveImage;
import com.example.readingdiary.R;
import com.example.readingdiary.adapters.GaleryFullViewAdapter;
import com.example.readingdiary.adapters.GaleryRecyclerViewAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

public class GaleryActivity extends AppCompatActivity {
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
    private ArrayList<Long> selectedImagesList = new ArrayList<>();
    private final int FULL_GALERY_CODE = 8800;
    MaterialToolbar toolbar;
    private int count = 0;
    String id;
    String user;
    private StorageReference imageStorage;
    private DocumentReference imagePathsDoc;
    long time;
    boolean editAccess;
    private String idUser; // в неё передаём id текущего пользовтеля
    boolean action_mode;
    TextView counterText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.galery_activity);
        toolbar = (MaterialToolbar)findViewById(R.id.long_click_toolbar);
        toolbar.setTitle("");
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
        counterText = (TextView)findViewById(R.id.counter_text);
        counterText.setText("Галерея");
        toolbar.getMenu().clear();
        toolbar.setTitle("");
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

            @Override
            public void onItemLongClick(int position) {
                adapter.setActionMode(true);
                action_mode = true;
                counterText.setText(count + " выбрано");
                toolbar.getMenu().clear();
                toolbar.inflateMenu(R.menu.menu_long_click);
                adapter.notifyDataSetChanged();
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            @Override
            public void onCheckClick(int position) {
                selectedImagesList.add(names.get(position));
                count++;
                counterText.setText(count + " выбрано");
            }

            @Override
            public void onUncheckClick(int position) {
                selectedImagesList.remove(names.get(position));
                count--;
                counterText.setText(count + " выбрано");
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
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            if (action_mode){
                action_mode=false;
                adapter.setActionMode(false);
                adapter.notifyDataSetChanged();
                toolbar.getMenu().clear();
                toolbar.inflateMenu(R.menu.base_menu);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                counterText.setText("Галерея");
                count=0;
            }
        }
        if (item.getItemId()== R.id.item_delete)
        {
            action_mode=false;
            adapter.setActionMode(false);
            deleteSelectedImages();
            adapter.notifyDataSetChanged();
            toolbar.getMenu().clear();
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            counterText.setText("Галерея");
            count=0;
        }
        return false;
    }

    private void deleteSelectedImages(){
        DeleteNote.deleteSelectedImages(user, id, selectedImagesList);
        for (Long i : selectedImagesList){
            images.remove(names.indexOf(i));
            names.remove(i);
//            adapter.notifyItemRemoved(names.indexOf(i));
        }
        selectedImagesList.clear();
        adapter.notifyDataSetChanged();

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
