package com.example.checklo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.checklo.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.model.Document;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "EditProfileActivity";

    private ImageView profileImage;
    private EditText userName;
    private EditText introductiontext;
    private FirebaseFirestore mdb;

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileImage = findViewById(R.id.profileImageView);

        retrieveProfileImage();
        retrieveProfiledata();

        ImageView changeImage = findViewById(R.id.changePictureImageView);
        changeImage.setImageResource(R.drawable.camera);
        ImageView backImage = findViewById(R.id.backBarImageView);
        backImage.setImageResource(R.drawable.back_button);
        TextView saveIntroduction = findViewById(R.id.introductionSaveTextView);
        TextView savename = findViewById(R.id.nameSaveTextView);

        changeImage.setOnClickListener(this);
        backImage.setOnClickListener(this);
        profileImage.setOnClickListener(this);
        saveIntroduction.setOnClickListener(this);
        savename.setOnClickListener(this);

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
    }

    private void retrieveProfiledata(){
        userName = findViewById(R.id.profileNameTextView);
        introductiontext = findViewById(R.id.introductionInputText);

        userName.setText(((UserClient)getApplicationContext()).getUser().getUsername());
        introductiontext.setText(((UserClient)getApplicationContext()).getUser().getUserintroduction());
    }

    private void retrieveProfileImage(){
        String avatar = "";
        try{

            avatar = ((UserClient)getApplicationContext()).getUser().getAvatar();

        }catch (NumberFormatException e){
            Log.e(TAG, "retrieveProfileImage: no avatar image. Setting default. " + e.getMessage() );
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl("gs://checklo-ae99a.appspot.com").child("Profile/" + avatar + ".png");
        Log.d(TAG, "Storage Url :" + storageReference);
        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Glide
                            .with(EditProfileActivity.this)
                            .load(task.getResult())
                            .thumbnail(0.5f)
                            .into(profileImage);

                }else {
                    Toast.makeText(getApplicationContext(), "태스크 실패!", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "다운로드 실패!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void changePicture(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_CODE);
                }

            } else {
                //TODO 버젼이 M보다 낮은 경우 바로 여기로 넘어가는지 확인해야함
                pickImageFromGallery();
            }
        } else {
            pickImageFromGallery();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CODE:{
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void pickImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                RequestOptions requestOptions = new RequestOptions()
                        .error(R.drawable.profile_default)
                        .placeholder(R.drawable.profile_default);

                Glide.with(this).setDefaultRequestOptions(requestOptions)
                        .load(resultUri)
                        .into(profileImage);

                FirebaseStorage storage = FirebaseStorage.getInstance();

                StorageReference storageReference = storage.getReferenceFromUrl("gs://checklo-ae99a.appspot.com")
                        .child("Profile/" + ((UserClient)getApplicationContext()).getUser().getUser_id() + ".png");

                storageReference.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                    }
                });
            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.profileImageView:{
                changePicture();
                break;
            }
            case R.id.changePictureImageView:{
                changePicture();
                break;
            }
            case R.id.backBarImageView:{
                onBackPressed();
                break;
            }
            case R.id.nameSaveTextView:{

                EditText editText = findViewById(R.id.profileNameTextView);

                if (editText.getText().toString().length() != 0) {
                    mdb = FirebaseFirestore.getInstance();
                    DocumentReference usersRef = mdb.collection(getString(R.string.collection_users)).document(FirebaseAuth.getInstance().getUid());

                    User user = new User();
                    user.setUsername(editText.getText().toString());

                    usersRef.update("username", user.getUsername()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getApplicationContext(), "변경 완료!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            }
            case R.id.introductionSaveTextView:{
                EditText editText = findViewById(R.id.introductionInputText);

                if (editText.getText().toString().length() != 0){
                    mdb = FirebaseFirestore.getInstance();
                    DocumentReference userRef = mdb.collection(getString(R.string.collection_users)).document(FirebaseAuth.getInstance().getUid());

                    User user = new User();
                    user.setUserintroduction(editText.getText().toString());

                    userRef.update("userintroduction", user.getUserintroduction()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getApplicationContext(), "변경 완료!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            }
        }
    }
}
