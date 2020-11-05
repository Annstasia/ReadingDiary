package com.example.readingdiary.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readingdiary.Classes.Directory;
import com.example.readingdiary.Classes.Note;
import com.example.readingdiary.Classes.RealNote;
import com.example.readingdiary.Fragments.CatalogFragment;
import com.example.readingdiary.Fragments.FilterDialogFragment;
import com.example.readingdiary.Fragments.SortDialogFragment;
import com.example.readingdiary.R;
import com.example.readingdiary.adapters.CatalogButtonAdapter;
import com.example.readingdiary.adapters.RecyclerViewAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class CatalogActivity extends AppCompatActivity implements SortDialogFragment.SortDialogListener, FilterDialogFragment.FilterDialogListener, CatalogFragment.OnCatalogFragmentListener {
    public DrawerLayout drawerLayout;
    Fragment fragment;
    MaterialToolbar toolbar;
    TextView counterText;
    public boolean action_mode = false;
    int count=0;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String user = "user0";
    int active=0;
    String userID;
    LinearLayoutManager layoutManager;
    private AppBarConfiguration mAppBarConfiguration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        // Кнопка добавление новой активности
        Toolbar toolbar = findViewById(R.id.toolbar_navigation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_catalog,
                R.id.nav_genres, R.id.nav_change_password, R.id.nav_log_out, R.id.nav_delete_account)
                .setDrawerLayout(drawerLayout)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);

        View headerView = navigationView.getHeaderView(0);
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            ((TextView)headerView.findViewById(R.id.textView)).setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        }


    }




        @Override
    public void changeFragment(Fragment fragment) {
        this.fragment = fragment;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_catalog, menu);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (fragment.toString().equals("catalog")){
            if (item.getItemId()== R.id.item_delete){
                ((CatalogFragment)fragment).deleteClick();
            }
            if (item.getItemId() == R.id.item_search){
                ((CatalogFragment)fragment).searchCLick1();

            }
            if (item.getItemId() == R.id.item_search1){
                ((CatalogFragment)fragment).searchClick2();
            }

            if (item.getItemId() == R.id.item_sort){
                ((CatalogFragment)fragment).sortClick();
            }
//        }

            if (item.getItemId()==R.id.item_filter){
                ((CatalogFragment)fragment).filterClick();
//                db.collection("genres").document(user).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if (documentSnapshot != null && documentSnapshot.getData() != null){
//                            TreeSet<String> genres =  new TreeSet<>();
//                            genres.addAll(documentSnapshot.getData().keySet());
//                            TreeSet<String> authors = new TreeSet<>();
//                            for (Note note: notes) {
//                                if (note.getItemType()==0){
//                                    authors.add(((RealNote) note).getAuthor());
////                    genres.add(((RealNote) note).getGenre());
//                                }
//                            }
//                            if (noFilter){
//                                checkedAuthors = new ArrayList<>(authors);
//                                checkedGenres = new ArrayList<>(genres);
//                            }
////                        onCatalogFragmentListener.filterClick(authors, genres, checkedAuthors, checkedGenres);
////                        FilterDialogFragment filterDialogFragment = new FilterDialogFragment(authors, genres, checkedAuthors, checkedGenres);
////                        FragmentManager manager = getSupportFragmentManager();
////                        FragmentTransaction transaction = manager.beginTransaction();
////                        filterDialogFragment.show(transaction, "dialog");
//                        }
//                    }
//                });


            }

        }

//        if (item.getItemId() == android.R.id.home){
//            if (menuType==0){
////                finish();
//            }
//            else if (menuType == 1){
//                action_mode=false;
//                mAdapter.setActionMode(false);
//                mAdapter.notifyDataSetChanged();
//                toolbar.getMenu().clear();
//                toolbar.inflateMenu(R.menu.menu_catalog);
////                toolbar.inflateMenu(R.menu.base_menu);
//                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//                menuType = 0;
//                counterText.setText("Каталог");
//                count=0;
//            }
//            else if (menuType==2){
//                findText1.clearComposingText();
//                counterText.setVisibility(View.VISIBLE);
//                findText1.setVisibility(View.GONE);
//                toolbar.getMenu().clear();
//                toolbar.inflateMenu(R.menu.menu_catalog);
////                toolbar.inflateMenu(R.menu.base_menu);
//                menuType = 0;
//            }
//        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSortClick(int position) {
        if (fragment.toString() == "catalog"){
            ((CatalogFragment)fragment).onSortClick(position);
        }

        Log.d("qwerty878", "sort");
//        sortType = position;
//        Log.d("strangeSort", choices[position]);
//        startSort();
    }

//    public void deleteSelectedRealNote(){
//        for (int i = 0; i < selectionRealNotesList.size(); i++){
//            String id = selectionRealNotesList.get(i).getID();
//            if (!((RealNote)selectionRealNotesList.get(i)).getPrivate()){
//                DeleteNote.deletePublicly(user, id);
//            }
//            notes.remove(selectionRealNotesList.get(i));
//            mAdapter.notifyItemRemoved(i);
//            DeleteNote.deleteNote(user, id);
//
//        }
//        mAdapter.notifyDataSetChanged();
//    }

    @Override
    public void onFilterClick(ArrayList<String> checkedAuthors, ArrayList<String> checkedGenres, ArrayList<String> checkedGenresID, String ratingStart, String ratingEnd, boolean showCatalog) {
        if (fragment.toString() == "catalog"){
            ((CatalogFragment)fragment).onFilterClick(checkedAuthors, checkedGenres, checkedGenresID, ratingStart, ratingEnd, showCatalog);
        }
//        noFilter = false;
//        this.checkedAuthors=checkedAuthors;
//        this.checkedGenres=checkedGenres;
//        for (int i = 0; i < notes.size(); i++){
//            if (notes.get(i).getItemType() != 0){
//                break;
//            }
//            if (!checkedAuthors.contains(((RealNote)notes.get(i)).getAuthor())){
//                notes.get(i).setVisibility(false);
//                continue;
//            }
//            boolean visible = false;
//            for (String j : checkedGenres){
//                if ((boolean)((RealNote)notes.get(i)).getGenre().get(j)){
//                    visible=true;
//                    break;
//                }
//            }
//            if (visible){
//                notes.get(i).setVisibility(true);
//            }
//            else{
//                notes.get(i).setVisibility(false);
//            }
//        }
//        mAdapter.notifyDataSetChanged();
    }

//    public void deleteSelectedDirectories(){
//            for (Directory directory : selectionDirectoriesList){
//                notes.remove(directory);
//                deleteDirectory(directory.getDirectory().replace("/", "\\"));
//                String s = directory.getDirectory();
//                String parDoc = s.substring(0, s.substring(0, s.length() - 1).lastIndexOf("/")+1).replace("/", "\\");
//                db.collection("User").document(user).collection("paths").document(parDoc).update("paths", FieldValue.arrayRemove(s.replace("/", "\\")));
//            }
//            selectionDirectoriesList.clear();
//        }

