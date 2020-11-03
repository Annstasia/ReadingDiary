package com.example.readingdiary.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readingdiary.Classes.VariousNotes;
import com.example.readingdiary.Classes.VariousNotesAudio;
import com.example.readingdiary.Classes.VariousNotesInterface;
import com.example.readingdiary.R;
import com.example.readingdiary.adapters.VariousViewAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VariousShow extends AppCompatActivity
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
    private TextView textCurrentTime, textTotalDuration;
    private SeekBar playerSeekBar;
    private Handler handler = new Handler();
    private FloatingActionButton playAudioButton;
    private String title;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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

        if (type.equals(getResources().getString(R.string.commentDir))) title = "Отзыв";
        else if (type.equals(getResources().getString(R.string.descriptionDir))) title = "Описание";
        else if (type.equals(getResources().getString(R.string.quoteDir))) title = "Цитаты";
        counterText.setText(title);

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
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {

        if (item.getItemId()== R.id.item_delete)
        {
            action_mode=false;
            viewAdapter.setActionMode(false);
            deleteVariousTextNotes();
            deleteVariousAudioNotes();
//            viewAdapter.notifyDataSetChanged();
            toolbar.getMenu().clear();
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            if (type.equals(getResources().getString(R.string.commentDir))) counterText.setText("Отзыв");
            else if (type.equals(getResources().getString(R.string.descriptionDir))) counterText.setText("Описание");
            else if (type.equals(getResources().getString(R.string.quoteDir))) counterText.setText("Цитаты");
            count=0;
        }

        if (item.getItemId() == android.R.id.home){
            action_mode=false;
            viewAdapter.setActionMode(false);
            viewAdapter.notifyDataSetChanged();
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.base_menu);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//            menuType = 0;
            counterText.setText(title);
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
            int pos = variousNotes.indexOf(selectedTextNotes.get(i));
            variousNotes.remove(pos);
            viewAdapter.notifyItemRemoved(pos);
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
            int pos = variousNotes.indexOf(selectedAudioNotes.get(i));
            variousNotes.remove(pos);
            viewAdapter.notifyItemRemoved(pos);
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
        final RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);
        recyclerView.setAdapter(viewAdapter);
        if (editAccess)
        {
            viewAdapter.setOnItemClickListener(new VariousViewAdapter.OnItemClickListener() {

                       @Override
                       public void onItemClick(int position) {
                           if (variousNotes.get(position).getItemType() == 0) {
                               Intent intent = new Intent(VariousShow.this, VariousNotebook.class);
                               intent.putExtra("id", id);
                               intent.putExtra("type", type);
                               intent.putExtra("path", ((VariousNotes) variousNotes.get(position)).getPath());
                               intent.putExtra("position", position + "");
                               startActivityForResult(intent, ADD_VIEW_RESULT_CODE);
                           }

                       }

                       @Override
                       public void onItemLongClick(int position) {
                           viewAdapter.setActionMode(true);
                           action_mode = true;
                           counterText.setText(count + " элементов выбрано");
                           toolbar.getMenu().clear();
                           toolbar.inflateMenu(R.menu.menu_long_click);
                           viewAdapter.notifyDataSetChanged();
                           getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                       }

                       @Override
                       public void onCheckClick(int position) {
                           if (variousNotes.get(position).getItemType() == 0) {
                               selectedTextNotes.add((VariousNotes) variousNotes.get(position));
                           } else {
                               selectedAudioNotes.add((VariousNotesAudio) variousNotes.get(position));
                           }
                           count++;
                           counterText.setText(count + " элементов выбрано");
                           // Toast.makeText(getApplicationContext(), selectedNotes.size() + " items selected", Toast.LENGTH_LONG).show();
                       }

                       @Override
                       public void onUncheckClick(int position) {
                           if (variousNotes.get(position).getItemType() == 0) {
                               selectedTextNotes.remove((VariousNotes) variousNotes.get(position));
                           } else {
                               selectedAudioNotes.remove((VariousNotesAudio) variousNotes.get(position));
                           }
                           count--;
                           counterText.setText(count + " элементов выбрано");
                       }

                @Override
                public void onPlayButtonPressed(final int position, View itemView) {
                    Log.d("qwerty169", "hi");
                    if (variousNotes.get(position).getItemType() == 1) {
                        Log.d("qwertyAudio", (audioItem == null) + " ");
                        if (audioItem != null){
                            Log.d("qwertyAudio", (audioItem == null) + " " + (audioItem == (VariousNotesAudio) variousNotes.get(position)));
                        }
                        // Останавливаем предыдущую запись, если та играет
                        if (audioItem != null && audioItem != (VariousNotesAudio) variousNotes.get(position)) {
                            audioItem.setPlaying(false);
                            activeMediaPlayer.reset();
                            playAudioButton.setImageResource(R.drawable.ic_action_play_light);
                            playerSeekBar.setProgress(0);
                            playerSeekBar.setEnabled(false);
                            textCurrentTime.setText("0:00");
                            textTotalDuration.setText("0:00");
                        }
                        // Если началась новая запись
                        if (audioItem == null || audioItem != (VariousNotesAudio) variousNotes.get(position)) {
                            Log.d("qwertyAudio", "hello");
                            audioItem = (VariousNotesAudio) variousNotes.get(position);
                            audioItem.changePlaying();
                            playAudioButton = itemView.findViewById(R.id.playAudioButton);
                            textCurrentTime = itemView.findViewById(R.id.textCurrentTime);
                            textTotalDuration = itemView.findViewById(R.id.textTotalDuration);
                            playerSeekBar = itemView.findViewById(R.id.musicSeekBar);
                            playerSeekBar.setMax(100);
                            playerSeekBar.setProgress(0);
                            playerSeekBar.setEnabled(true);
                            if (activeMediaPlayer == null) {
                                activeMediaPlayer = new MediaPlayer();
                            }
                            playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    if (fromUser){
                                        int playPosition = (activeMediaPlayer.getDuration() / 100) * progress;
                                        activeMediaPlayer.seekTo(playPosition);
                                        textCurrentTime.setText(milliSecondsToTimer(activeMediaPlayer.getCurrentPosition()));
                                    }

                                }

                                @Override

                                public void onStartTrackingTouch(SeekBar seekBar) {

                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {

                                }
                            });
                            activeMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                                @Override
                                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                                    playerSeekBar.setSecondaryProgress(percent);
                                }
                            });
                            activeMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    playerSeekBar.setProgress(0);
                                    playerSeekBar.setEnabled(false);
                                    playAudioButton.setImageResource(R.drawable.ic_action_play_light);
                                    textCurrentTime.setText("0:00");
                                    textTotalDuration.setText("0:00");
                                    Log.d("qwertyAudio", "complite");
                                    if (audioItem != null){
                                        audioItem.changePlaying();
                                        audioItem = null;
                                    }

                                    mp.reset();
                                }
                            });
                            if (audioItem.isPlaying()) {
                                    if (activeMediaPlayer == null) {
                                        activeMediaPlayer = new MediaPlayer();
                                    }
                                    playAudioButton.setImageResource(R.drawable.ic_action_pause_light);
                                    prepareMediaPlayer(audioItem.getUri().toString());
                                    activeMediaPlayer.start();
                                    updateSeekBar();
                            }
                        }
                        else if (audioItem != null && audioItem == (VariousNotesAudio) variousNotes.get(position)) {
                            audioItem.changePlaying();
                            if (audioItem.isPlaying()) {
                                playAudioButton.setImageResource(R.drawable.ic_action_pause_light);
                                activeMediaPlayer.start();
                                updateSeekBar();
                            } else {
                                handler.removeCallbacks(updater);
                                playAudioButton.setImageResource(R.drawable.ic_action_play_light);
                                activeMediaPlayer.pause();
                            }
                        }
                    }
                }
            });
        }
    }

    private void prepareMediaPlayer(String uri){
        try{
            activeMediaPlayer.setDataSource(uri);
            activeMediaPlayer.prepare();
            textTotalDuration.setText(milliSecondsToTimer(activeMediaPlayer.getDuration()));
        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
            long currentDuration = activeMediaPlayer.getCurrentPosition();
            textCurrentTime.setText(milliSecondsToTimer(currentDuration));
        }
    };

    private void updateSeekBar(){
        if (activeMediaPlayer.isPlaying()){
            playerSeekBar.setProgress((int) (((float) activeMediaPlayer.getCurrentPosition() / activeMediaPlayer.getDuration()) * 100));
            handler.postDelayed(updater, 1000);
        }
    }

    private String milliSecondsToTimer(long milliSeconds){
        String timerString = "";
        String secondsString;
        int hours = (int)(milliSeconds / (1000 * 60 * 60));
        int minuts = (int)((milliSeconds % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) ((milliSeconds % (1000 * 60)) / 1000);
        if (hours > 0){
            timerString = hours + ":";
        }
        secondsString = seconds+"";
        if (seconds < 10){
            secondsString = "0" + seconds;
        }
        timerString = timerString + minuts + ":" + secondsString;
        return timerString;
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

    @Override
    protected void onDestroy() {
        if (activeMediaPlayer != null){
            if (activeMediaPlayer.isPlaying()){
                handler.removeCallbacks(updater);
            }
            activeMediaPlayer.reset();
            activeMediaPlayer = null;
        }
        super.onDestroy();
    }
}
