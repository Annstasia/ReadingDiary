package com.example.readingdiary.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readingdiary.Activities.CatalogActivity;
import com.example.readingdiary.Activities.EditNoteActivity;
import com.example.readingdiary.Activities.NoteActivity;
import com.example.readingdiary.Classes.DeleteNote;
import com.example.readingdiary.Classes.Directory;
import com.example.readingdiary.Classes.Note;
import com.example.readingdiary.Classes.RealNote;
import com.example.readingdiary.Classes.ui.CatalogViewModel;
import com.example.readingdiary.R;
import com.example.readingdiary.adapters.CatalogButtonAdapter;
import com.example.readingdiary.adapters.RecyclerViewAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

public class CatalogFragment extends Fragment {
    public interface OnCatalogFragmentListener{
        void changeFragment(Fragment fragment);
    }
    private OnCatalogFragmentListener onCatalogFragmentListener;
    private CatalogViewModel catalogViewModel;
    RecyclerViewAdapter mAdapter;
    String parent = "./";
    ArrayList<Note> notes;
    ArrayList<String> notesID;
    HashMap<String, Object>  genres;
    ArrayList<String> buttons;
    RecyclerView recyclerView;
    RecyclerView buttonView;
    CatalogButtonAdapter buttonAdapter;
    Button findButton;
    EditText findText1;
    EditText findText;
    ArrayList<String> sortsList;
    androidx.appcompat.widget.Toolbar toolbar;
    TextView counterText;
    String comp = "";
    int order;
    int startPos;
    int NOTE_REQUEST_CODE = 12345;
    int CREATE_NOTE_REQUEST_CODE = 12346;
    public boolean action_mode = false;
    int count = 0;
    int menuType = 0;
    int ext = 0;
    int sortType = 0;
    ArrayList<RealNote> selectionRealNotesList = new ArrayList<>();
    ArrayList<Directory> selectionDirectoriesList = new ArrayList<>();
    ArrayList<Note> notFilteredNotes;
    private String TAG_DARK = "dark_theme";
    SharedPreferences sharedPreferences;
    Button online;
    int toolbarHeight = 0;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String user = "user0";
    int active = 0;
    String userID;
    private ArrayList<String> checkedAuthors = new ArrayList<>();
    private ArrayList<String> checkedGenres = new ArrayList<>();
    LinearLayoutManager layoutManager;
    private boolean noFilter = true;
    private AppBarConfiguration mAppBarConfiguration;
    View root;
    private boolean filterClicked = false;
    private boolean showCatalog=true;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.activity_catalog, container, false);
        if (FirebaseAuth.getInstance().getCurrentUser()==null){
            user=null;
        }
        else{
            user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        if (user != null){
            notes = new ArrayList<Note>(); // список того, что будет отображаться в каталоге.
            buttons = new ArrayList<String>(); // Список пройденный каталогов до текущего
            findViews();
            buttons.add(parent);

            db.collection("genres").document(user).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null && documentSnapshot.getData()!= null){
                        if (genres==null){
                            genres = (HashMap)documentSnapshot.getData();
                            selectAll();
                        }
                        else{
                            genres = (HashMap)documentSnapshot.getData();
                        }

                    }
                }
            });

            setAdapters();
            // Кнопка добавление новой активности
            FloatingActionButton addNote = root.findViewById(R.id.addNote);
            addNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(getContext(), EditNoteActivity.class);
                    intent.putExtra("path", parent);
                    startActivityForResult(intent, CREATE_NOTE_REQUEST_CODE);
                }
            });
            onCatalogFragmentListener.changeFragment(this);
            toolbar = getActivity().findViewById(R.id.toolbar_navigation);
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.menu_catalog);

        }


        return root;
    }



    public void sortClick(){
        SortDialogFragment sortDialogFragment = new SortDialogFragment(sortType);
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        sortDialogFragment.show(transaction, "dialog");
    }

    public void filterClick(){
        if (notes.contains(null)){
            filterClicked = true;
            return;
        }
        ArrayList<String> authors = new ArrayList<>();
        for (Note note: notes) {
            if (note.getItemType()==0 && !authors.contains(((RealNote)note).getAuthor())){
                authors.add(((RealNote) note).getAuthor());
            }
        }
        ArrayList<String> genresList = new ArrayList<>();
        for (Object ob : genres.values()) {
            genresList.add(ob.toString());
        }
        Collections.sort(authors);
        if (noFilter){
            checkedAuthors = new ArrayList<>(authors);
            checkedGenres = new ArrayList<>(genresList);
            checkedAuthors.add("Не указан");
            checkedGenres.add("Не указан");

        }
        FilterDialogFragment filterDialogFragment = new FilterDialogFragment(authors, genresList, new ArrayList<String>(genres.keySet()), checkedAuthors, checkedGenres, showCatalog);
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        filterDialogFragment.show(transaction, "dialog");
    }


    public void deleteClick(){
        action_mode=false;
        mAdapter.setActionMode(false);
        deleteSelectedRealNote();
        deleteSelectedDirectories();
        mAdapter.notifyDataSetChanged();
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_catalog);
        menuType = 0;
        counterText.setText("");
        count=0;
        toolbar.setNavigationIcon(R.drawable.ic_navigation_light);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CatalogActivity)getActivity()).drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    public void searchCLick1(){
        counterText.setVisibility(View.GONE);
        findText1.setVisibility(View.VISIBLE);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_search);

        toolbar.setNavigationIcon(R.drawable.ic_back_light);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counterText.setVisibility(View.VISIBLE);
                findText1.setVisibility(View.GONE);
                toolbar.getMenu().clear();
                toolbar.inflateMenu(R.menu.menu_catalog);
                toolbar.setNavigationIcon(R.drawable.ic_navigation_light);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((CatalogActivity)getActivity()).drawerLayout.openDrawer(GravityCompat.START);
                    }
                });

            }
        });
    }

    public void searchClick2(){
        if (!findText1.getText().toString().equals("")){
            notes.clear();
            searchInDocuments(findText1.getText().toString());
            mAdapter.notifyDataSetChanged();
            findText1.clearComposingText();

        }
        counterText.setVisibility(View.VISIBLE);
        findText1.setVisibility(View.GONE);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_catalog);
        toolbar.setNavigationIcon(R.drawable.ic_navigation_light);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CatalogActivity)getActivity()).drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }



    private void findViews(){
        recyclerView = root.findViewById(R.id.recyclerViewCatalog);  // здесь будут отображаться каталоги и файлы notes
        buttonView = root.findViewById(R.id.buttonViewCatalog);  // здесь будут отображаться пройденные поддиректории buttons
        counterText = getActivity().findViewById(R.id.counter_text);
        findText1 = getActivity().findViewById(R.id.editTextFind);
    }

    private void setAdapters(){
        mAdapter = new RecyclerViewAdapter(notes, getContext());
        layoutManager = new LinearLayoutManager(getContext());
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);

        mAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override

            public void onItemClick(int position) {
                // В notes хранятся объекты двух классов, имплементирующих Note - RealNote и Directory
                // RealNote - собственно запись пользователя. При клике нужно перейти к записи, т.е к NoteActivity
                // Directory - директория. При клике нужно перейти в эту директорию.
                int type = notes.get(position).getItemType();
                if (type == 0){
                    Intent intent = new Intent(getContext(), NoteActivity.class);
                    intent.putExtra("id", notes.get(position).getID());
                    startActivityForResult(intent, NOTE_REQUEST_CODE);
                }
                if (type == 1){
                    active++;
                    Directory directory = (Directory) notes.get(position);
                    parent = directory.getDirectory(); // устанавливаем директорию, на которую нажали в качестве отправной
                    notes.clear();
                    Log.d("qwerty17", parent);
                    buttons.add(parent);
                    buttonAdapter.notifyDataSetChanged();
                    selectAll(); // выбираем новые данные из бд
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onItemLongClick(int position) {
                mAdapter.setActionMode(true);
                action_mode = true;
                counterText.setText(count + " выбрано");
                toolbar.getMenu().clear();
                toolbar.inflateMenu(R.menu.menu_long_click);
                toolbar.setNavigationIcon(R.drawable.ic_back_light);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        action_mode=false;
                        mAdapter.setActionMode(false);
                        mAdapter.notifyDataSetChanged();
                        toolbar.getMenu().clear();
                        toolbar.inflateMenu(R.menu.menu_catalog);
                        menuType = 0;
                        counterText.setText("");
                        count=0;
                        toolbar.setNavigationIcon(R.drawable.ic_navigation_light);
                        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((CatalogActivity)getActivity()).drawerLayout.openDrawer(GravityCompat.START);
                            }
                        });
                    }
                });
                menuType=1;
                mAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCheckClick(int position) {
                count++;
                counterText.setText(count + " выбрано");
                Note note = notes.get(position);
                if (note.getItemType()==1){
                    selectionDirectoriesList.add((Directory) note);
                   }
                else{
                    selectionRealNotesList.add((RealNote) note);
                   }

            }


            @Override
            public void onUncheckClick(int position) {
                count--;
                counterText.setText(count + " выбрано");
                Note note = notes.get(position);
                if (note.getItemType() == 1) {
                    selectionDirectoriesList.remove(note);
                } else {
                    selectionRealNotesList.remove(note);
                }
            }
        });

        buttonAdapter = new CatalogButtonAdapter(buttons);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        RecyclerView.ItemAnimator itemAnimator1 = new DefaultItemAnimator();
        buttonView.setAdapter(buttonAdapter);
        buttonView.setLayoutManager(layoutManager1);
        buttonView.setItemAnimator(itemAnimator1);
        buttonAdapter.setOnItemClickListener(new CatalogButtonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                active++;
                parent = buttons.get(position);
                reloadButtonsView();
                reloadRecyclerView();
            }
        });
    }



    private void selectAll() {
        final String par1 = parent.replace("/", "\\");
        final String par2 = parent;
        final long old_active = active;
        db.collection("User").document(user).collection("paths").document(par1).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null){
                            ArrayList<String> list = (ArrayList<String>) documentSnapshot.get("paths");
                            if (list != null) {
                                for (String i : list) {
                                    if (active!=old_active){
                                        break;
                                    }
                                    if (!par1.equals(parent.replace("/", "\\"))){
                                        break;
                                    }
                                    notes.add(new Directory(i, i.replace("\\", "/")));
                                }
                            }
                        }
                        startPos = notes.size();
                        mAdapter.notifyDataSetChanged();
                        db.collection("Notes").document(user).collection("userNotes").whereEqualTo("path", par1).get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if (queryDocumentSnapshots != null){
                                            ArrayList<Long> names = new ArrayList<>();
                                            for (final QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                if (active!=old_active){
                                                    break;
                                                }
                                                int index = names.size();
                                                for (int i = 0; i < index; i++){
                                                    if (Long.parseLong(documentSnapshot.getId()) > names.get(i)){
                                                        index = i;
                                                        break;
                                                    }
                                                }
                                                names.add(index, Long.parseLong(documentSnapshot.getId()));
                                                notes.add(startPos + index, null);
                                            }
                                            for (int i = 0; i < names.size(); i++){
                                                generateNote(names.get(i).toString(), startPos + i, true);
                                            }

                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("qwerty9", e.toString());
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("qwerty10", e.toString());
                    }
                });
    }



    private void reloadButtonsView(){
        // перезагрузка buttonView. Удаляются все элементы button, выбираются новые из текущего пути
        buttons.clear();
        String[] pathTokens = (parent).split("/");
        // текущий путь - строка из названий директорий
        String prev = "";
        for (int i = 0; i < pathTokens.length; i++){
            if (pathTokens[i].equals("")){
                continue;
            }
            prev = prev + pathTokens[i] + "/";
            buttons.add(prev);
        }
        buttonAdapter.notifyDataSetChanged();
    }

    private void reloadRecyclerView(){
        // перезагрузка recyclerView. Удаляются все элементы notes, выбираются новые из бд
        notes.clear();
        selectAll();
        mAdapter.notifyDataSetChanged();
    }

    public void pathUpdate(String newPath){
        if (parent.equals(newPath.replace("\\", "/"))){
            changeById(newPath);
        }
        else{
            parent = newPath.replace("\\", "/");
            reloadRecyclerView();
            reloadButtonsView();
        }
    }

    public void deleteNote(String id){
        int index = -1;
        for (int i = 0; i < notes.size(); i++){
            if (notes.get(i).getID().equals(id)){
                index = i;
                break;
            }
        }
        if (index != -1){
            notes.remove(index);
            mAdapter.notifyItemRemoved(index);
        }
    }


    public void generateNote(final String id, final int index, final boolean change){
        final int active0 = active;

        db.collection("Notes").document(user).collection("userNotes").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot documentSnapshot0) {
                        final HashMap<String, Object> map = (HashMap) documentSnapshot0.getData();
                        if (map==null) return;
                        HashMap<String, Object> genreMap = new HashMap<>();
                        if (map.get("genre")!= null){
                            genreMap = (HashMap)map.get("genre");
                            boolean changes = false;
                            ArrayList<String> arrayList = new ArrayList<>(genreMap.keySet());
                            for (String i : arrayList){
                                if (!genres.containsKey(i)){
                                    genreMap.remove(i);
                                    changes = true;
                                }
                                if (genres.get(i) != genreMap.get(i)){
                                    genreMap.put(i, genres.get(i));
                                    changes=true;
                                }
                            }
                            if (changes){
                                db.collection("Notes").document(user).collection("userNotes").document(id).update("genre", genreMap);
                            }
                        }
                        final RealNote realNote = new RealNote(id, map.get("path").toString(), map.get("author").toString(),
                                map.get("title").toString(), Double.valueOf(map.get("rating").toString()), genreMap);
                        realNote.setTime(Long.parseLong(map.get("timeAdd").toString()));
                        final int index1;
                        if (active0 == active){
                            if (index == -1) {
                                notes.add(realNote);
                                mAdapter.notifyItemInserted(notes.size() - 1);
                                index1 = notes.size()-1;

                            } else {
                                if (change){
                                    notes.set(index, realNote);
                                    mAdapter.notifyItemChanged(index);
                                }
                                else{
                                    notes.add(index, realNote);
                                    mAdapter.notifyItemInserted(index);
                                }
                                index1 = index;
                            }
                        }
                        else{
                            index1=0;
                            return;
                        }
                        if(filterClicked && !notes.contains(null)){
                            filterClicked=false;
                            filterClick();

                        }

                        if (map.get("imagePath")!= null && !map.get("imagePath").toString().equals("")){
                            db.collection("Common").document(user).collection(documentSnapshot0.getId()).document("Images").addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                                                Log.d("qwerty72", (boolean)documentSnapshot.get(map.get("imagePath").toString()) + "");
                                    if (documentSnapshot.get(map.get("imagePath").toString()) != null && (boolean)documentSnapshot.get(map.get("imagePath").toString())==true){
                                        FirebaseStorage.getInstance().getReference(user).child(documentSnapshot0.getId()).child("Images").child(map.get("imagePath").toString()).getDownloadUrl()
                                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        Log.d("qwerty72", "success");
                                                        if (active0 == active){
                                                            realNote.setCoverPath(uri);
                                                            ((RealNote)notes.get(index1)).setCoverPath(uri);
                                                            mAdapter.notifyItemChanged(index1);
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("qwerty72", e.toString());
                                                        if (active0 == active){
                                                            if (index == -1) {
                                                                notes.add(realNote);
                                                                mAdapter.notifyItemInserted(notes.size() - 1);
                                                            } else {
                                                                notes.set(index, realNote);
                                                                mAdapter.notifyItemChanged(index);
                                                            }
                                                        }

                                                    }
                                                });
                                    }
                                }});
                        }


                    }
                });
    }
    public void insertById(final String id){
        generateNote(id, startPos, false);
    }
    public void changeById(final String id){
        for (int j = startPos; j < notes.size(); j++){
            final int i = j;
            if (notes.get(i).getID().equals(id)){
                generateNote(notes.get(i).getID(), i, true);
                break;
            }
        }
    }
    private void selectTitle(String title){
        db.collection("Notes").document(user).collection("userNotes").whereEqualTo("title", title).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null){
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                generateNote(documentSnapshot.getId(), -1, false);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("qwerty9", e.toString());
                    }
                });
    }
    public void deleteDirectory(String path){
        final String path1 = path;
        final File dir0 = new File(path);
        db.collection("User").document(user).collection("paths").whereEqualTo("parent", path1).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null){
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                deleteDirectory(documentSnapshot.getId());
                            }
                        }
                    }
                });
        db.collection("User").document(user).collection("paths").document(path1).delete();

        db.collection("Notes").document(user).collection("userNotes").whereEqualTo("path", path1).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null){
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                DeleteNote.deleteNote(user, documentSnapshot.getId());
                            }
                        }

                    }
                });
    }
    public void deleteSelectedDirectories(){
        for (Directory directory : selectionDirectoriesList){
            notes.remove(directory);
            deleteDirectory(directory.getDirectory().replace("/", "\\"));
            String s = directory.getDirectory();
            String parDoc = s.substring(0, s.substring(0, s.length() - 1).lastIndexOf("/")+1).replace("/", "\\");
            db.collection("User").document(user).collection("paths").document(parDoc).update("paths", FieldValue.arrayRemove(s.replace("/", "\\")));
        }
        selectionDirectoriesList.clear();
    }