//    public void deleteDirectory(String path){
//        final String path1 = path;
//        final File dir0 = new File(path);
//        db.collection("User").document(user).collection("paths").whereEqualTo("parent", path1).get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (queryDocumentSnapshots != null){
//                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                                deleteDirectory(documentSnapshot.getId());
//                            }
//                        }
//                    }
//                });
//        db.collection("User").document(user).collection("paths").document(path1).delete();
//
//        db.collection("Notes").document(user).collection("userNotes").whereEqualTo("path", path1).get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (queryDocumentSnapshots != null){
//                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                                DeleteNote.deleteNote(user, documentSnapshot.getId());
//                                if (!(boolean)documentSnapshot.get("private")){
//                                    DeleteNote.deletePublicly(user, documentSnapshot.getId());
//                                }
//                            }
//                        }
//
//                    }
//                });
//    }


//    private void selectAll() {
//        final String par1 = parent.replace("/", "\\");
//        final String par2 = parent;
//        final long old_active = active;
//        db.collection("User").document(user).collection("paths").document(par1).get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if (documentSnapshot != null){
//                            ArrayList<String> list = (ArrayList<String>) documentSnapshot.get("paths");
//                            if (list != null) {
//                                for (String i : list) {
//                                    if (active!=old_active){
//                                        break;
//                                    }
//                                    if (!par1.equals(parent.replace("/", "\\"))){
//                                        break;
//                                    }
//                                    notes.add(new Directory(i, i.replace("\\", "/")));
//                                }
//                            }
//                        }
//                        startPos = notes.size();
//                        mAdapter.notifyDataSetChanged();
//                        db.collection("Notes").document(user).collection("userNotes").whereEqualTo("path", par1).get()
//                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                        if (queryDocumentSnapshots != null){
//                                            for (final QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                                                if (active!=old_active){
//                                                    break;
//                                                }
//                                                  final HashMap<String, Object> map = (HashMap<String, Object>) documentSnapshot.getData();
//                                                generateNote(documentSnapshot.getId(), -1);
//                                            }
//                                        }
//                                        mAdapter.notifyDataSetChanged();
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Log.e("qwerty9", e.toString());
//                                    }
//                                });
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e("qwerty10", e.toString());
//                    }
//                });
//    }

//        private void selectTitle(String title){
//            db.collection("Notes").document(user).collection("userNotes").whereEqualTo("title", title).get()
//                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                        @Override
//                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                            if (queryDocumentSnapshots != null){
//                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                                    generateNote(documentSnapshot.getId(), -1);
//                                }
//                            }
//                            mAdapter.notifyDataSetChanged();
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.e("qwerty9", e.toString());
//                        }
//                    });
//        }

//        private void changeById(final String id){
//            for (int j = startPos; j < notes.size(); j++){
//                final int i = j;
//                if (notes.get(i).getID().equals(id)){
//                    generateNote(notes.get(i).getID(), i);
//                    break;
//                }
//            }
//        }

//        private void insertById(final String id){
//            generateNote(id, -1);
//        }


//        public void generateNote(final String id, final int index){
//        final int active0 = active;
//            db.collection("Notes").document(user).collection("userNotes").document(id).get()
//                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                        @Override
//                        public void onSuccess(final DocumentSnapshot documentSnapshot0) {
//                            final HashMap<String, Object> map = (HashMap) documentSnapshot0.getData();
//                            if (map==null) return;
//                            TreeMap<String, Object> genreMap = new TreeMap<>();
//                            genreMap.putAll((Map)map.get("genre"));
//                            final RealNote realNote = new RealNote(id, map.get("path").toString(), map.get("author").toString(),
//                                    map.get("title").toString(), Double.valueOf(map.get("rating").toString()), (boolean)map.get("private"),
//                                    (double) map.get("publicRatingSum"), (long)map.get("publicRatingCount"), genreMap);
//                            realNote.setTime(Long.parseLong(map.get("timeAdd").toString()));
//                            final int index1;
//                            if (active0 == active){
//                                if (index == -1) {
//                                    notes.add(realNote);
//                                    mAdapter.notifyItemInserted(notes.size() - 1);
//                                    index1 = notes.size()-1;
//
//                                } else {
//                                    notes.set(index, realNote);
//                                    mAdapter.notifyItemChanged(index);
//                                    index1 = index;
//                                }
//                            }
//                            else{
//                                index1=0;
//                                return;
//                            }
//
//                            if (map.get("imagePath")!= null && !map.get("imagePath").toString().equals("")){
//                                db.collection("Common").document(user).collection(documentSnapshot0.getId()).document("Images").addSnapshotListener(CatalogActivity.this, new EventListener<DocumentSnapshot>() {
//                                            @Override
//                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
////                                                Log.d("qwerty72", (boolean)documentSnapshot.get(map.get("imagePath").toString()) + "");
//                                                if (documentSnapshot.get(map.get("imagePath").toString()) != null && (boolean)documentSnapshot.get(map.get("imagePath").toString())==true){
//                                                    Log.d("qwerty72", "imagePathTrue");
//                                                    Log.d("qwerty72", map.get("imagePath").toString());
//                                                    FirebaseStorage.getInstance().getReference(user).child(documentSnapshot0.getId()).child("Images").child(map.get("imagePath").toString()).getDownloadUrl()
//                                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                                                @Override
//                                                                public void onSuccess(Uri uri) {
//                                                                    Log.d("qwerty72", "success");
//                                                                    if (active0 == active){
//                                                                        realNote.setCoverPath(uri);
//                                                                        ((RealNote)notes.get(index1)).setCoverPath(uri);
//                                                                        mAdapter.notifyItemChanged(index1);
//                                                                    }
//
//
////                                                                    if (index == -1) {
////                                                                        notes.add(realNote);
////                                                                        mAdapter.notifyItemInserted(notes.size() - 1);
////                                                                    } else {
////                                                                        notes.set(index, realNote);
////                                                                        mAdapter.notifyItemChanged(index);
////                                                                    }
//                                                                }
//                                                            })
//                                                            .addOnFailureListener(new OnFailureListener() {
//                                                                @Override
//                                                                public void onFailure(@NonNull Exception e) {
//                                                                    Log.d("qwerty72", e.toString());
//                                                                    if (active0 == active){
//                                                                        if (index == -1) {
//                                                                            notes.add(realNote);
//                                                                            mAdapter.notifyItemInserted(notes.size() - 1);
//                                                                        } else {
//                                                                            notes.set(index, realNote);
//                                                                            mAdapter.notifyItemChanged(index);
//                                                                        }
//                                                                    }
//
//                                                                }
//                                                            });
//                                                    }
////                                                else if (documentSnapshot.get(map.get("imagePath").toString()) != null && (boolean)documentSnapshot.get(map.get("imagePath").toString())==false){
////                                                    if (index == -1){
////                                                        notes.add(realNote);
////                                                        mAdapter.notifyItemInserted(notes.size()-1);
////
////                                                    }
////                                                    else{
////                                                        notes.set(index, realNote);
////                                                        mAdapter.notifyItemChanged(index);
////                                                    }
////                                                }
//                                            }});
//                                        }
//
////                            else{
////                                if (index == -1){
////                                    notes.add(realNote);
////                                    mAdapter.notifyItemInserted(notes.size()-1);
////
////                                }
////                                else{
////                                    notes.set(index, realNote);
////                                    mAdapter.notifyItemChanged(index);
////                                }
////                            }
//
//                        }
//                    });
//        }


