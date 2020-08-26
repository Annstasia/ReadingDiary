package com.example.readingdiary.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readingdiary.Classes.DeleteFilesClass;
import com.example.readingdiary.Classes.DeleteUser;
import com.example.readingdiary.Classes.VariousNoteComparator;
import com.example.readingdiary.Classes.VariousNotes;
import com.example.readingdiary.Classes.VariousNotesAudio;
import com.example.readingdiary.Classes.VariousNotesInterface;
import com.example.readingdiary.Fragments.AddShortNameFragment;
import com.example.readingdiary.Fragments.SettingsDialogFragment;
import com.example.readingdiary.R;
import com.example.readingdiary.adapters.VariousViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariousShow extends AppCompatActivity implements SettingsDialogFragment.SettingsDialogListener
{
    // класс отвечает за активность с каталогами
    private String TAG_DARK = "dark_theme";
    SharedPreferences sharedPreferences;
    private String id;
    private String type;
    private String audioType;
    VariousViewAdapter viewAdapter;
    RecyclerView recyclerView;
    ArrayList<VariousNotesInterface> variousNotes;
    ArrayList<Long> variousNotesNames;

    private final int ADD_VIEW_RESULT_CODE = 666;
    private final int ADD_AUDIO_RESULT_CODE = 777;
    File fileDir1;
    MaterialToolbar toolbar;
    TextView counterText;
    int count=0;

    boolean action_mode=false;
    ArrayList<VariousNotes> selectedTextNotes = new ArrayList<>();
    ArrayList<VariousNotesAudio> selectedAudioNotes = new ArrayList<>();

    private DocumentReference variousNotePaths;
    private DocumentReference variousNoteAudioPaths;
    private CollectionReference variousNoteStorage;
    private StorageReference storageReference;

    String user;
    private String idUser;
    Button addVariousItem;
    boolean editAccess;
    MainActivity mein = new MainActivity();
    MediaPlayer activeMediaPlayer;
    VariousNotesAudio audioItem;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        sharedPreferences = this.getSharedPreferences(TAG_DARK, Context.MODE_PRIVATE);
        boolean dark = sharedPreferences.getBoolean(TAG_DARK, false);
        if (dark)
        {
            setTheme(R.style.DarkTheme);
        }
        else
        {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_various_show);

        Bundle args = getIntent().getExtras();
        id = args.get("id").toString();
        type = args.get("type").toString();
        audioType = type+"Audio";
        if (args.get("owner")!= null){
            user = args.get("owner").toString();
            editAccess = false;
        }
        else{
            user = FirebaseAuth.getInstance().getUid();
            editAccess = true;
        }

        variousNoteStorage = FirebaseFirestore.getInstance().collection("VariousNotes").document(user).collection(id);
        variousNotePaths = variousNoteStorage.document(type);
        variousNoteAudioPaths = variousNoteStorage.document(audioType);
        variousNotes = new ArrayList<>();
        variousNotesNames = new ArrayList<>();
        storageReference = FirebaseStorage.getInstance().getReference(user).child(id).child(type);

        openNotes(variousNotePaths);
        openNotes(variousNoteAudioPaths);
        findViews();
        toolbar.getMenu().clear();
        toolbar.setTitle("");
        if (type.equals(getResources().getString(R.string.commentDir))) counterText.setText("Отзыв");
        else if (type.equals(getResources().getString(R.string.descriptionDir))) counterText.setText("Описание");
        else if (type.equals(getResources().getString(R.string.quoteDir))) counterText.setText("Цитаты");

//        counterText.setText(type);
        setSupportActionBar(toolbar);

        setAdapters();
        setButtons();

//        user= idUser;// Для тестов, обязательно поменяй

        if (user==idUser)//проверка на автора
        {
            addVariousItem.setVisibility(View.INVISIBLE);
            //recyclerView.setClickable(false);
        }
    }

