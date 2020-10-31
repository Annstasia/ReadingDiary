package com.example.readingdiary.Activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readingdiary.Classes.DeleteUser;
import com.example.readingdiary.Classes.ImageClass;
import com.example.readingdiary.Fragments.AddShortNameFragment;
import com.example.readingdiary.Fragments.DeleteDialogFragment;
import com.example.readingdiary.Fragments.SetCoverDialogFragment;
import com.example.readingdiary.Fragments.SettingsDialogFragment;
import com.example.readingdiary.R;
import com.example.readingdiary.adapters.GaleryFullViewAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;


public class GaleryFullViewActivity extends AppCompatActivity implements DeleteDialogFragment.DeleteDialogListener,
        SetCoverDialogFragment.SetCoverDialogListener, SettingsDialogFragment.SettingsDialogListener {
    // класс отвечает за активность с каталогами
    private String TAG_DARK = "dark_theme";
    SharedPreferences sharedPreferences;
    private RecyclerView galeryFullView;;
    int position;
    long positionName;
    private GaleryFullViewAdapter adapter;
    private List<ImageClass> images;
    private List<Long> names;
    private boolean changed = false;
    String id;
    MaterialToolbar toolbar;
    boolean chooseCover = false;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String user;
    private StorageReference imageStorage;
    private DocumentReference imagePathsDoc;
    boolean editAccess;


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
        setContentView(R.layout.activity_galery_full_view);

        toolbar = (MaterialToolbar)findViewById(R.id.base_toolbar);
        setSupportActionBar(toolbar);
        // открываем и сохраняем в список изображения для данной записи
        Bundle args = getIntent().getExtras();
        id = args.get("id").toString();
        if (args.get("owner")==null){
            user = FirebaseAuth.getInstance().getUid();
            editAccess = true;
        }
        else{
            user = args.get("owner").toString();
            editAccess = false;
        }
        imagePathsDoc = FirebaseFirestore.getInstance().collection("Common").document(user).collection(id).document("Images");
        imageStorage = FirebaseStorage.getInstance().getReference(user).child(id).child("Images");
//        position = Integer.parseInt(args.get("position").toString());
        positionName = Long.parseLong(args.get("position").toString());
        position = 0;
        images = new ArrayList<>();
        names = new ArrayList<>();
        Button deleteButton = (Button) findViewById(R.id.deleteFullImageButton);
        Button coverButton = (Button) findViewById(R.id.setAsCoverButton);
        galeryFullView = (RecyclerView) findViewById(R.id.galery_full_recycle_view);
        final PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(galeryFullView);
        // добавляем адаптер
        adapter = new GaleryFullViewAdapter(images, getApplicationContext());
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        galeryFullView.setAdapter(adapter);
        galeryFullView.setLayoutManager(layoutManager);
        galeryFullView.setItemAnimator(itemAnimator);
//        scrollToPosition(position);
//        galeryFullView.scrollToPosition(position);
//        galeryFullView.post(new Runnable() {
//            @Override
//            public void run() {
//                View view = layoutManager.findViewByPosition(position);
//                Log.d("qwerty786876", (view==null) + "");
//                if (view == null) {
////                Log.e(WingPickerView.class.getSimpleName(), "Cant find target View for initial Snap");
//                    return;
//                }
//
//                int[] snapDistance = pagerSnapHelper.calculateDistanceToFinalSnap(layoutManager, view);
//                if (snapDistance[0] != 0 || snapDistance[1] != 0) {
//                    galeryFullView.scrollBy(snapDistance[0], snapDistance[1]);
//                }
//                Log.d("qwerty786876", snapDistance[0] + " " + snapDistance[1]);
//            }
//        });
//        galeryFullView.post(()->{
//            View view = layoutManager.findViewByPosition(position);
//            if (view == null) {
////                Log.e(WingPickerView.class.getSimpleName(), "Cant find target View for initial Snap");
//                return;
//            }
//
//            int[] snapDistance = pagerSnapHelper.calculateDistanceToFinalSnap(layoutManager, view);
//            if (snapDistance[0] != 0 || snapDistance[1] != 0) {
//                galeryFullView.scrollBy(snapDistance[0], snapDistance[1]);
//            }
//        });

        final LinearLayout buttonsLayout = (LinearLayout) findViewById(R.id.full_view_button_layout);
        galeryShapshotListener();

        Log.d("qwerty4557", position+"");

        final Handler uiHandler = new Handler();

        final Runnable makeLayoutGone = new Runnable(){
            @Override
            public void run(){
                buttonsLayout.setVisibility(View.GONE);
            }
        };

        // при нажатии на картинку появляется менюшка к ней. Там есть кнопки удаления и установки в качестве обложки
        adapter.setOnItemClickListener(new GaleryFullViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                if (editAccess){
                    buttonsLayout.setVisibility(View.VISIBLE);
                    position = pos;

                    // через 8 секунд меню пропадает
                    uiHandler.postDelayed(makeLayoutGone, 8000);
                }

            }
        });


        // кнопка удаления. При нажатии изображение удаляется
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDeleteOpen();

            }
        });


        coverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSetCoverOpen();

            }
        });
    }

    private void foo(LinearLayoutManager layoutManager, PagerSnapHelper pagerSnapHelper){
        View view = layoutManager.findViewByPosition(position);
        if (view == null) {
//                Log.e(WingPickerView.class.getSimpleName(), "Cant find target View for initial Snap");
            return;
        }

        int[] snapDistance = pagerSnapHelper.calculateDistanceToFinalSnap(layoutManager, view);
        if (snapDistance[0] != 0 || snapDistance[1] != 0) {
            galeryFullView.scrollBy(snapDistance[0], snapDistance[1]);
        }
    }

    private void scrollToPosition(final int position){
        galeryFullView.scrollToPosition(position);
        RecyclerView.ViewHolder viewHolder = galeryFullView.findViewHolderForLayoutPosition(position);
        if (viewHolder != null){
            galeryFullView.scrollToPosition(position);
        }
        else{
            galeryFullView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    galeryFullView.removeOnScrollListener(this);
                    if (dx == 0){
                        scrollToPosition(position);
                    }
                }
            });
            galeryFullView.scrollToPosition(position);
        }

    }

    @Override
    public void onDeleteClicked()
    {
        String toDel = ""+names.get(position);
        names.remove(position);
        images.remove(position);
        adapter.notifyDataSetChanged();
        imageStorage.child(toDel).delete();
        imagePathsDoc.update(toDel, FieldValue.delete());
        if (!changed)
        {
            changed=true;
            setResultChanged();
        }
    }
    @Override
    public  void onSetCover() {
        db.collection("Notes").document(user).collection("userNotes").document(id).
                update("imagePath", names.get(position));
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
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
        Intent intent = new Intent(GaleryFullViewActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public void onDelete()
    {
        DeleteUser.deleteUser(this, user);
        db.collection("PublicID").document(user).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable DocumentSnapshot documentSnapshot, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                if (documentSnapshot == null || documentSnapshot.getString("id")==null){
                    Toast.makeText(getApplicationContext(),"Аккаунт удалён",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(GaleryFullViewActivity.this, MainActivity.class);
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
    public void onForgot() {
        Intent intent = new Intent(GaleryFullViewActivity.this, ForgotPswActivity.class);
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

    public void updateImages(HashMap<String, Boolean> hashMap){
        for (String key : hashMap.keySet()){
            final Long l = Long.parseLong(key);
            if (names.contains(l) && hashMap.get(key) == true && images.get(names.indexOf(l)).getType()==0){
                imageStorage.child(key).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        images.set(names.indexOf(l), new ImageClass(uri));
                        adapter.notifyItemChanged(names.indexOf(l));
                    }
                });
            }
            else if (!names.contains(l)){
                int index0 = names.size();
                for (int i = 0; i < names.size(); i++){
                    if (names.get(i) > l){
                        index0 = i;
                        Log.d("qwerty3248632", names.get(i) + " " + l + " " + positionName);
                        break;
                    }
                }
                names.add(index0, l);
                images.add(index0, new ImageClass(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_image)));
                final int finalIndex0 = index0;
                adapter.notifyItemInserted(index0);
                galeryFullView.scrollToPosition(names.indexOf(positionName));
                Log.d("qwerty2312", names.size() + " hash " + hashMap.size());

                if (hashMap.get(key)){
                    imageStorage.child(key).getDownloadUrl().
                            addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
//                                    names.add(finalIndex0, l);
                                    images.set(names.indexOf(l), new ImageClass(uri));
                                    adapter.notifyItemChanged(names.indexOf(l));
                                }
                            });
                }
            }
        }
    }


    public void galeryShapshotListener(){
        imagePathsDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Log.d("qwerty31", "HI");
                if (e != null){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    final HashMap<String, Boolean> hashMap = (HashMap) documentSnapshot.getData();
                    if (hashMap != null){
                        Log.d("qwerty24324", "hi from galery " + "hashMap");
                        if (!names.contains(positionName) && hashMap.containsKey(positionName+"")){
                            Log.d("qwerty24324", "hi from galery " + "contains");
//                            long l = positionName;
                            int index=names.size();
                            for (int i = 0; i < names.size(); i++){
                                if (names.get(i) > positionName){
                                    index = i;
                                    break;
                                }
                            }
                            final int finalIndex = index;
                            if (hashMap.get(positionName+"")==false){
                                names.add(index, positionName);
                                images.add(index, new ImageClass(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_image)));
                                adapter.notifyItemInserted(index);
                                position = index;
                                galeryFullView.scrollToPosition(position);
                                updateImages(hashMap);
                            }
                            else{
                                imageStorage.child(positionName+"").getDownloadUrl().
                                        addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                names.add(finalIndex, positionName);
                                                images.add(finalIndex, new ImageClass(uri));
                                                adapter.notifyItemInserted(images.size()-1);
                                                position = finalIndex;
                                                galeryFullView.scrollToPosition(finalIndex);
                                                Log.d("qwerty24324", "hi from falery " + finalIndex);
                                                updateImages(hashMap);
                                            }
                                        });
                            }
                        }
                        else{
                            updateImages(hashMap);
                        }
