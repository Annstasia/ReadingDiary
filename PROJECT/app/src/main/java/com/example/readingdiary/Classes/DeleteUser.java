package com.example.readingdiary.Classes;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DeleteUser {
    public static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void deleteUser(final Context context, final String user){
        final boolean[] arr = new boolean[1];
        arr[0] = false;
        FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    db.collection("PublicID").document(user).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot != null && documentSnapshot.get("id") != null){
                                final String name = documentSnapshot.get("id").toString();
                                db.runTransaction(new Transaction.Function<Boolean>() {
                                    @Nullable
                                    @Override
                                    public Boolean apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                        DocumentReference documentReference = db.collection("allNames").document("allNames");
                                        DocumentSnapshot allNamesShapshot = transaction.get(documentReference);
                                        if (allNamesShapshot != null && allNamesShapshot.getString("names")!=null){
                                            ArrayList<String> names=new ArrayList<>();
                                            Collections.addAll(names, allNamesShapshot.getString("names").split(" "));
                                            names.remove(name);
                                            Map<String, String> map = new HashMap<>();
                                            map.put("names", names.stream().collect(Collectors.joining(" ")));
//                                             allNamesShapshot.set(map);
                                            transaction.set(documentReference, map);
                                        }
                                        return true;
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean aBoolean) {
                                        db.collection("PublicID").document(user).delete();
                                        DeleteNote.deleteDirectory(user, ".\\");
                                    }
                                });


                            }
                        }
                    });
                }
                else {
                    Log.e("qwerty70", task.getException().toString());
                    Toast.makeText(context, "Что-то пошло не так. Попробуйте заново авторизоваться, чтобы удалить аккаунт", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

