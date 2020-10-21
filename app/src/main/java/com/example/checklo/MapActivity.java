package com.example.checklo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.checklo.models.User;
import com.example.checklo.models.UserLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import static com.example.checklo.Constants.ERROR_DIALOG_REQUEST;
import static com.example.checklo.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.checklo.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MapActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MapActivity";
    private static int status = 1000;

    //widgets
    private User mUser;

    //vars
    private ListenerRegistration mUserEventListener;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation mUserLocation;
    private FirebaseFirestore mDb;
    private ArrayList<UserLocation> mUserLocationList = new ArrayList<>();
    private ArrayList<User> mUserList = new ArrayList<>();
    private Fragment profileFragment, mapFragment, chatboardFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mDb = FirebaseFirestore.getInstance();

        getUsers();

        chatboardFragment = new ChatboardFragment();
        profileFragment = new ProfileFragment();
        mapFragment = new MapFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, chatboardFragment).commit();

        ImageView profile = (ImageView) findViewById(R.id.profileMenuButton);
        profile.setImageResource(R.drawable.profile);

        ImageView location = (ImageView) findViewById(R.id.locationMenuButton);
        location.setImageResource(R.drawable.location);

        ImageView chatboard = (ImageView) findViewById(R.id.chatboardMenuButton);
        chatboard.setImageResource(R.drawable.chat_clicked);

        profile.setOnClickListener(this);
        location.setOnClickListener(this);
        chatboard.setOnClickListener(this);

    }

    private void getUsers(){

       CollectionReference usersRef = mDb
                .collection(getString(R.string.collection_users));

        mUserEventListener = usersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "onEvent: Listen failed.", e);
                    return;
                }

                if(queryDocumentSnapshots != null){

                    // Clear the list and add all the users again
                    mUserList.clear();
                    mUserList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        mUserList.add(user);
                        getUserLocation(user);
                    }

                    Log.d(TAG, "onEvent: user list size: " + mUserList.size());
                }
            }
        });

    }

    private void getUserLocation(User user) {
        DocumentReference locationRef = mDb.collection(getString(R.string.collection_user_locations))
                .document(user.getUser_id());

        locationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().toObject(UserLocation.class) != null) {
                        mUserLocationList.add(task.getResult().toObject(UserLocation.class));
                    }
                }

            }
        });

    }

    private void getUserDetails(){
        if(mUserLocation == null){
            mUserLocation = new UserLocation();

            DocumentReference userRef = mDb.collection(getString(R.string.collection_users)).document(FirebaseAuth.getInstance().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: successfully got the user details.");

                        User user = task.getResult().toObject(User.class);
                        mUserLocation.setUser(user);
                        ((UserClient)getApplicationContext()).setUser(user);
                        getLastKnownLocation();
                    }
                }
            });
        }
        else {
            getLastKnownLocation();
        }
    }

    private void saveUserLocation(){
        if(mUserLocation != null){
            DocumentReference locationRef = mDb.collection(getString(R.string.collection_user_locations))
                    .document(FirebaseAuth.getInstance().getUid());

            locationRef.set(mUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Log.d(TAG, "saveUserLocation: \ninserted user location into database." + "\n latitude: " +
                                mUserLocation.getGeo_point().getLatitude() +
                                "\n longitude: " + mUserLocation.getGeo_point().getLongitude());
                    }
                }
            });
        }
    }

    private void getLastKnownLocation(){
        Log.d(TAG, "getLastKnownLocation: called.");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful() && task.getResult() != null){
                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    Log.d(TAG, "onComplete: latitude: " + geoPoint.getLatitude());
                    Log.d(TAG, "onComplete: longitude: " + geoPoint.getLongitude());

                    mUserLocation.setGeo_point(geoPoint);
                    mUserLocation.setTimestamp(null);
                    saveUserLocation();

                }

            }
        });

    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getUserDetails();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    getUserDetails();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        status = 1000;

        if(checkMapServices()){
            if(mLocationPermissionGranted){
                getUserDetails();
            }
            else{
                getLocationPermission();
            }
        }

    }

    private void inflateLocationFragment(){
        Log.d(TAG, "inflateLocationFragment called");

        Fragment updatedMapFragment = new MapFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.intent_user_list), mUserList);
        bundle.putParcelableArrayList(getString(R.string.intent_user_locations), mUserLocationList);
        updatedMapFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, updatedMapFragment).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.profileMenuButton:{
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.container);
                if (current != profileFragment) {
                    status = 1000;

                    getSupportFragmentManager().beginTransaction().replace(R.id.container, profileFragment).commit();

                    ImageView profile = (ImageView) findViewById(R.id.profileMenuButton);
                    profile.setImageResource(R.drawable.profile_click);

                    ImageView location = (ImageView) findViewById(R.id.locationMenuButton);
                    location.setImageResource(R.drawable.location);

                    ImageView chatboard = (ImageView) findViewById(R.id.chatboardMenuButton);
                    chatboard.setImageResource(R.drawable.chat);

                    break;
                }else{
                    break;
                }


            }
            case R.id.locationMenuButton:{
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.container);
                if(current != mapFragment && status != 1001){
                    status = 1001;

                    inflateLocationFragment();

                    ImageView profile = (ImageView) findViewById(R.id.profileMenuButton);
                    profile.setImageResource(R.drawable.profile);

                    ImageView location = (ImageView) findViewById(R.id.locationMenuButton);
                    location.setImageResource(R.drawable.location_clicked);

                    ImageView chatboard = (ImageView) findViewById(R.id.chatboardMenuButton);
                    chatboard.setImageResource(R.drawable.chat);

                    break;
                }else{
                    break;
                }

            }
            case R.id.chatboardMenuButton:{
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.container);
                if (current != chatboardFragment){
                    status = 1000;

                    getSupportFragmentManager().beginTransaction().replace(R.id.container, chatboardFragment).commit();

                    ImageView profile = (ImageView) findViewById(R.id.profileMenuButton);
                    profile.setImageResource(R.drawable.profile);

                    ImageView location = (ImageView) findViewById(R.id.locationMenuButton);
                    location.setImageResource(R.drawable.location);

                    ImageView chatboard = (ImageView) findViewById(R.id.chatboardMenuButton);
                    chatboard.setImageResource(R.drawable.chat_clicked);

                    break;
                }else{
                    break;
                }

            }
        }
    }
}