//    private int binarySearch

    @Override
    public void onChangeThemeClick(boolean isChecked) {
        Toast.makeText(this, "На нас напали светлые маги. Темная тема пока заперта", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onExitClick()
    {
        MainActivity MainActivity = new MainActivity();
        MainActivity.currentUser=null;
        MainActivity.mAuth.signOut();
        Intent intent = new Intent(VariousShow.this, MainActivity.class);
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
                    Intent intent = new Intent(VariousShow.this, MainActivity.class);
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
        Intent intent = new Intent(VariousShow.this, ForgotPswActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId()==R.id.item_settings)
        {
            int location[] = new int[2];
            toolbar.getLocationInWindow(location);
            int y = getResources().getDisplayMetrics().heightPixels;
            int x = getResources().getDisplayMetrics().widthPixels;

            SettingsDialogFragment settingsDialogFragment = new SettingsDialogFragment(y, x, sharedPreferences.getBoolean(TAG_DARK, false));
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            settingsDialogFragment.show(transaction, "dialog");
        }
        if (item.getItemId()== R.id.item_delete)
        {
            action_mode=false;
            viewAdapter.setActionMode(false);
            deleteVariousTextNotes();
            deleteVariousAudioNotes();
            viewAdapter.notifyDataSetChanged();
            toolbar.getMenu().clear();
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            if (type.equals(getResources().getString(R.string.commentDir))) counterText.setText("Отзыв");
            else if (type.equals(getResources().getString(R.string.descriptionDir))) counterText.setText("Описание");
            else if (type.equals(getResources().getString(R.string.quoteDir))) counterText.setText("Цитаты");
            count=0;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        try
        {
            if (requestCode == ADD_VIEW_RESULT_CODE && resultCode == RESULT_OK)
            {
                Log.d("qwerty151", "hi");
                Bundle args = data.getExtras();
                if (args.get("time") != null)
                {
//                    long time = Long.parseLong(args.get("time").toString());
//                    File file = new File(fileDir1, time+".txt");
//                    BufferedReader br = new BufferedReader(new FileReader(file));
//                    StringBuilder str = new StringBuilder();
//                    String line;
//                    while ((line = br.readLine()) != null){
//                        str.append(line);
//                        str.append('\n');
//                    }
//                    variousNotes.add(new VariousNotes(str.toString(), file.getAbsolutePath(), time, false));
//                    viewAdapter.notifyDataSetChanged();
                }
                else if (args.get("updatePath") != null)
                {
                    int position = Integer.parseInt(args.get("position").toString());
                    ((VariousNotes)variousNotes.get(position)).setNeedsUpdate(true);

                }

            }

            else if (requestCode == ADD_AUDIO_RESULT_CODE && resultCode == RESULT_OK && data != null){
                Log.d("qwerty151", "upload");
                uploadAudio(data.getData());
            }

            Log.d("qwerty151", (requestCode == ADD_AUDIO_RESULT_CODE) + " " + (resultCode == RESULT_OK) + " " + (data != null));
            if (data == null){
                Log.d("qwerty151", "null");
            }
        }
        catch (Exception e)
        {
            Log.e("resultShowException", e.toString());
        }

    }

    private void uploadAudio(final Uri audio){

        final long time = System.currentTimeMillis();
        Map<String, Boolean> map = new HashMap<>();
        map.put(time+"", false);
        variousNoteAudioPaths.set(map, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        storageReference.child(time+"").putFile(audio).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_LONG).show();
                                variousNoteAudioPaths.update(time+"", true);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("qwerty151", e.toString());
                            }
                        });

                    }
                });

    }

    private void deleteVariousTextNotes()
    {
        String[] deletePaths = new String[selectedTextNotes.size()];
        for (int i = 0; i < deletePaths.length; i++)
        {
            variousNotes.remove(selectedTextNotes.get(i));
            deletePaths[i] = selectedTextNotes.get(i).getPath();
            variousNotesNames.remove((Long)Long.parseLong(deletePaths[i]));
            variousNotePaths.update(deletePaths[i], FieldValue.delete());
        }
        selectedTextNotes.clear();
        WriteBatch writeBatch = FirebaseFirestore.getInstance().batch();
        for (int i = 0; i < deletePaths.length; i++)
        {
            writeBatch.delete(variousNoteStorage.document(deletePaths[i]));
        }
        writeBatch.commit();
    }

    private void deleteVariousAudioNotes(){
        Long[] deletePaths = new Long[selectedAudioNotes.size()];
        for (int i = 0; i< deletePaths.length; i++){
            variousNotes.remove(selectedAudioNotes.get(i));
            deletePaths[i] = selectedAudioNotes.get(i).getTime();
            variousNotesNames.remove(deletePaths[i]);
            variousNoteAudioPaths.update(deletePaths[i]+"", FieldValue.delete());
        }
        selectedAudioNotes.clear();
        for (int i = 0; i < deletePaths.length; i++){
            storageReference.child(deletePaths[i]+"").delete();
        }
    }

    private void findViews()
    {
        recyclerView = (RecyclerView) findViewById(R.id.various_recycler_view);
        toolbar = (MaterialToolbar) findViewById(R.id.long_click_toolbar);
        counterText = (TextView) findViewById(R.id.counter_text);
    }

    private void setAdapters()
    {
        viewAdapter = new VariousViewAdapter(variousNotes);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);
        recyclerView.setAdapter(viewAdapter);
        if (editAccess)
        {
            viewAdapter.setOnItemClickListener(new VariousViewAdapter.OnItemClickListener()
            {

                @Override
                public void onItemClick(int position)
                {
                    if (variousNotes.get(position).getItemType()==0){
                        Intent intent = new Intent(VariousShow.this, VariousNotebook.class);
                        intent.putExtra("id", id);
                        intent.putExtra("type", type);
                        intent.putExtra("path", ((VariousNotes)variousNotes.get(position)).getPath());
                        intent.putExtra("position", position + "");
                        startActivityForResult(intent, ADD_VIEW_RESULT_CODE);
                    }

                }

                @Override
                public void onItemLongClick(int position)
                {
                    viewAdapter.setActionMode(true);
                    action_mode = true;
                    counterText.setText(count + " элементов выбрано");
                    toolbar.getMenu().clear();
                    toolbar.inflateMenu(R.menu.menu_long_click);
                    viewAdapter.notifyDataSetChanged();
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }

                @Override
                public void onCheckClick(int position)
                {
                    if (variousNotes.get(position).getItemType()==0){
                        selectedTextNotes.add((VariousNotes)variousNotes.get(position));
                    }
                    else{
                        selectedAudioNotes.add((VariousNotesAudio)variousNotes.get(position));
                    }
                    count++;
                    counterText.setText(count + " элементов выбрано");
                    // Toast.makeText(getApplicationContext(), selectedNotes.size() + " items selected", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onUncheckClick(int position)
                {
                    if (variousNotes.get(position).getItemType()==0){
                        selectedTextNotes.remove((VariousNotes)variousNotes.get(position));
                    }
                    else{
                        selectedAudioNotes.remove((VariousNotesAudio)variousNotes.get(position));
                    }
                    count--;
                    counterText.setText(count + " элементов выбрано");
                }

                @Override
                public void onPlayButtonPressed(final int position) {
                    if (variousNotes.get(position).getItemType() == 1) {
                        if (audioItem != null && audioItem != (VariousNotesAudio) variousNotes.get(position)) {
                            audioItem.setPlaying(false);
                        }
                        if (audioItem != (VariousNotesAudio) variousNotes.get(position)) {
                            audioItem = (VariousNotesAudio) variousNotes.get(position);
                            audioItem.changePlaying();
                            viewAdapter.notifyDataSetChanged();
                            if (audioItem.isPlaying()) {
                                try {
                                    if (activeMediaPlayer == null) {
                                        activeMediaPlayer = new MediaPlayer();
                                    }
                                    activeMediaPlayer.reset();
                                    activeMediaPlayer.setDataSource(audioItem.getUri().toString());
                                    activeMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                        @Override
                                        public void onPrepared(MediaPlayer mp) {
                                            mp.start();
                                        }
                                    });
                                    activeMediaPlayer.prepare();
                                    activeMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mp) {
                                            audioItem.changePlaying();
                                            viewAdapter.notifyItemChanged(position);
                                        }
                                    });
                                } catch (IOException e) {
                                    Log.e("onPlayButtonIOexception", e.toString());
                                }
                            }
                        }
                        else {
                            audioItem.changePlaying();
                            viewAdapter.notifyItemChanged(position);
                            if (audioItem.isPlaying()) {
                                activeMediaPlayer.start();
                            } else {
                                activeMediaPlayer.pause();
                            }
                        }
                    }
                }
            });
        }
    }

    private void setButtons()
    {
        addVariousItem = (Button) findViewById(R.id.addVariousItem);
        if(!editAccess){
            addVariousItem.setVisibility(View.GONE);
        }
        else{
            addVariousItem.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(VariousShow.this, VariousNotebook.class);
                    intent.putExtra("id", id);
                    intent.putExtra("type", type);
                    startActivityForResult(intent, ADD_VIEW_RESULT_CODE);
                }
            });
        }

        FloatingActionButton addAudioButton = (FloatingActionButton) findViewById(R.id.addAudioButton);
        addAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent audioPickIntent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                startActivityForResult(audioPickIntent, ADD_AUDIO_RESULT_CODE);
            }
        });
