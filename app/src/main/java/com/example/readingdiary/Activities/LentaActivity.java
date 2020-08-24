//package com.example.readingdiary.Activities;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.DefaultItemAnimator;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ProgressBar;
//
//import com.example.readingdiary.Classes.RealNote;
//import com.example.readingdiary.R;
//import com.example.readingdiary.adapters.PostAdapter;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.FirebaseFirestoreException;
//import com.google.firebase.firestore.SetOptions;
//import com.google.firebase.firestore.Transaction;
//import com.google.firebase.storage.FirebaseStorage;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.TreeMap;
//
//public class LentaActivity extends AppCompatActivity
//{
//    Button bUpdateLent;
//    RecyclerView rvPosts;
//    PostAdapter postAdapter;
//    ProgressBar progressBar;
//    ArrayList<String> subUsersId;
//    String user;
//    FirebaseFirestore db = FirebaseFirestore.getInstance();
//    ArrayList<RealNote> notes=new ArrayList<>();
//    ArrayList<String[]> notesRef=new ArrayList<>();
//    long action=0;
//    boolean load=false;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_lenta);
//        bUpdateLent = findViewById(R.id.bUpdateLent);
//        rvPosts = findViewById(R.id.rvPosts);
//        progressBar =findViewById(R.id.progressBar);
//        postAdapter = new PostAdapter(notes);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
//        rvPosts.setAdapter(postAdapter);
//        rvPosts.setLayoutManager(layoutManager);
//        rvPosts.setItemAnimator(itemAnimator);
//        progressBar.setVisibility(View.INVISIBLE);
//        user = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        db.collection("Subscriptions").document(user).get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
//                {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot)
//                    {
//                        if (documentSnapshot != null && documentSnapshot.getData() != null)
//                        {
//                            subUsersId = new ArrayList<String>(documentSnapshot.getData().keySet());
//                            chooseNotes();
//                        }
//
//                    }
//                });
//
//        postAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener()
//        {
//            @Override
//            public void onItemClick(int position)
//            {
//                Intent intent = new Intent(LentaActivity.this, NoteActivity.class);
//                intent.putExtra("security", "guest");
//                intent.putExtra("owner", notes.get(position).getOwner());
//                intent.putExtra("id", notes.get(position).getID());
//                startActivity(intent);
//            }
//
//            @Override
//            public void onRatingChanged(final int position, final float rating)
//            {
//                if (notes.get(position).getOwner()==null)
//                {
//                    return;
//                }
//                Log.d("qwerty57", "hi");
//                HashMap<String, HashMap> hashMap0 = new HashMap<>();
//                HashMap<String, Double> hashMap= new HashMap<>();
//                hashMap.put(notes.get(position).getID(), (double)rating);
//                hashMap0.put(notes.get(position).getOwner(), hashMap);
//                db.collection("UsersRating").document(user).set(hashMap0, SetOptions.merge());
//                db.runTransaction(new Transaction.Function<Number[]>()
//                {
//
//                    @Nullable
//                    @Override
//                    public Number[] apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
//                        DocumentReference noteRef = db.collection("Notes").document(notes.get(position).getOwner()).
//                                collection("userNotes").document(notes.get(position).getID());
//                        DocumentSnapshot noteSnapshot = transaction.get(noteRef);
//                        double newSum = noteSnapshot.getDouble("publicRatingSum") - notes.get(position).getRating() + rating;
//                        long newCount = noteSnapshot.getLong("publicRatingCount");
//                        if (notes.get(position).getRating() == 0.0)
//                        {
//                            newCount++;
//                            transaction.update(noteRef, "publicRatingCount", newCount);
//                        }
//                        transaction.update(noteRef, "publicRatingSum", newSum);
//                        return new Number[]{newSum, newCount};
//                    }
//                }).addOnSuccessListener(new OnSuccessListener<Number[]>()
//                {
//                    @Override
//                    public void onSuccess(Number[] longs)
//                    {
//                        if (notes.size() > position)
//                        {
//                            notes.get(position).setPublicRatingSum((double)longs[0]);
//                            notes.get(position).setPublicRatingCount((long)longs[1]);
//                            notes.get(position).setRating(rating);
//                            postAdapter.notifyItemChanged(position);
//                        }
//
//                    }
//                });
//            }
//        });
//
//        final Handler uiHandler = new Handler();
//
//        final Runnable makeLayoutGone = new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                bUpdateLent.setClickable(true);
//            }
//        };
//
//        bUpdateLent.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                if (subUsersId != null && subUsersId.size()!=0&& load==true)
//                {
//                    bUpdateLent.setClickable(false);
//                    uiHandler.postDelayed(makeLayoutGone, 500);
//                    action++;
//                    notes.clear();
//                    notesRef.clear();
//                    chooseNotes();
//                }
//
//            }
//        });
//
//
//
//
//    }
//
//    public void showNotes()
//    {
//        Log.d("qwerty58", "show");
//        final long curAction = action;
//        db.collection("UsersRating").document(user).get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
//                {
//                @Override
//                public void onSuccess(DocumentSnapshot documentSnapshot)
//                {
//                    final Map<String, Object> userRatingMap;
//                    if (documentSnapshot!=null)
//                    {
//                        userRatingMap = documentSnapshot.getData();
//                    }
//                    else
//                    {
//                        userRatingMap =null;
//                    }
//                    notes.clear();
//                    for (int i = 0; i < notesRef.size(); i++)
//                    {
//                        notes.add(new RealNote("", "", "", "", 0.0, false, 0.0, 0));
//                    }
//                    postAdapter.notifyDataSetChanged();
//                    for (int i = 0; i < notesRef.size(); i++)
//                    {
//                        final String[] arr = notesRef.get(i);
//                        final int j = i;
//                        db.collection("Notes").document(arr[0]).collection("userNotes").document(arr[1]).get()
//                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
//                                {
//                                    @Override
//                                    public void onSuccess(DocumentSnapshot documentSnapshot)
//                                    {
//                                        final HashMap map = (HashMap) documentSnapshot.getData();
//                                        if (map != null){
//                                            final double rating;
//                                            if (userRatingMap != null &&
//                                                    userRatingMap.containsKey(arr[0])
//                                                    && ((Map<String, Double>)userRatingMap.get(arr[0])).containsKey(arr[1]))
//                                            {
//                                                rating = Double.valueOf(((Map<String, Double>)userRatingMap.get(arr[0])).get(arr[1]));
//                                            }
//                                            else
//                                            {
//                                                rating=0.0;
//                                            }
//                                            if (map.get("imagePath")!= null && !map.get("imagePath").toString().equals("")){
//                                                FirebaseStorage.getInstance().getReference(arr[0]).child(arr[1]).child("Images").child(map.get("imagePath").toString()).getDownloadUrl()
//                                                        .addOnSuccessListener(new OnSuccessListener<Uri>()
//                                                        {
//                                                            @Override
//                                                            public void onSuccess(Uri uri)
//                                                            {
//                                                                if (curAction!=action)
//                                                                {
//                                                                    return;
//                                                                }
//                                                                else
//                                                                {
//                                                                    final RealNote realNote = new RealNote(arr[1], map.get("path").toString(), map.get("author").toString(),
//                                                                            map.get("title").toString(), rating, (boolean)map.get("private"), (double)map.get("publicRatingSum"), (long)map.get("publicRatingCount"));
//                                                                    realNote.setOwner(arr[0]);
//                                                                    realNote.setCoverPath(uri);
//                                                                    int index = -1;
//                                                                    Log.d("qwerty52", realNote.getID());
//                                                                    if (curAction!=action)
//                                                                    {
//                                                                        return;
//                                                                    }
//                                                                    notes.set(j, realNote);
//                                                                    postAdapter.notifyItemChanged(j);
//                                                                }
//
//                                                            }
//                                                        })
//                                                        .addOnFailureListener(new OnFailureListener()
//                                                        {
//                                                            @Override
//                                                            public void onFailure(@NonNull Exception e)
//                                                            {
//                                                                if (curAction!=action)
//                                                                {
//                                                                    return;
//                                                                }
//                                                                else
//                                                                {
//                                                                    final RealNote realNote = new RealNote(arr[1], map.get("path").toString(), map.get("author").toString(),
//                                                                            map.get("title").toString(), rating, (boolean)map.get("private"), (double)map.get("publicRatingSum"), (long)map.get("publicRatingCount"));
//                                                                    realNote.setOwner(arr[0]);
//                                                                    if (curAction!=action)
//                                                                    {
//                                                                        return;
//                                                                    }
//                                                                    notes.set(j, realNote);
//                                                                    postAdapter.notifyItemChanged(j);
//                                                                }
//
////
//
//                                                            }
//                                                        });
//                                            }
//                                            else
//                                            {
//                                                if (curAction!=action)
//                                                {
//                                                    return;
//                                                }
//                                                else
//                                                {
//                                                    final RealNote realNote = new RealNote(arr[1], map.get("path").toString(), map.get("author").toString(),
//                                                            map.get("title").toString(), rating, (boolean)map.get("private"), (double)map.get("publicRatingSum"), (long)map.get("publicRatingCount"));
//                                                    realNote.setOwner(arr[0]);
//                                                    if (curAction!=action)
//                                                    {
//                                                        return;
//                                                    }
//                                                    notes.set(j, realNote);
//                                                    postAdapter.notifyItemChanged(j);
//                                                }
//                                            }
//                                        }
//                                    }
//                                });
//                    }
//
//
//                }
//            });
//
//
//        load=true;
//    }
//
//    public void chooseNotes()
//    {
//        final long curAction = action;
//        final TreeMap<Long, ArrayList<String[]>> treeMap = new TreeMap<>();
//        final int[] arr = {0, subUsersId.size()};
//        Log.d("qwerty58", "chooseNotes " + subUsersId.size());
//        for (final String subUserId : subUsersId){
//            db.collection("Publicly").document(subUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                @Override
//                public void onSuccess(DocumentSnapshot documentSnapshot) {
//                    HashMap<String, String> map = (HashMap) documentSnapshot.getData();
//                    if (map != null) {
//                        for (String key : map.keySet()) {
//                            if (treeMap.containsKey(-Long.parseLong(key))) {
//                                treeMap.get(-Long.parseLong(key)).add(new String[]{subUserId, map.get(key)});
//                            } else {
//                                ArrayList<String[]> arrayList = new ArrayList<>();
//                                arrayList.add(new String[]{subUserId, map.get(key)});
//                                treeMap.put(-Long.parseLong(key), arrayList);
//                            }
//                        }
//                    }
//                    Log.d("qwerty58", ""+arr[0]);
//                    arr[0]++;
//                    if (arr[0] == arr[1]){
//                        int count=0;
//                        for (Long key : treeMap.keySet()){
//                            if (count >= 20){
//                                break;
//                            }
//                            for (String[] q : treeMap.get(key)){
//                                notesRef.add(q);
//                                count++;
//                                Log.d("qwerty54", q[0] + "  "+ q[1]);
//                                if (count >= 20){
//                                    break;
//                                }
//                            }
//                        }
//                        Log.d("qwerty58", "curAct " + curAction + " " + action);
//                        if (curAction == action){
//                            showNotes();
//                        }
//
//
//
//                    }
//                }
//            });
//        }
//    }
//
//
//}
