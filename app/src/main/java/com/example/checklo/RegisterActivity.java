package com.example.checklo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.checklo.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import static android.text.TextUtils.isEmpty;
import static com.example.checklo.util.Check.doStringsMatch;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "RegisterActivity";

    //widgets
    private EditText mEmail, mPassword, mConfirmPassword;
    private ProgressBar mProgressBar;

    private FirebaseFirestore mDb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmail = (EditText) findViewById(R.id.input_email);
        mPassword = (EditText) findViewById(R.id.input_password);
        mConfirmPassword = (EditText) findViewById(R.id.input_confirm_password);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        findViewById(R.id.btn_register).setOnClickListener(this);

        mDb = FirebaseFirestore.getInstance();
    }

    public void registerNewEmail(final String email, String password){
        Log.d(TAG, "registerNewEmail: start register");
        showDialog();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: AuthState: " + FirebaseAuth.getInstance().getCurrentUser().getUid());

                            User user = new User();
                            user.setEmail(email);
                            user.setUsername(email.substring(0, email.indexOf("@")));
                            user.setUser_id(FirebaseAuth.getInstance().getUid());

                            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build();

                            mDb.setFirestoreSettings(settings);

                            DocumentReference newUserRef = mDb.collection("Users").document(FirebaseAuth.getInstance().getUid());

                            newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    hideDialog();

                                    if(task.isSuccessful()){
                                        redirectLoginScreen();
                                    }else{
                                        View parentLayout = findViewById(android.R.id.content);
                                        Snackbar.make(parentLayout, "무엇인가 잘 못 되었습니다.", Snackbar.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }else{
                            View parentLayout = findViewById(android.R.id.content);
                            Snackbar.make(parentLayout, "무엇인가 잘 못 되었습니다.", Snackbar.LENGTH_SHORT).show();
                            hideDialog();
                        }
                    }
                });
    }

    private void redirectLoginScreen(){
        Log.d(TAG, "redirectLoginScreen: redirecting to login screen.");

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_register:{
                Log.d(TAG, "onClick: attempting to register.");

                //check for null valued EditText fields
                if(!isEmpty(mEmail.getText().toString())
                        && !isEmpty(mPassword.getText().toString())
                        && !isEmpty(mConfirmPassword.getText().toString())){

                    //check if passwords match
                    if(doStringsMatch(mPassword.getText().toString(), mConfirmPassword.getText().toString())){
                        //Initiate registration task
                        registerNewEmail(mEmail.getText().toString(), mPassword.getText().toString());
                    }else{
                        Toast.makeText(RegisterActivity.this, "비밀번호 일치하게 입력해주세요", Toast.LENGTH_SHORT).show();
                    }


                }else{
                    Toast.makeText(RegisterActivity.this, "모든 정보를 기입해주세요!", Toast.LENGTH_SHORT).show();
                }
                break;

            }
        }

    }
}