//         addVariousItem = (Button) findViewById(R.id.addVariousItem);

    }


    public int binarySearch(ArrayList<Long> arrayList, long key){
        int left = -1, right = arrayList.size(), middle;
        while (left + 1 < right){
            middle = (left + right) / 2;
            if (key < arrayList.get(middle)){
                left = middle;
            }
            else{
                right = middle;
            }
        }
        return right;
    }

    private void openNotes(final DocumentReference variousPath)
    {
        try
        {
            int count;
            variousPath.addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Log.e("VariousShowOpenNotes", e.toString());
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    else
                    {
                        HashMap<String, Boolean> hashMap = (HashMap) documentSnapshot.getData();
                        if (hashMap != null)
                        {
//                            count = hashMap.size();
                            for (final String key : hashMap.keySet())
                            {

                                final Long l = Long.parseLong(key);
                                if (!variousNotesNames.contains(l) && hashMap.get(key) == true)
                                {
                                    if (variousPath == variousNotePaths){
                                        variousNoteStorage.document(key).get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                                                {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        if (documentSnapshot != null && documentSnapshot.get("text") != null)
                                                        {
                                                            int putIndex = binarySearch(variousNotesNames, l);
                                                            variousNotes.add(putIndex, new VariousNotes(documentSnapshot.get("text").toString(), l + "",
                                                                    l, false, false));
                                                            variousNotesNames.add(putIndex, l);
//                                                            variousNotes.add(new VariousNotes(documentSnapshot.get("text").toString(), l + "",
//                                                                    l, false, false));
                                                            viewAdapter.notifyItemInserted(putIndex);
//                                                            variousNotesNames.add(l);
//                                                            if (count==null){
//                                                                count=0;
//                                                            }
//                                                            count--;
//                                                            if (count==0){
//                                                                Collections.sort(variousNotes, new VariousNoteComparator());
//                                                                Collections.sort(variousNotesNames);
////                                                                variousNotes.sort();
//                                                            }
                                                        }
                                                    }

                                                });
                                    }
                                    else if (variousPath == variousNoteAudioPaths){
                                        storageReference.child(key).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                if (uri != null){
                                                    int putIndex = binarySearch(variousNotesNames, l);
                                                    variousNotes.add(putIndex, new VariousNotesAudio(l, uri));
                                                    variousNotesNames.add(putIndex, l);
                                                    viewAdapter.notifyItemInserted(putIndex);

//                                                    if (count==null){
//                                                        count=0;
//                                                    }
//                                                    count--;
//                                                    if (count==0){
//                                                        Collections.sort(variousNotes, new VariousNoteComparator());
//                                                        Collections.sort(variousNotesNames);
////                                                                variousNotes.sort();
//                                                    }
                                                }
                                            }
                                        });
                                    }

                                }
                                else if (variousPath == variousNotePaths && hashMap.get(key) == true && variousNotesNames.contains(l) &&
                                        ((VariousNotes)variousNotes.get(variousNotesNames.indexOf(l))).isNeedsUpdate())
                                {
                                    variousNoteStorage.document(key).get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                                            {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot)
                                                {
                                                    variousNotes.set(variousNotesNames.indexOf(l), new VariousNotes(documentSnapshot.get("text").toString(), l + "",
                                                            l, false, false));
                                                    viewAdapter.notifyItemChanged(variousNotesNames.indexOf(l));
                                                   //variousNotesNames.add(l);
                                                }
                                            });
                                }

                            }
                        }

                    }
                }
            });

        }
        catch (Exception e)
        {
            Log.e("openShowException", e.toString());
        }

    }

}