//    private void reloadRecyclerView(){
//        // перезагрузка recyclerView. Удаляются все элементы notes, выбираются новые из бд
//        notes.clear();
//        selectAll();
//        mAdapter.notifyDataSetChanged();
//    }

//    private void reloadButtonsView(){
//        // перезагрузка buttonView. Удаляются все элементы button, выбираются новые из текущего пути
//        buttons.clear();
//        String pathTokens[] = (parent).split("/");
//        // текущий путь - строка из названий директорий
//        String prev = "";
//        for (int i = 0; i < pathTokens.length; i++){
//            if (pathTokens[i].equals("")){
//                continue;
//            }
//            prev = prev + pathTokens[i] + "/";
//            buttons.add(prev);
//        }
//        buttonAdapter.notifyDataSetChanged();
//    }

//    private void findViews(){
//        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewCatalog);  // здесь будут отображаться каталоги и файлы notes
//        buttonView = (RecyclerView) findViewById(R.id.buttonViewCatalog);  // здесь будут отображаться пройденные поддиректории buttons
//        toolbar = (MaterialToolbar) findViewById(R.id.long_click_toolbar);
//        counterText = (TextView) findViewById(R.id.counter_text);
//        findText1 = (EditText) findViewById(R.id.editTextFind);
//    }





    int rep =0;
    @Override
    public void onBackPressed()
    {

        for (int i=0;i<1;i++)
        {
            if (rep<3)
            {
                rep++;

                if(rep==1)
                {
                    Toast.makeText(CatalogActivity.this, "Для выхода из приложения нажмите ещё раз ", Toast.LENGTH_SHORT).show();
                    CountDownTimer mCount=new CountDownTimer(2000,1000)
                    {
                        @Override
                        public void onTick(long millisUntilFinished)
                        {

                        }

                        @Override
                        public void onFinish()
                        {

                            rep--;

                        }
                    }.start();


                }
                else if (rep==2)
                {
                    ext();
                    rep=0;
                }

            }
        }
    }

    private void ext()
    {
        moveTaskToBack(true);
        super.onDestroy();
        System.exit(0);

    }

//    private void setAdapters(){
//        mAdapter = new RecyclerViewAdapter(notes, this);
//        layoutManager = new LinearLayoutManager(this);
//        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
//        recyclerView.setAdapter(mAdapter);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setItemAnimator(itemAnimator);
//
//        mAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
//            @Override
//
//                public void onItemClick(int position) {
//                    // В notes хранятся объекты двух классов, имплементирующих Note - RealNote и Directory
//                    // RealNote - собственно запись пользователя. При клике нужно перейти к записи, т.е к NoteActivity
//                    // Directory - директория. При клике нужно перейти в эту директорию.
//                    int type = notes.get(position).getItemType();
//                    if (type == 0){
//                        RealNote realNote = (RealNote) notes.get(position);
//                        Intent intent = new Intent(CatalogActivity.this, NoteActivity.class);
//                        // чтобы понять какую запись нужно отобразить в NoteActivity, запихиваем в intent id записи из бд
//                        intent.putExtra("id", realNote.getID());
//                        startActivityForResult(intent, NOTE_REQUEST_CODE); // в NoteActivity пользователь может изменить путь.
//                        //Если изменит, то вернется intent, чтобы можно было изменить отображение каталогов
//                    }
//                    if (type == 1){
//                        active++;
//                        Directory directory = (Directory) notes.get(position);
//                        parent = directory.getDirectory(); // устанавливаем директорию, на которую нажали в качестве отправной
//                        notes.clear();
//                        Log.d("qwerty17", parent);
//                        buttons.add(parent);
//                        buttonAdapter.notifyDataSetChanged();
//                        selectAll(); // выбираем новые данные из бд
//                        mAdapter.notifyDataSetChanged();
//                    }
//                }
//
//            @Override
//            public void onItemLongClick(int position) {
//                mAdapter.setActionMode(true);
//                action_mode = true;
//                counterText.setText(count + " элементов выбрано");
//                toolbar.getMenu().clear();
//                toolbar.inflateMenu(R.menu.menu_long_click);
//                menuType=1;
////                toolbar.setMenu(m);
//                mAdapter.notifyDataSetChanged();
//                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            }
//
//
//                @Override
//                public void onCheckClick(int position) {
//                    count++;
//                    counterText.setText(count + " элементов выбрано");
//                    Note note = notes.get(position);
//                    if (note.getItemType()==1){
//                        selectionDirectoriesList.add((Directory) note);
//                     //   Toast.makeText(getApplicationContext(), selectionDirectoriesList.size() + " Directory", Toast.LENGTH_LONG).show();
//                    }
//                    else{
//                        selectionRealNotesList.add((RealNote) note);
//                      //  Toast.makeText(getApplicationContext(), selectionRealNotesList.size() + " RealNote", Toast.LENGTH_LONG).show();
//                    }
//
//                }
//
//
//                @Override
//                public void onUncheckClick(int position) {
//                    count--;
//                    counterText.setText(count + " элементов выбрано");
//                    Note note = notes.get(position);
//                    if (note.getItemType() == 1) {
//                        selectionDirectoriesList.remove((Directory) note);
//                        //  Toast.makeText(getApplicationContext(), selectionDirectoriesList.size() + " Directory", Toast.LENGTH_LONG).show();
//                    } else {
//                        selectionRealNotesList.remove((RealNote) note);
//                        //  Toast.makeText(getApplicationContext(), selectionRealNotesList.size() + " RealNote", Toast.LENGTH_LONG).show();
//                    }
//                }
//
//            @Override
//            public void onPrivacyChanged(final int position) {
//                if (notes.get(position).getItemType()==0){
//                    boolean isPrivate = ((RealNote)notes.get(position)).changePrivate();
//                    if (!isPrivate)
//                    {
//
//                        Map<String, String> map = new HashMap<>();
//                        List<String> list = new ArrayList<>();
//                        map.put(""+((RealNote) notes.get(position)).getTime(), notes.get(position).getID());
//                        db.collection("Publicly").document(user).set(map, SetOptions.merge());
//                        db.collection("Notes").document(user).collection("userNotes").document(notes.get(position).getID()).update("private", false);
//
//
//                    }
//                    else{
//                        Map<String, String> map = new HashMap<>();
//                        map.put(""+((RealNote) notes.get(position)).getTime(), notes.get(position).getID());
//                        db.collection("Publicly").document(user).update(""+((RealNote) notes.get(position)).getTime(), FieldValue.delete());
//                        db.collection("Notes").document(user).collection("userNotes").document(notes.get(position).getID()).update("private", true);
//
//                    }
//                    HashMap<String, Object> map = new HashMap<>();
//                    map.put("private", isPrivate);
//                    db.collection("Notes").document(user).collection("userNotes").document(notes.get(position).getID()).set(map, SetOptions.merge());
//                    mAdapter.notifyItemChanged(position);
//                }
//            }
//        });
//
//            buttonAdapter = new CatalogButtonAdapter(buttons);
//            LinearLayoutManager layoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//            RecyclerView.ItemAnimator itemAnimator1 = new DefaultItemAnimator();
//            buttonView.setAdapter(buttonAdapter);
//            buttonView.setLayoutManager(layoutManager1);
//            buttonView.setItemAnimator(itemAnimator1);
//            buttonAdapter.setOnItemClickListener(new CatalogButtonAdapter.OnItemClickListener() {
//                @Override
//                public void onItemClick(int position) {
//                    active++;
//                    parent = buttons.get(position);
//                    reloadButtonsView();
//                    reloadRecyclerView();
//                }
//            });
//    }
////