//                        for (String key : hashMap.keySet()){
//                            final Long l = Long.parseLong(key);
//                            if (names.contains(l) && hashMap.get(key) == true && images.get(names.indexOf(l)).getType()==0){
//                                imageStorage.child(key).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                    @Override
//                                    public void onSuccess(Uri uri) {
//                                        images.set(names.indexOf(l), new ImageClass(uri));
//                                        adapter.notifyItemChanged(names.indexOf(l));
//                                    }
//                                });
//                            }
//                            else if (!names.contains(l)){
//                                int index0 = names.size();
//                                for (int i = 0; i < names.size(); i++){
//                                    if (names.get(i) > l){
//                                        index0 = i;
//                                        break;
//                                    }
//                                }
//                                final int finalIndex0 = index0;
//                                if (hashMap.get(key)==false){
//                                    names.add(index0, l);
//                                    images.add(index0, new ImageClass(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_image)));
//                                    adapter.notifyItemInserted(index0);
//                                }
//                                else{
//                                    imageStorage.child(key).getDownloadUrl().
//                                            addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                                @Override
//                                                public void onSuccess(Uri uri) {
//                                                        names.add(finalIndex0, l);
//                                                        images.add(finalIndex0, new ImageClass(uri));
//                                                        adapter.notifyItemInserted(finalIndex0);
//                                                }
//                                            });
//                                }
//                            }
//                        }
                    }

                }


            }
        });

    }

    private void dialogDeleteOpen(){
        DeleteDialogFragment dialog = new DeleteDialogFragment();
        dialog.show(getSupportFragmentManager(), "deleteDialog");
    }

    private void dialogSetCoverOpen(){
        SetCoverDialogFragment dialog = new SetCoverDialogFragment();
        dialog.show(getSupportFragmentManager(), "setCover");
    }

    private void setResultChanged(){
        // создание возвращаемого интента
        Log.d("DELETEIMAGE1", "resultChanged");
        Intent returnIntent = new Intent();
        returnIntent.putExtra("changed", changed);
        setResult(RESULT_OK, returnIntent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}

