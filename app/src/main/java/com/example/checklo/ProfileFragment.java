package com.example.checklo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.checklo.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ProfileActivity";

    private int REQUEST_CHECK = 1001;

    private ImageView profileImage, nextButtonImage;
    private TextView userName, userIntroduction;
    private LinearLayout signoutLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profileImageView);
        nextButtonImage = view.findViewById(R.id.nextButtonImage);
        nextButtonImage.setImageResource(R.drawable.next_image);

        signoutLayout = view.findViewById(R.id.signoutView);

        retrieveProfileImage();

        userName = view.findViewById(R.id.profileNameTextView);
        userName.setText(((UserClient)getActivity().getApplicationContext()).getUser().getUsername());

        userIntroduction = view.findViewById(R.id.myIntroductionTextView);
        userIntroduction.setText(((UserClient)getActivity().getApplicationContext()).getUser().getUserintroduction());

        ImageView settingImage = view.findViewById(R.id.settingImageView);

        profileImage.setOnClickListener(this);
        settingImage.setOnClickListener(this);
        signoutLayout.setOnClickListener(this);

        return view;
    }

    private void retrieveProfileImage(){
        if (isAdded()){
            String avatar = "";
            try{

                avatar = ((UserClient)getActivity().getApplicationContext()).getUser().getAvatar();

            }catch (NumberFormatException e){
                Log.e(TAG, "retrieveProfileImage: no avatar image. Setting default. " + e.getMessage() );
            }

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReferenceFromUrl("gs://checklo-ae99a.appspot.com").child("Profile/" + avatar + ".png");
            Log.d(TAG, "Storage Url :" + storageReference);
            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(isAdded()){
                        if (task.isSuccessful()){

                            Glide.with(getActivity())
                                    .load(task.getResult())
                                    .into(profileImage);

                            Toast.makeText(getActivity().getApplicationContext(), "다운로드 완료!", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getActivity().getApplicationContext(), "태스크 실패!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity().getApplicationContext(), "다운로드 실패!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.settingImageView :{
                goEditProfile();
                break;
            }
            case R.id.profileImageView:{
                goEditProfile();
                break;
            }

            case R.id.signoutView:{
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHECK){
            if (resultCode == RESULT_OK){
                retrieveProfileImage();
            } else {

            }
        }
    }

    private String getURLForResource(int resId) {
        return Uri.parse("android.resource://" + R.class.getPackage().getName() + "/" + resId).toString();
    }

    public void goEditProfile(){
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        startActivityForResult(intent, REQUEST_CHECK);
    }
}