//    private void startSort() {
//        if (sortType == 0){
//            comp = "title";
//            order = 1;
//        }
//        if (sortType == 1){
//            comp = "title";
//            order = -1;
//        }
//        if (sortType == 2){
//            comp = "author";
//            order = 1;
//        }
//        if (sortType == 3){
//            comp = "author";
//            order = -1;
//        }
//        if (sortType == 4){
//            comp = "rating";
//            order = 1;
//        }
//        if (sortType == 5){
//            comp = "rating";
//            order = -1;
//        }
//        Log.d("qwerty121", startPos + " " + comp +  " " + order);
//        quickSort(startPos, notes.size() - 1);
//        mAdapter.notifyDataSetChanged();
//
//
//    }
//
//    private void quickSort(int from, int to) {
//        if (from < to) {
//            int divideIndex;
//            if (!comp.equals("rating")){
//                divideIndex = partitionString(from, to);
//            }
//            else{
//                divideIndex = partitionDouble(from, to);
//            }
//
//            quickSort(from, divideIndex - 1);
//            quickSort(divideIndex, to);
//        }
//    }
//    private int partitionString(int from, int to)
//    {
//        int rightIndex = to;
//        int leftIndex = from;
//
//        String pivot = getComparable((RealNote) notes.get(from + (to - from) / 2));
//        while (leftIndex <= rightIndex)
//        {
//
//            while (order * (getComparable((RealNote) notes.get(leftIndex)).compareTo(pivot)) < 0)
//            {
//                leftIndex++;
//            }
//
//            while (order * (getComparable((RealNote) notes.get(rightIndex)).compareTo(pivot)) > 0)
//            {
//                rightIndex--;
//            }
//
//            if (leftIndex <= rightIndex)
//            {
//                swap(rightIndex, leftIndex);
//                leftIndex++;
//                rightIndex--;
//            }
//        }
//        return leftIndex;
//    }
//
//    private int partitionDouble(int from, int to){
//        int rightIndex = to;
//        int leftIndex = from;
//
//        Double pivot = getComparableDouble((RealNote) notes.get(from + (to - from) / 2));
//        while (leftIndex <= rightIndex)
//        {
//
//            while (order * (getComparableDouble((RealNote) notes.get(leftIndex)).compareTo(pivot)) < 0)
//            {
//                leftIndex++;
//            }
//
//            while (order * (getComparableDouble((RealNote) notes.get(rightIndex)).compareTo(pivot)) > 0)
//            {
//                rightIndex--;
//            }
//
//            if (leftIndex <= rightIndex)
//            {
//                swap(rightIndex, leftIndex);
//                leftIndex++;
//                rightIndex--;
//            }
//        }
//        return leftIndex;
//    }
//
//
//    private String getComparable(RealNote realNote){
//        if (comp.equals("title")){
//            return realNote.getTitle();
//        }
//        if (comp.equals("author")){
//            return realNote.getAuthor();
//        }
//
//        return "";
//    }
//
//    private void swap(int index1, int index2)
//    {
//        Note tmp  = notes.get(index1);
//        notes.set(index1, notes.get(index2));
//        notes.set(index2, tmp);
//    }
//
//    private Double getComparableDouble(RealNote realNote){
//        return realNote.getRating();
//    }
//
//


}



