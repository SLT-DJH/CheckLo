package com.example.checklo;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MapActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Fragment mapfragment = new MapFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mapfragment).commit();

        ImageView profile = (ImageView) findViewById(R.id.profileMenuButton);
        profile.setImageResource(R.drawable.profile);

        ImageView location = (ImageView) findViewById(R.id.locationMenuButton);
        location.setImageResource(R.drawable.location_clicked);

        ImageView chatboard = (ImageView) findViewById(R.id.chatboardMenuButton);
        chatboard.setImageResource(R.drawable.chat);

        profile.setOnClickListener(this);
        location.setOnClickListener(this);
        chatboard.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.profileMenuButton:{
                Fragment profilefragment = new ProfileFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.container, profilefragment).commit();

                ImageView profile = (ImageView) findViewById(R.id.profileMenuButton);
                profile.setImageResource(R.drawable.profile_click);

                ImageView location = (ImageView) findViewById(R.id.locationMenuButton);
                location.setImageResource(R.drawable.location);

                ImageView chatboard = (ImageView) findViewById(R.id.chatboardMenuButton);
                chatboard.setImageResource(R.drawable.chat);

                break;

            }
            case R.id.locationMenuButton:{
                Fragment mapfragment = new MapFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.container, mapfragment).commit();

                ImageView profile = (ImageView) findViewById(R.id.profileMenuButton);
                profile.setImageResource(R.drawable.profile);

                ImageView location = (ImageView) findViewById(R.id.locationMenuButton);
                location.setImageResource(R.drawable.location_clicked);

                ImageView chatboard = (ImageView) findViewById(R.id.chatboardMenuButton);
                chatboard.setImageResource(R.drawable.chat);

                break;

            }
            case R.id.chatboardMenuButton:{
                Fragment chatboardfragment = new ChatboardFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.container, chatboardfragment).commit();

                ImageView profile = (ImageView) findViewById(R.id.profileMenuButton);
                profile.setImageResource(R.drawable.profile);

                ImageView location = (ImageView) findViewById(R.id.locationMenuButton);
                location.setImageResource(R.drawable.location);

                ImageView chatboard = (ImageView) findViewById(R.id.chatboardMenuButton);
                chatboard.setImageResource(R.drawable.chat_clicked);

                break;
            }
        }
    }
}
