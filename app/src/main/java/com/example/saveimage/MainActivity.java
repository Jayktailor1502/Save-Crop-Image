package com.example.saveimage;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Objects;

import static android.os.Environment.DIRECTORY_PICTURES;
import static androidx.core.content.FileProvider.getUriForFile;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    public static long fileName;
    OutputStream fos;
    Uri picUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.photoView);
    }

    private void saveToGallery(Bitmap bitmap) {
        try{
            fileName = System.currentTimeMillis();
            ContentResolver contentResolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG"+fileName + ".jpg");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, DIRECTORY_PICTURES + File.separator + "DemoFolder");
            Uri imageURI = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            fos = contentResolver.openOutputStream(Objects.requireNonNull(imageURI));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Objects.requireNonNull(fos);
            fos.flush();
            fos.close();
            Toast.makeText(MainActivity.this, "Image Saved", Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            Toast.makeText(MainActivity.this, "Image Not Saved", Toast.LENGTH_LONG).show();
            Log.e("Hello","Exception - " + e);
        }
    }

    ActivityResultLauncher<Intent> launchCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK) {
                          Intent data = result.getData();
                          assert data != null;
                          Bitmap photo1 = (Bitmap) data.getExtras().get("data");
                          saveToGallery(photo1);
//                          imageView.setImageBitmap(photo1);
                          picUri = data.getData();
                          cropImage(picUri);
                    }
                }
            });

    ActivityResultLauncher<Intent> launchCrop = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        Bitmap photo1 = (Bitmap) data.getExtras().get("data");
//                        saveToGallery(photo1);
                        imageView.setImageBitmap(photo1);
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> launchGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        Uri selectedImageUri = data.getData();
                        imageView.setImageURI(selectedImageUri);
                    }
                }
            }
    );

    private void cropImage(Uri picUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(picUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        intent.putExtra("crop", "true");
        intent.putExtra("scale","true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);
        intent.putExtra("return-data", true);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
//        String mImageUri = String.valueOf(picUri);
//        Uri mCroppedImage = picUri;
        launchCrop.launch(intent);
    }

    public void takePictureFromCamera(View view) {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        launchCamera.launch(takePictureIntent);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        if(permissionDeniedResponse.isPermanentlyDenied()) {
                            Toast.makeText(MainActivity.this,"Please allow permissions",Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }


    public void takePictureFromGallery(View view) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launchGallery.launch(pickPhoto);
    }
}