package com.example.readingdiary.Classes;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DeleteNote {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void deleteDirectory(final String user, final String path){
        final String path1 = path;
        final File dir0 = new File(path);
        db.collection("User").document(user).collection("paths").whereEqualTo("parent", path1).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null){
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                deleteDirectory(user, documentSnapshot.getId());
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
                                deleteNote(user, documentSnapshot.getId());
                                if (!(boolean)documentSnapshot.get("private")){
                                }
                            }
                        }

                    }
                });
    }

    public static void deleteNote(String user, String id){
        db.collection("Notes").document(user).collection("userNotes").document("allNotes").update(id, FieldValue.delete());
        db.collection("Notes").document(user).collection("userNotes").document(id).delete();
        deleteImages(user, id);
        deleteVariousNote(user, id, "comment");
        deleteVariousNote(user, id, "description");
        deleteVariousNote(user, id, "quotes");
    }



    public static void deleteVariousNote(String user, String id, final String type){
        final CollectionReference collectionReference = db.collection("VariousNotes").document(user).collection(id);
        collectionReference.document(type).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Boolean> map = (HashMap) documentSnapshot.getData();
                        if (map != null){
                            for (String path : map.keySet()){
                                collectionReference.document(path).delete();
                            }
                        }
                        collectionReference.document(type).delete();
                    }
                });
    }

    public static void deleteImages(String user, String id) {
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference(user).child(id);
        final DocumentReference documentReference = db.collection("Common").document(user).collection(id).document("Images");
        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Boolean> map = (HashMap) documentSnapshot.getData();
                        Log.d("qwerty41", "hi1");

                        if (map != null) {
                            Log.d("qwerty41", "hi2");

                            for (String path : map.keySet()) {
                                Log.d("qwerty41", "hi3 " + path);

                                storageReference.child("Images").child(path).delete();
                            }
                        }
                        documentReference.delete();

                    }
                });
    }





    public static void deleteSelectedImages(String user, String noteID, ArrayList<Long> imagesID){
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference(user).child(noteID).child("Images");
        final DocumentReference documentReference = db.collection("Common").document(user).collection(noteID).document("Images");

        for (long i : imagesID) {
            documentReference.update(i + "", FieldValue.delete());
            storageReference.child(i+"").delete();
            Log.d("qwerty121", "storage " +  user + " " + noteID + " " + i);
        }
    }

}
