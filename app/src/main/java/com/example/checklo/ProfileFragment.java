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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.checklo.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
        RequestOptions requestOptions = new RequestOptions()
                .error(R.drawable.profile_default)
                .placeholder(R.drawable.profile_default);

        String avatar = "";
        try{
            avatar =((UserClient)getActivity().getApplicationContext()).getUser().getAvatar();
            Log.d(TAG, "getAvatar: " + ((UserClient)getActivity().getApplicationContext()).getUser().getAvatar());
            if (avatar == null) {
                String drawablePath = getURLForResource(R.drawable.profile_default);

                User user = ((UserClient)getActivity().getApplicationContext()).getUser();
                user.setAvatar(drawablePath);

                FirebaseFirestore.getInstance()
                        .collection(getString(R.string.collection_users))
                        .document(FirebaseAuth.getInstance().getUid())
                        .set(user);

                avatar = drawablePath;
            }

        }catch (NumberFormatException e){
            Log.e(TAG, "retrieveProfileImage: no avatar image. Setting default. " + e.getMessage() );
        }

        Glide.with(getActivity())
                .setDefaultRequestOptions(requestOptions)
                .load(Uri.parse(avatar))
                .into(profileImage);
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