//
    public void onFilterClick(ArrayList<String> checkedAuthors, ArrayList<String> checkedGenres, ArrayList<String> checkedGenresID, String ratingStart, String ratingEnd, boolean showCatalog) {
        noFilter = false;
        this.showCatalog = showCatalog;
        Log.d("qwerty433", "filter");
        this.checkedAuthors=checkedAuthors;
        this.checkedGenres=checkedGenres;
        for (int i = 0; i < notes.size(); i++){
            if (notes.get(i).getItemType() != 0){
                notes.get(i).setVisibility(showCatalog);
                Log.d("qwerty433", "catalog "+ showCatalog);
                continue;
            }
            if (!checkedAuthors.contains(((RealNote)notes.get(i)).getAuthor())){
                Log.d("qwerty433", "noAuthor");
                notes.get(i).setVisibility(false);
                continue;
            }
            boolean visible = false;
            for (String j : checkedGenresID){
                if (((RealNote)notes.get(i)).getGenre().get(j) != null){
                    visible=true;
                    break;
                }
            }
            if (visible){
                notes.get(i).setVisibility(true);
            }
            else{
                if (checkedGenres.contains("Не указан") && ((RealNote)notes.get(i)).getGenre().size()==0){
                    notes.get(i).setVisibility(true);
                }
                else{
                    notes.get(i).setVisibility(false);
                    continue;
                }

            }
            try{
                double start = 0.0, end = 5.0;
                if (!ratingStart.isEmpty()){
                    start = Double.parseDouble(ratingStart);
                }
                if (!ratingEnd.isEmpty()){
                    end = Double.parseDouble(ratingEnd);
                }
                if (((RealNote)notes.get(i)).getRating() < start || ((RealNote)notes.get(i)).getRating() > end){
                    notes.get(i).setVisibility(false);
                }
            }
            catch (Exception e){
                Log.e("ratingException", e.getMessage());
            }
            finally {
            }

        }
        mAdapter.notifyDataSetChanged();
    }


    public void onSortClick(int position) {
        sortType = position;
        Comparator<Note> comparator;
        final int minus;
        if (position % 2 == 1){
            minus = -1;
        }
        else{
            minus = 1;
        }
        if (position==0 || position==1){
            comparator = new Comparator<Note>() {
                @Override
                public int compare(Note o1, Note o2) {
                    if (o1.getItemType()==1 && o2.getItemType()==0){
                        return -1;
                    }
                    else if (o1.getItemType()==0 && o2.getItemType()==1){
                        return 1;
                    }
                    else if (o1.getItemType() == 0 && o2.getItemType() == 0){
                        return minus * ((RealNote)o1).getTitle().toLowerCase().compareTo(((RealNote)o2).getTitle().toLowerCase());
                    }
                    else{
                        return minus * ((Directory)o1).getDirectory().compareTo(((Directory)o2).getDirectory());
                    }
                }
            };
        }
        else if (position == 2 || position == 3){
            comparator = new Comparator<Note>() {
                @Override
                public int compare(Note o1, Note o2) {
                    if (o1.getItemType()==1 && o2.getItemType()==0){
                        return -1;
                    }
                    else if (o1.getItemType()==0 && o2.getItemType()==1){
                        return 1;
                    }
                    else if (o1.getItemType() == 0 && o2.getItemType() == 0){
                        return minus * ((RealNote)o1).getAuthor().toLowerCase().compareTo(((RealNote)o2).getAuthor().toLowerCase());
                    }
                    else{
                        return minus * ((Directory)o1).getDirectory().compareTo(((Directory)o2).getDirectory());
                    }
                }
            };
        }
        else if (position == 4 || position == 5){
            comparator = new Comparator<Note>() {
                @Override
                public int compare(Note o1, Note o2) {
                    if (o1.getItemType()==1 && o2.getItemType()==0){
                        return -1;
                    }
                    else if (o1.getItemType()==0 && o2.getItemType()==1){
                        return 1;
                    }
                    else if (o1.getItemType() == 0 && o2.getItemType() == 0){
                        if (((RealNote)o1).getRating() - ((RealNote)o2).getRating() > 0){
                            return minus;
                        }
                        else if (((RealNote)o1).getRating() - ((RealNote)o2).getRating() < 0)
                        {
                            return -minus;
                        }
                        return 0;
                    }
                    else{
                        return minus * ((Directory)o1).getDirectory().compareTo(((Directory)o2).getDirectory());
                    }
                }
            };
        }
        else{
            comparator =new Comparator<Note>() {
                @Override
                public int compare(Note o1, Note o2) {
                    if (o1.getItemType()==1 && o2.getItemType()==0){
                        return -1;
                    }
                    else if (o1.getItemType()==0 && o2.getItemType()==1){
                        return 1;
                    }
                    else if (o1.getItemType() == 0 && o2.getItemType() == 0){
                        if (((RealNote)o1).getTime() - ((RealNote)o2).getTime() > 0){
                            return minus;
                        }
                        else if (((RealNote)o1).getTime() - ((RealNote)o2).getTime() < 0){
                            return -minus;
                        }
                        return 0;
                    }
                    else{
                        return minus * ((Directory)o1).getDirectory().compareTo(((Directory)o2).getDirectory());
                    }
                }
            };
        }
        Collections.sort(notes, comparator);
        mAdapter.notifyDataSetChanged();


//        startSort();
    }

    @NonNull
    @Override
    public String toString() {
        return "catalog";
    }


    public void deleteSelectedRealNote(){
        for (int i = 0; i < selectionRealNotesList.size(); i++){
            String id = selectionRealNotesList.get(i).getID();
            notes.remove(selectionRealNotesList.get(i));
            mAdapter.notifyItemRemoved(i);
            DeleteNote.deleteNote(user, id);

        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        active++;
        if (data != null && requestCode == NOTE_REQUEST_CODE){
            // если изменился путь до записи, добавилась новая запись, то переходим к этой записи
            if (data.getExtras().get("deleted") != null){
                String id = data.getExtras().get("id").toString();
                int index = -1;
                for (int i = 0; i < notes.size(); i++){
                    if (notes.get(i).getID().equals(id)){
                        index = i;
                        break;
                    }
                }
                if (index != -1){
                    notes.remove(index);
                    mAdapter.notifyItemRemoved(index);
                }

            }

            else if (data.getExtras().get("path") != null){
                Log.d("qwerty544", "path not null");
                if (parent.equals(data.getExtras().get("path").toString().replace("\\", "/"))){
                    changeById(data.getExtras().get("id").toString());
                }
                else{
                    parent = data.getExtras().get("path").toString().replace("\\", "/");
                    reloadRecyclerView();
                    reloadButtonsView();
                }

            }
            else{
                changeById(data.getExtras().get("id").toString());
            }
        }


        if (data != null && data.getExtras() != null && data.getExtras().get("noNote") != null && requestCode==CREATE_NOTE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            parent = data.getExtras().get("path").toString();
            Log.d("Qwerty4567654", parent);
            reloadRecyclerView();
            reloadButtonsView();
        }
        else if (requestCode==CREATE_NOTE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null && data.getExtras().get("deleted")==null){
            Intent intent = new Intent(getContext(), NoteActivity.class); // вызов активности записи
            intent.putExtra("id", data.getExtras().get("id").toString()); // передаем id активности в бд, чтобы понять какую активность надо показывать
            intent.putExtra("changed", "true");
            insertById(data.getExtras().get("id").toString());
            startActivityForResult(intent, NOTE_REQUEST_CODE);

        }


    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onCatalogFragmentListener = (OnCatalogFragmentListener) context;
    }



    private void searchInDocuments(final String searched){
        class NoteItem implements Comparable<NoteItem>{
            String id;
            int difference;
            public NoteItem(String id, int diffence){
                this.id = id;
                this.difference = diffence;
            }

            @Override
            public int compareTo(NoteItem o) {
                return this.difference - o.difference;
            }
        }
        db.collection("Notes").document(user).collection("userNotes").document("allNotes").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot != null && documentSnapshot.getData() != null){
                    ArrayList<NoteItem> arrayList = new ArrayList<>();
                    for (String id : documentSnapshot.getData().keySet()){
                        arrayList.add(new NoteItem(id, levenshteinAlgorithm(searched, documentSnapshot.getData().get(id).toString())));
                    }
                    Collections.sort(arrayList);
                    ArrayList<String> notesID = new ArrayList<>();
                    notes.clear();
                    for (NoteItem noteItem : arrayList) {
                        if (noteItem.difference <= searched.length()) {
                            notesID.add(noteItem.id);
                            notes.add(null);
                        }
                        else {
                            break;
                        }
                    }
                    for (int i = 0; i <notesID.size(); i++){
                        generateNote(notesID.get(i), i, true);
                    }

                }
            }
        });

    }

    private int levenshteinAlgorithm(String searched, String saved){
        searched = searched.toLowerCase();
        saved = saved.toLowerCase();
        if (searched.equals(saved)){
            return -100000;
        }
        if (saved.contains(searched)){
            return -searched.length();
        }


        if (searched.length() < saved.length()){
            String temp = saved;
            saved = searched;
            searched = temp;
        }
        char[] s = searched.toCharArray();
        char[] t = saved.toCharArray();
        int m = s.length;
        int n = t.length;
        int[] current = new int[n+1];
        for (int i = 0; i < n + 1; i++){
            current[i] = i;
        }
        for (int i = 1; i < m + 1; i++){
            int[] previous = current.clone();
            current = new int[n+1];
            current[0] = i;
//            Log.d("qwerty5554433", searched + " " + saved + " " + current.length + " " + n + " " + m);
            for (int j = 1; j < n + 1; j++){
                int change = previous[j - 1];
                if (s[i-1]!=t[j-1]){
                    change++;
                }
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), change);
            }
        }
        return current[n];
    }


}