package com.example.readingdiary.Classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SaveImage {
    // не помню где нашла способ сжатия без загрузки. Там был пример для файла, я переделала для uri
    public static Bitmap decodeSampledBitmapFromResource(Uri imageUri,
                                                  int reqWidth, int reqHeight, Context context) throws Exception{

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
        BitmapFactory.decodeStream(imageStream, null, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        options.inJustDecodeBounds = false;
        imageStream.close();
        imageStream = context.getContentResolver().openInputStream(imageUri);
        return BitmapFactory.decodeStream(imageStream, null, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = (int) Math.max((double)height / reqHeight, (double)width/reqWidth);
        return inSampleSize;
    }
    // Это уже на все 100% наше
    public static Bitmap saveImage(String user, String id, final Uri imageUri, long time1, Context context) {
        try{
            final StorageReference imageStorage = FirebaseStorage.getInstance().getReference(user).child(id).child("Images");
            final DocumentReference imagePathsDoc = FirebaseFirestore.getInstance().collection("Common").document(user).collection(id).document("Images");
            int px = 600;
            Bitmap cover = decodeSampledBitmapFromResource(imageUri, px, px, context); // файл сжимается
            final long time = time1;
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            cover.compress(Bitmap.CompressFormat.PNG, 100, stream);// сохранение
            Map<String, Boolean> map = new HashMap<>();
            map.put(time+"", false);
            imagePathsDoc.set(map, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            imageStorage.child(time + "").putBytes(stream.toByteArray())
                                    .addOnSuccessListener(
                                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    imagePathsDoc.update(time+"", true);

                                                }
                                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            imagePathsDoc.update(time+"", FieldValue.delete());

                                        }
                                    });
                        }});
            return cover;
        }
        catch (Exception e){
            Log.e("saveImageException", e.toString());
        }
        return null;

    }
}
