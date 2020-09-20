package com.example.checklo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.text.TextUtils.isEmpty;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LoginActivity";

    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText mEmail, mPassword;
    private Button mEmailLogin, facebookLogin, googleLogin, kakaoLogin, naverLogin;
    private ImageView facebook, kakao, naver, google, logo;
    private TextView registerLink;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupFirebaseAuth();

        setContentView(R.layout.activity_login);
        //EditText
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);

        //Button
        mEmailLogin = findViewById(R.id.emailLoginButton);
        facebookLogin = findViewById(R.id.facebookLoginButton);
        googleLogin = findViewById(R.id.googleLoginButton);
        kakaoLogin = findViewById(R.id.kakaoLoginButton);
        naverLogin = findViewById(R.id.naverLoginButton);

        //Image
        facebook = findViewById(R.id.facebookImageView);
        facebook.setImageResource(R.drawable.facebook);

        kakao = findViewById(R.id.kakaoImageView);
        kakao.setImageResource(R.drawable.kakao);

        google = findViewById(R.id.googleImageView);
        google.setImageResource(R.drawable.google);

        naver = findViewById(R.id.naverImageView);
        naver.setImageResource(R.drawable.naver);

        logo = findViewById(R.id.logoImageView);
        logo.setImageResource(R.drawable.logo);

        //onClick
        registerLink = findViewById(R.id.registerLinkText);
        registerLink.setOnClickListener(this);
        mEmailLogin = findViewById(R.id.emailLoginButton);
        mEmailLogin.setOnClickListener(this);

        mProgressBar = findViewById(R.id.progressBar);


    }

    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: started");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(LoginActivity.this, "인증 되셨습니다.: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    //User signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }

    private void signin(){
        if(!isEmpty(mEmail.getText().toString())
                && !isEmpty(mPassword.getText().toString())){
            Log.d(TAG, "onClick: attempting to authenticate.");

            showDialog();

            FirebaseAuth.getInstance().signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            hideDialog();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this, "인증 실패", Toast.LENGTH_SHORT).show();
                    hideDialog();
                }
            });
        }else{
            Toast.makeText(LoginActivity.this, "모든 사항을 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.registerLinkText:{
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.emailLoginButton:{
                signin();
                break;
            }
        }

    }
}