//public class CatalogActivity extends AppCompatActivity implements SortDialogFragment.SortDialogListener,
//        SettingsDialogFragment.SettingsDialogListener, FilterDialogFragment.FilterDialogListener {
//    // класс отвечает за активность с каталогами
//    RecyclerViewAdapter mAdapter;
//    String parent = "./";
//    ArrayList<Note> notes;
//    ArrayList<String> buttons;
//    RecyclerView recyclerView;
//    RecyclerView buttonView;
//    CatalogButtonAdapter buttonAdapter;
//    Button findButton;
//    EditText findText1;
//    EditText findText;
//    ArrayList<String> sortsList;
//    MaterialToolbar toolbar;
//    TextView counterText;
//    String comp="";
//    int order;
//    int startPos;
//    int NOTE_REQUEST_CODE = 12345;
//    int CREATE_NOTE_REQUEST_CODE = 12346;
//    public boolean action_mode = false;
//    int count=0;
//    int menuType = 0;
//    int ext =0;
//    int sortType=0;
//    ArrayList<RealNote> selectionRealNotesList = new ArrayList<>();
//    ArrayList<Directory> selectionDirectoriesList = new ArrayList<>();
//    String[] choices = new String[]{"По названиям по возрастанию",
//            "По названиям по убыванию",
//            "По автору по возрастанию",
//            "По автору по убыванию",
//            "По рейтингу по возрастанию",
//            "По рейтингу по убыванию"};
//    ArrayList<Note> notFilteredNotes;
//    private String TAG_DARK = "dark_theme";
//    SharedPreferences sharedPreferences;
//    Button online;
//    MainActivity mein = new MainActivity();
//    int toolbarHeight=0;
//    private FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private String user = "user0";
//    int active=0;
//    String userID;
//    private ArrayList<String> checkedAuthors = new ArrayList<>();
//    private ArrayList<String> checkedGenres = new ArrayList<>();
//    LinearLayoutManager layoutManager;
//    private boolean noFilter = true;
//    private AppBarConfiguration mAppBarConfiguration;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        sharedPreferences = this.getSharedPreferences(TAG_DARK, Context.MODE_PRIVATE);
//        boolean dark = sharedPreferences.getBoolean(TAG_DARK, false);
//
//
//        if (dark){
//            setTheme(R.style.DarkTheme);
//        }
//        else{
//            setTheme(R.style.AppTheme);
//        }
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_navigation);
////        user = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
////        Получение разрешений на чтение и запись
//        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                == PackageManager.PERMISSION_GRANTED) {
//
//        } else {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//
//        }
//        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
//                == PackageManager.PERMISSION_GRANTED) {
//
//        } else {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
//        }
//        notes = new ArrayList<Note>(); // список того, что будет отображаться в каталоге.
//        buttons = new ArrayList<String>(); // Список пройденный каталогов до текущего
//        findViews();
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        counterText.setText("Каталог");
//        buttons.add(parent);
//        selectAll(); // чтение данных из бд
//        setAdapters();
////        online= (Button) findViewById(R.id.bOnline);
////        online.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                Intent intent = new Intent(CatalogActivity.this, OnlineActivity.class);
////                startActivity(intent);
////
////            }
////        });
//
//
//        // Кнопка добавление новой активности
//        FloatingActionButton addNote = (FloatingActionButton) findViewById(R.id.addNote);
//        addNote.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                // Возвращается intent, если пользователь действительно добавил активность
//                Intent intent = new Intent(CatalogActivity.this, EditNoteActivity.class);
//
//                intent.putExtra("path", parent);
//                Log.d("putExtra", parent + " !");
//                startActivityForResult(intent, CREATE_NOTE_REQUEST_CODE);
//
//
//            }
//        });
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        NavigationView navigationView = findViewById(R.id.nav_view);
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        mAppBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.nav_genres, R.id.nav_change_password, R.id.nav_log_out, R.id.nav_delete_account)
//                .setDrawerLayout(drawer)
//                .build();
//
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);
//
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
//                || super.onSupportNavigateUp();
//    }
//
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN){
//            InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
//        }
//        return super.dispatchTouchEvent(event);
//    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu){
////        getMenuInflater().inflate(R.menu.menu_catalog, menu);
////        getMenuInflater().inflate(R.menu.base_menu, menu);
//        getMenuInflater().inflate(R.menu.activity_navigation_drawer, menu);
//
//        return true;
//    }
//
//
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        active++;
//        if (data != null && requestCode == NOTE_REQUEST_CODE){
//            // если изменился путь до записи, добавилась новая запись, то переходим к этой записи
//            if (data.getExtras().get("deleted") != null){
//                String id = data.getExtras().get("id").toString();
//                int index = -1;
//                for (int i = 0; i < notes.size(); i++){
//                    if (notes.get(i).getID().equals(id)){
//                        index = i;
//                        break;
//                    }
//                }
//                if (index != -1){
//                    notes.remove(index);
//                    mAdapter.notifyItemRemoved(index);
//                }
//
//            }
//
//            else if (data.getExtras().get("path") != null){
//                if (parent.equals(data.getExtras().get("path").toString().replace("\\", "/"))){
//                    changeById(data.getExtras().get("id").toString());
//                }
//                else{
//                    parent = data.getExtras().get("path").toString().replace("\\", "/");
//                    reloadRecyclerView();
//                    reloadButtonsView();
//                }
//
//            }
//            else{
//                changeById(data.getExtras().get("id").toString());
//            }
//        }
//
//        if (requestCode==CREATE_NOTE_REQUEST_CODE && resultCode == RESULT_OK){
//            Intent intent = new Intent(CatalogActivity.this, NoteActivity.class); // вызов активности записи
//            intent.putExtra("id", data.getExtras().get("id").toString()); // передаем id активности в бд, чтобы понять какую активность надо показывать
//            intent.putExtra("changed", "true");
//            insertById(data.getExtras().get("id").toString());
//            startActivityForResult(intent, NOTE_REQUEST_CODE);
//
//        }
//        else if (data != null && data.getExtras() != null && data.getExtras().get("noNote") != null){
//            parent = data.getExtras().get("path").toString();
//            reloadRecyclerView();
//            reloadButtonsView();
//        }
//
//    }
//
//
//
//
//
//
//    @Override
//    public void onChangeThemeClick(boolean isChecked) {
//        Toast.makeText(this, "На нас напали светлые маги. Темная тема пока заперта", Toast.LENGTH_LONG).show();
//    }
//
//
//
//    @Override
//    public void onExitClick() {
//        ext =1;
//        mein.currentUser=null;
//        mein.mAuth.signOut();
//        Intent intent = new Intent(CatalogActivity.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//    }
//
//
//    @Override
//    public void onForgot()
//    {
//        Intent intent = new Intent(CatalogActivity.this, ForgotPswActivity.class);
//        startActivity(intent);
//    }
//
//    @Override
//    public void onDelete()
//    {
//        DeleteUser.deleteUser(this, user);
//        db.collection("PublicID").document(user).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                if (documentSnapshot == null || documentSnapshot.getString("id")==null){
//                    Toast.makeText(CatalogActivity.this,"Аккаунт удалён",Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(CatalogActivity.this, MainActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onChangeIdClick(String userID) {
//        AddShortNameFragment saveDialogFragment = new AddShortNameFragment(true, userID, user);
//        saveDialogFragment.setCancelable(false);
//        FragmentManager manager = getSupportFragmentManager();
//        FragmentTransaction transaction = manager.beginTransaction();
//        saveDialogFragment.show(transaction, "dialog");
//        this.userID = userID;
//    }
//
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId()==R.id.item_settings){
//            int location[] = new int[2];
//            toolbar.getLocationInWindow(location);
//            int y = getResources().getDisplayMetrics().heightPixels;
//            int x = getResources().getDisplayMetrics().widthPixels;
//            SettingsDialogFragment settingsDialogFragment = new SettingsDialogFragment(y, x, sharedPreferences.getBoolean(TAG_DARK, false));
//            FragmentManager manager = getSupportFragmentManager();
//            FragmentTransaction transaction = manager.beginTransaction();
//            settingsDialogFragment.show(transaction, "dialog");
//        }
//        if (item.getItemId()== R.id.item_delete){
//            action_mode=false;
//            mAdapter.setActionMode(false);
//            deleteSelectedRealNote();
//            deleteSelectedDirectories();
//            mAdapter.notifyDataSetChanged();
//            toolbar.getMenu().clear();
//            toolbar.inflateMenu(R.menu.menu_catalog);
//            toolbar.inflateMenu(R.menu.base_menu);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//            menuType = 0;
//            counterText.setText("Каталог");
//            count=0;
//        }
//        if (item.getItemId() == R.id.item_search){
//            counterText.setVisibility(View.GONE);
//            findText1.setVisibility(View.VISIBLE);
//            toolbar.getMenu().clear();
//            toolbar.inflateMenu(R.menu.menu_search);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
//            menuType = 2;
//
//        }
//        if (item.getItemId() == R.id.item_search1){
//            if (!findText1.getText().toString().equals("")){
//                notes.clear();
//                selectTitle(findText1.getText().toString());
//                mAdapter.notifyDataSetChanged();
//                findText1.clearComposingText();
//            }
//            counterText.setVisibility(View.VISIBLE);
//            findText1.setVisibility(View.GONE);
//            toolbar.getMenu().clear();
//            toolbar.inflateMenu(R.menu.menu_catalog);
//            toolbar.inflateMenu(R.menu.base_menu);
//            menuType = 0;
//        }
//
//        if (item.getItemId() == R.id.item_sort){
//            SortDialogFragment sortDialogFragment = new SortDialogFragment(choices, sortType);
//            FragmentManager manager = getSupportFragmentManager();
//            FragmentTransaction transaction = manager.beginTransaction();
//            sortDialogFragment.show(transaction, "dialog");
//        }
//
//        if (item.getItemId()==R.id.item_filter){
//            db.collection("genres").document(user).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                @Override
//                public void onSuccess(DocumentSnapshot documentSnapshot) {
//                    if (documentSnapshot != null && documentSnapshot.getData() != null){
//                        TreeSet<String> genres =  new TreeSet<>();
//                        genres.addAll(documentSnapshot.getData().keySet());
//                        TreeSet<String> authors = new TreeSet<>();
//                        for (Note note: notes) {
//                            if (note.getItemType()==0){
//                                authors.add(((RealNote) note).getAuthor());
////                    genres.add(((RealNote) note).getGenre());
//                            }
//                        }
//                        if (noFilter){
//                            checkedAuthors = new ArrayList<>(authors);
//                            checkedGenres = new ArrayList<>(genres);
//                        }
//                        FilterDialogFragment filterDialogFragment = new FilterDialogFragment(authors, genres, checkedAuthors, checkedGenres);
//                        FragmentManager manager = getSupportFragmentManager();
//                        FragmentTransaction transaction = manager.beginTransaction();
//                        filterDialogFragment.show(transaction, "dialog");
//                    }
//                }
//            });
//
//
//        }
//
//        if (item.getItemId() == android.R.id.home){
//            if (menuType==0){
////                finish();
//            }
//            else if (menuType == 1){
//                action_mode=false;
//                mAdapter.setActionMode(false);
//                mAdapter.notifyDataSetChanged();
//                toolbar.getMenu().clear();
//                toolbar.inflateMenu(R.menu.menu_catalog);
//                toolbar.inflateMenu(R.menu.base_menu);
//                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//                menuType = 0;
//                counterText.setText("Каталог");
//                count=0;
//            }
//            else if (menuType==2){
//                findText1.clearComposingText();
//                counterText.setVisibility(View.VISIBLE);
//                findText1.setVisibility(View.GONE);
//                toolbar.getMenu().clear();
//                toolbar.inflateMenu(R.menu.menu_catalog);
//                toolbar.inflateMenu(R.menu.base_menu);
//                menuType = 0;
//            }
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public void onSortClick(int position) {
//        sortType = position;
//        Log.d("strangeSort", choices[position]);
//        startSort();
//    }
//
//    public void deleteSelectedRealNote(){
//        for (int i = 0; i < selectionRealNotesList.size(); i++){
//            String id = selectionRealNotesList.get(i).getID();
//            if (!((RealNote)selectionRealNotesList.get(i)).getPrivate()){
//                DeleteNote.deletePublicly(user, id);
//            }
//            notes.remove(selectionRealNotesList.get(i));
//            mAdapter.notifyItemRemoved(i);
//            DeleteNote.deleteNote(user, id);
//
//        }
//        mAdapter.notifyDataSetChanged();
//    }
//
//    @Override
//    public void onFilterClick(ArrayList<String> checkedAuthors, ArrayList<String> checkedGenres) {
//        noFilter = false;
//        this.checkedAuthors=checkedAuthors;
//        this.checkedGenres=checkedGenres;
//        for (int i = 0; i < notes.size(); i++){
//            if (notes.get(i).getItemType() != 0){
//                break;
//            }
//            if (!checkedAuthors.contains(((RealNote)notes.get(i)).getAuthor())){
//                notes.get(i).setVisibility(false);
//                continue;
//            }
//            boolean visible = false;
//            for (String j : checkedGenres){
//                if ((boolean)((RealNote)notes.get(i)).getGenre().get(j)){
//                    visible=true;
//                    break;
//                }
//            }
//            if (visible){
//                notes.get(i).setVisibility(true);
//            }
//            else{
//                notes.get(i).setVisibility(false);
//            }
//        }
//        mAdapter.notifyDataSetChanged();
//    }
//
//    public void deleteSelectedDirectories(){
//        for (Directory directory : selectionDirectoriesList){
//            notes.remove(directory);
//            deleteDirectory(directory.getDirectory().replace("/", "\\"));
//            String s = directory.getDirectory();
//            String parDoc = s.substring(0, s.substring(0, s.length() - 1).lastIndexOf("/")+1).replace("/", "\\");
//            db.collection("User").document(user).collection("paths").document(parDoc).update("paths", FieldValue.arrayRemove(s.replace("/", "\\")));
//        }
//        selectionDirectoriesList.clear();
//    }
//
//    public void deleteDirectory(String path){
//        final String path1 = path;
//        final File dir0 = new File(path);
//        db.collection("User").document(user).collection("paths").whereEqualTo("parent", path1).get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (queryDocumentSnapshots != null){
//                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                                deleteDirectory(documentSnapshot.getId());
//                            }
//                        }
//                    }
//                });
//        db.collection("User").document(user).collection("paths").document(path1).delete();
//
//        db.collection("Notes").document(user).collection("userNotes").whereEqualTo("path", path1).get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (queryDocumentSnapshots != null){
//                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                                DeleteNote.deleteNote(user, documentSnapshot.getId());
//                                if (!(boolean)documentSnapshot.get("private")){
//                                    DeleteNote.deletePublicly(user, documentSnapshot.getId());
//                                }
//                            }
//                        }
//
//                    }
//                });
//    }
//
//
////    private void selectAll() {
////        final String par1 = parent.replace("/", "\\");
////        final String par2 = parent;
////        final long old_active = active;
////        db.collection("User").document(user).collection("paths").document(par1).get()
////                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
////                    @Override
////                    public void onSuccess(DocumentSnapshot documentSnapshot) {
////                        if (documentSnapshot != null){
////                            ArrayList<String> list = (ArrayList<String>) documentSnapshot.get("paths");
////                            if (list != null) {
////                                for (String i : list) {
////                                    if (active!=old_active){
////                                        break;
////                                    }
////                                    if (!par1.equals(parent.replace("/", "\\"))){
////                                        break;
////                                    }
////                                    notes.add(new Directory(i, i.replace("\\", "/")));
////                                }
////                            }
////                        }
////                        startPos = notes.size();
////                        mAdapter.notifyDataSetChanged();
////                        db.collection("Notes").document(user).collection("userNotes").whereEqualTo("path", par1).get()
////                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
////                                    @Override
////                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
////                                        if (queryDocumentSnapshots != null){
////                                            for (final QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
////                                                if (active!=old_active){
////                                                    break;
////                                                }
////                                                  final HashMap<String, Object> map = (HashMap<String, Object>) documentSnapshot.getData();
////                                                generateNote(documentSnapshot.getId(), -1);
////                                            }
////                                        }
////                                        mAdapter.notifyDataSetChanged();
////                                    }
////                                })
////                                .addOnFailureListener(new OnFailureListener() {
////                                    @Override
////                                    public void onFailure(@NonNull Exception e) {
////                                        Log.e("qwerty9", e.toString());
////                                    }
////                                });
////                    }
////                })
////                .addOnFailureListener(new OnFailureListener() {
////                    @Override
////                    public void onFailure(@NonNull Exception e) {
////                        Log.e("qwerty10", e.toString());
////                    }
////                });
////    }
//
//    private void selectTitle(String title){
//        db.collection("Notes").document(user).collection("userNotes").whereEqualTo("title", title).get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (queryDocumentSnapshots != null){
//                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                                generateNote(documentSnapshot.getId(), -1);
//                            }
//                        }
//                        mAdapter.notifyDataSetChanged();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e("qwerty9", e.toString());
//                    }
//                });
//    }
//
//    private void changeById(final String id){
//        for (int j = startPos; j < notes.size(); j++){
//            final int i = j;
//            if (notes.get(i).getID().equals(id)){
//                generateNote(notes.get(i).getID(), i);
//                break;
//            }
//        }
//    }
//
//    private void insertById(final String id){
//        generateNote(id, -1);
//    }
//
//    public void generateNote(final String id, final int index){
//        final int active0 = active;
//        db.collection("Notes").document(user).collection("userNotes").document(id).get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(final DocumentSnapshot documentSnapshot0) {
//                        final HashMap<String, Object> map = (HashMap) documentSnapshot0.getData();
//                        if (map==null) return;
//                        TreeMap<String, Object> genreMap = new TreeMap<>();
//                        genreMap.putAll((Map)map.get("genre"));
//                        final RealNote realNote = new RealNote(id, map.get("path").toString(), map.get("author").toString(),
//                                map.get("title").toString(), Double.valueOf(map.get("rating").toString()), (boolean)map.get("private"),
//                                (double) map.get("publicRatingSum"), (long)map.get("publicRatingCount"), genreMap);
//                        realNote.setTime(Long.parseLong(map.get("timeAdd").toString()));
//                        final int index1;
//                        if (active0 == active){
//                            if (index == -1) {
//                                notes.add(realNote);
//                                mAdapter.notifyItemInserted(notes.size() - 1);
//                                index1 = notes.size()-1;
//
//                            } else {
//                                notes.set(index, realNote);
//                                mAdapter.notifyItemChanged(index);
//                                index1 = index;
//                            }
//                        }
//                        else{
//                            index1=0;
//                            return;
//                        }
//
//                        if (map.get("imagePath")!= null && !map.get("imagePath").toString().equals("")){
//                            db.collection("Common").document(user).collection(documentSnapshot0.getId()).document("Images").addSnapshotListener(CatalogActivity.this, new EventListener<DocumentSnapshot>() {
//                                @Override
//                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
////                                                Log.d("qwerty72", (boolean)documentSnapshot.get(map.get("imagePath").toString()) + "");
//                                    if (documentSnapshot.get(map.get("imagePath").toString()) != null && (boolean)documentSnapshot.get(map.get("imagePath").toString())==true){
//                                        Log.d("qwerty72", "imagePathTrue");
//                                        Log.d("qwerty72", map.get("imagePath").toString());
//                                        FirebaseStorage.getInstance().getReference(user).child(documentSnapshot0.getId()).child("Images").child(map.get("imagePath").toString()).getDownloadUrl()
//                                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                                    @Override
//                                                    public void onSuccess(Uri uri) {
//                                                        Log.d("qwerty72", "success");
//                                                        if (active0 == active){
//                                                            realNote.setCoverPath(uri);
//                                                            ((RealNote)notes.get(index1)).setCoverPath(uri);
//                                                            mAdapter.notifyItemChanged(index1);
//                                                        }
//
//
////                                                                    if (index == -1) {
////                                                                        notes.add(realNote);
////                                                                        mAdapter.notifyItemInserted(notes.size() - 1);
////                                                                    } else {
////                                                                        notes.set(index, realNote);
////                                                                        mAdapter.notifyItemChanged(index);
////                                                                    }
//                                                    }
//                                                })
//                                                .addOnFailureListener(new OnFailureListener() {
//                                                    @Override
//                                                    public void onFailure(@NonNull Exception e) {
//                                                        Log.d("qwerty72", e.toString());
//                                                        if (active0 == active){
//                                                            if (index == -1) {
//                                                                notes.add(realNote);
//                                                                mAdapter.notifyItemInserted(notes.size() - 1);
//                                                            } else {
//                                                                notes.set(index, realNote);
//                                                                mAdapter.notifyItemChanged(index);
//                                                            }
//                                                        }
//
//                                                    }
//                                                });
//                                    }
////                                                else if (documentSnapshot.get(map.get("imagePath").toString()) != null && (boolean)documentSnapshot.get(map.get("imagePath").toString())==false){
////                                                    if (index == -1){
////                                                        notes.add(realNote);
////                                                        mAdapter.notifyItemInserted(notes.size()-1);
////
////                                                    }
////                                                    else{
////                                                        notes.set(index, realNote);
////                                                        mAdapter.notifyItemChanged(index);
////                                                    }
////                                                }
//                                }});
//                        }
//
////                            else{
////                                if (index == -1){
////                                    notes.add(realNote);
////                                    mAdapter.notifyItemInserted(notes.size()-1);
////
////                                }
////                                else{
////                                    notes.set(index, realNote);
////                                    mAdapter.notifyItemChanged(index);
////                                }
////                            }
//
//                    }
//                });
//    }
//
//
//    private void reloadRecyclerView(){
//        // перезагрузка recyclerView. Удаляются все элементы notes, выбираются новые из бд
//        notes.clear();
//        selectAll();
//        mAdapter.notifyDataSetChanged();
//    }
//
//    private void reloadButtonsView(){
//        // перезагрузка buttonView. Удаляются все элементы button, выбираются новые из текущего пути
//        buttons.clear();
//        String pathTokens[] = (parent).split("/");
//        // текущий путь - строка из названий директорий
//        String prev = "";
//        for (int i = 0; i < pathTokens.length; i++){
//            if (pathTokens[i].equals("")){
//                continue;
//            }
//            prev = prev + pathTokens[i] + "/";
//            buttons.add(prev);
//        }
//        buttonAdapter.notifyDataSetChanged();
//    }
//
//    private void findViews(){
//        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewCatalog);  // здесь будут отображаться каталоги и файлы notes
//        buttonView = (RecyclerView) findViewById(R.id.buttonViewCatalog);  // здесь будут отображаться пройденные поддиректории buttons
//        toolbar = (MaterialToolbar) findViewById(R.id.long_click_toolbar);
//        counterText = (TextView) findViewById(R.id.counter_text);
//        findText1 = (EditText) findViewById(R.id.editTextFind);
//    }
//
//
//
//
//
//    int rep =0;
//    @Override
//    public void onBackPressed()
//    {
//
//        for (int i=0;i<1;i++)
//        {
//            if (rep<3)
//            {
//                rep++;
//
//                if(rep==1)
//                {
//                    Toast.makeText(CatalogActivity.this, "Для выхода из приложения нажмите ещё раз ", Toast.LENGTH_SHORT).show();
//                    CountDownTimer mCount=new CountDownTimer(2000,1000)
//                    {
//                        @Override
//                        public void onTick(long millisUntilFinished)
//                        {
//
//                        }
//
//                        @Override
//                        public void onFinish()
//                        {
//
//                            rep--;
//
//                        }
//                    }.start();
//
//
//                }
//                else if (rep==2)
//                {
//                    ext();
//                    rep=0;
//                }
//
//            }
//        }
//    }
//
//    private void ext()
//    {
//        moveTaskToBack(true);
//        super.onDestroy();
//        System.exit(0);
//
//    }
//
//    private void setAdapters(){
//        mAdapter = new RecyclerViewAdapter(notes, this);
//        layoutManager = new LinearLayoutManager(this);
//        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
//        recyclerView.setAdapter(mAdapter);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setItemAnimator(itemAnimator);
//
//        mAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
//            @Override
//
//            public void onItemClick(int position) {
//                // В notes хранятся объекты двух классов, имплементирующих Note - RealNote и Directory
//                // RealNote - собственно запись пользователя. При клике нужно перейти к записи, т.е к NoteActivity
//                // Directory - директория. При клике нужно перейти в эту директорию.
//                int type = notes.get(position).getItemType();
//                if (type == 0){
//                    RealNote realNote = (RealNote) notes.get(position);
//                    Intent intent = new Intent(CatalogActivity.this, NoteActivity.class);
//                    // чтобы понять какую запись нужно отобразить в NoteActivity, запихиваем в intent id записи из бд
//                    intent.putExtra("id", realNote.getID());
//                    startActivityForResult(intent, NOTE_REQUEST_CODE); // в NoteActivity пользователь может изменить путь.
//                    //Если изменит, то вернется intent, чтобы можно было изменить отображение каталогов
//                }
//                if (type == 1){
//                    active++;
//                    Directory directory = (Directory) notes.get(position);
//                    parent = directory.getDirectory(); // устанавливаем директорию, на которую нажали в качестве отправной
//                    notes.clear();
//                    Log.d("qwerty17", parent);
//                    buttons.add(parent);
//                    buttonAdapter.notifyDataSetChanged();
//                    selectAll(); // выбираем новые данные из бд
//                    mAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onItemLongClick(int position) {
//                mAdapter.setActionMode(true);
//                action_mode = true;
//                counterText.setText(count + " элементов выбрано");
//                toolbar.getMenu().clear();
//                toolbar.inflateMenu(R.menu.menu_long_click);
//                menuType=1;
////                toolbar.setMenu(m);
//                mAdapter.notifyDataSetChanged();
//                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            }
//
//
//            @Override
//            public void onCheckClick(int position) {
//                count++;
//                counterText.setText(count + " элементов выбрано");
//                Note note = notes.get(position);
//                if (note.getItemType()==1){
//                    selectionDirectoriesList.add((Directory) note);
//                    //   Toast.makeText(getApplicationContext(), selectionDirectoriesList.size() + " Directory", Toast.LENGTH_LONG).show();
//                }
//                else{
//                    selectionRealNotesList.add((RealNote) note);
//                    //  Toast.makeText(getApplicationContext(), selectionRealNotesList.size() + " RealNote", Toast.LENGTH_LONG).show();
//                }
//
//            }
//
//
//            @Override
//            public void onUncheckClick(int position) {
//                count--;
//                counterText.setText(count + " элементов выбрано");
//                Note note = notes.get(position);
//                if (note.getItemType() == 1) {
//                    selectionDirectoriesList.remove((Directory) note);
//                    //  Toast.makeText(getApplicationContext(), selectionDirectoriesList.size() + " Directory", Toast.LENGTH_LONG).show();
//                } else {
//                    selectionRealNotesList.remove((RealNote) note);
//                    //  Toast.makeText(getApplicationContext(), selectionRealNotesList.size() + " RealNote", Toast.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onPrivacyChanged(final int position) {
//                if (notes.get(position).getItemType()==0){
//                    boolean isPrivate = ((RealNote)notes.get(position)).changePrivate();
//                    if (!isPrivate)
//                    {
//
//                        Map<String, String> map = new HashMap<>();
//                        List<String> list = new ArrayList<>();
//                        map.put(""+((RealNote) notes.get(position)).getTime(), notes.get(position).getID());
//                        db.collection("Publicly").document(user).set(map, SetOptions.merge());
//                        db.collection("Notes").document(user).collection("userNotes").document(notes.get(position).getID()).update("private", false);
//
//
//                    }
//                    else{
//                        Map<String, String> map = new HashMap<>();
//                        map.put(""+((RealNote) notes.get(position)).getTime(), notes.get(position).getID());
//                        db.collection("Publicly").document(user).update(""+((RealNote) notes.get(position)).getTime(), FieldValue.delete());
//                        db.collection("Notes").document(user).collection("userNotes").document(notes.get(position).getID()).update("private", true);
//
//                    }
//                    HashMap<String, Object> map = new HashMap<>();
//                    map.put("private", isPrivate);
//                    db.collection("Notes").document(user).collection("userNotes").document(notes.get(position).getID()).set(map, SetOptions.merge());
//                    mAdapter.notifyItemChanged(position);
//                }
//            }
//        });
//
//        buttonAdapter = new CatalogButtonAdapter(buttons);
//        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//        RecyclerView.ItemAnimator itemAnimator1 = new DefaultItemAnimator();
//        buttonView.setAdapter(buttonAdapter);
//        buttonView.setLayoutManager(layoutManager1);
//        buttonView.setItemAnimator(itemAnimator1);
//        buttonAdapter.setOnItemClickListener(new CatalogButtonAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(int position) {
//                active++;
//                parent = buttons.get(position);
//                reloadButtonsView();
//                reloadRecyclerView();
//            }
//        });
//    }
////
//
//    private void startSort() {
//        if (sortType == 0){
//            comp = "title";
//            order = 1;
//        }
//        if (sortType == 1){
//            comp = "title";
//            order = -1;
//        }
//        if (sortType == 2){
//            comp = "author";
//            order = 1;
//        }
//        if (sortType == 3){
//            comp = "author";
//            order = -1;
//        }
//        if (sortType == 4){
//            comp = "rating";
//            order = 1;
//        }
//        if (sortType == 5){
//            comp = "rating";
//            order = -1;
//        }
//        Log.d("qwerty121", startPos + " " + comp +  " " + order);
//        quickSort(startPos, notes.size() - 1);
//        mAdapter.notifyDataSetChanged();
//
//
//    }
//
//    private void quickSort(int from, int to) {
//        if (from < to) {
//            int divideIndex;
//            if (!comp.equals("rating")){
//                divideIndex = partitionString(from, to);
//            }
//            else{
//                divideIndex = partitionDouble(from, to);
//            }
//
//            quickSort(from, divideIndex - 1);
//            quickSort(divideIndex, to);
//        }
//    }
//    private int partitionString(int from, int to)
//    {
//        int rightIndex = to;
//        int leftIndex = from;
//
//        String pivot = getComparable((RealNote) notes.get(from + (to - from) / 2));
//        while (leftIndex <= rightIndex)
//        {
//
//            while (order * (getComparable((RealNote) notes.get(leftIndex)).compareTo(pivot)) < 0)
//            {
//                leftIndex++;
//            }
//
//            while (order * (getComparable((RealNote) notes.get(rightIndex)).compareTo(pivot)) > 0)
//            {
//                rightIndex--;
//            }
//
//            if (leftIndex <= rightIndex)
//            {
//                swap(rightIndex, leftIndex);
//                leftIndex++;
//                rightIndex--;
//            }
//        }
//        return leftIndex;
//    }
//
//    private int partitionDouble(int from, int to){
//        int rightIndex = to;
//        int leftIndex = from;
//
//        Double pivot = getComparableDouble((RealNote) notes.get(from + (to - from) / 2));
//        while (leftIndex <= rightIndex)
//        {
//
//            while (order * (getComparableDouble((RealNote) notes.get(leftIndex)).compareTo(pivot)) < 0)
//            {
//                leftIndex++;
//            }
//
//            while (order * (getComparableDouble((RealNote) notes.get(rightIndex)).compareTo(pivot)) > 0)
//            {
//                rightIndex--;
//            }
//
//            if (leftIndex <= rightIndex)
//            {
//                swap(rightIndex, leftIndex);
//                leftIndex++;
//                rightIndex--;
//            }
//        }
//        return leftIndex;
//    }
//
//
//    private String getComparable(RealNote realNote){
//        if (comp.equals("title")){
//            return realNote.getTitle();
//        }
//        if (comp.equals("author")){
//            return realNote.getAuthor();
//        }
//
//        return "";
//    }
//
//    private void swap(int index1, int index2)
//    {
//        Note tmp  = notes.get(index1);
//        notes.set(index1, notes.get(index2));
//        notes.set(index2, tmp);
//    }
//
//    private Double getComparableDouble(RealNote realNote){
//        return realNote.getRating();
//    }
//
//
//
//
//}
//
