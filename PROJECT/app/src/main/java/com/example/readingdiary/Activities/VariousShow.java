package com.example.readingdiary.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.readingdiary.Classes.VariousNotes;
import com.example.readingdiary.Fragments.AddShortNameFragment;
import com.example.readingdiary.Fragments.SettingsDialogFragment;
import com.example.readingdiary.R;
import com.example.readingdiary.adapters.VariousViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.WriteBatch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class VariousShow extends AppCompatActivity implements SettingsDialogFragment.SettingsDialogListener
{
    // класс отвечает за активность с каталогами
    private String TAG_DARK = "dark_theme";
    SharedPreferences sharedPreferences;
    private String id;
    private String type;
    VariousViewAdapter viewAdapter;
    RecyclerView recyclerView;
    ArrayList<VariousNotes> variousNotes;
    ArrayList<Long> variousNotesNames;

    private final int ADD_VIEW_RESULT_CODE = 666;
    File fileDir1;
    MaterialToolbar toolbar;
    TextView counterText;
    int count=0;

    boolean action_mode=false;
    ArrayList<VariousNotes> selectedNotes = new ArrayList<>();

    private DocumentReference variousNotePaths;
    private CollectionReference variousNoteStorage;

    String user;
    private String idUser;
    Button addVariousItem;
    boolean editAccess;
    MainActivity mein = new MainActivity();

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
        variousNotes = new ArrayList<>();
        variousNotesNames = new ArrayList<>();

        openNotes();
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
            deleteVariousNotes();
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
                    variousNotes.get(position).setNeedsUpdate(true);

                }

            }
        }
        catch (Exception e)
        {
            Log.e("resultShowException", e.toString());
        }

    }

    private void deleteVariousNotes()
    {
        String[] deletePaths = new String[selectedNotes.size()];
        for (int i = 0; i < deletePaths.length; i++)
        {
            variousNotes.remove(selectedNotes.get(i));
            deletePaths[i] = selectedNotes.get(i).getPath();
            variousNotesNames.remove((Long)Long.parseLong(deletePaths[i]));
            variousNotePaths.update(deletePaths[i], FieldValue.delete());
        }
        selectedNotes.clear();
        WriteBatch writeBatch = FirebaseFirestore.getInstance().batch();
        for (int i = 0; i < deletePaths.length; i++)
        {
            writeBatch.delete(variousNoteStorage.document(deletePaths[i]));
//            variousNotes.remove(selectedNotes.get(i));
//            deleteArr[i] = new File(selectedNotes.get(i).getPath());
        }
        writeBatch.commit();

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

                    Intent intent = new Intent(VariousShow.this, VariousNotebook.class);
                    intent.putExtra("id", id);
                    intent.putExtra("type", type);
                    intent.putExtra("path", variousNotes.get(position).getPath());
                    intent.putExtra("position", position + "");
                    startActivityForResult(intent, ADD_VIEW_RESULT_CODE);
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
                    selectedNotes.add(variousNotes.get(position));
                    count++;
                    counterText.setText(count + " элементов выбрано");
                    // Toast.makeText(getApplicationContext(), selectedNotes.size() + " items selected", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onUncheckClick(int position)
                {
                    selectedNotes.remove(variousNotes.get(position));
                    count--;
                    counterText.setText(count + " элементов выбрано");

                    // Toast.makeText(getApplicationContext(), selectedNotes.size() + " items selected", Toast.LENGTH_LONG).show();

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
//         addVariousItem = (Button) findViewById(R.id.addVariousItem);

    }


    private void openNotes()
    {
        try
        {
            variousNotePaths.addSnapshotListener(new EventListener<DocumentSnapshot>()
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

                            for (String key : hashMap.keySet())
                            {
                                final Long l = Long.parseLong(key);
                                if (!variousNotesNames.contains(l) && hashMap.get(key) == true)
                                {
                                    variousNoteStorage.document(key).get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                                            {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot != null && documentSnapshot.get("text") != null)
                                                    {
                                                        variousNotes.add(new VariousNotes(documentSnapshot.get("text").toString(), l + "",
                                                                l, false, false));
                                                        viewAdapter.notifyItemInserted(variousNotes.size());
                                                        variousNotesNames.add(l);
                                                    }

                                                }
                                            });
                                }
                                else if (hashMap.get(key) == true && variousNotesNames.contains(l) &&
                                        variousNotes.get(variousNotesNames.indexOf(l)).isNeedsUpdate())
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
