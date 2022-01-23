package com.example.hackvengers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hackvengers.location.UserLocation;
import com.example.hackvengers.user.UserObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.GeoPoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {


    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    private static final int ERROR_DIALOG_REQUEST = 9000;

    private static final String TAG="mYDebug";


    boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation mUserLocation;


    TextView phoneNumber,login;
    EditText mPhoneNumber,
            mVerificationCode;


    String phoneNo;
    Button mSend;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    FirebaseAuth mAuth;
    FirebaseDatabase mDb;

    String mVerificationId;

    @Override
    public void onResume() {
        super.onResume();
        if (checkMapServices()) {
            if (mLocationPermissionGranted)
            {
                userLoggedIn();
                //getUserDetails();
            }
            else
                getLocationPermission();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDb=FirebaseDatabase.getInstance();

        mFusedLocationClient= LocationServices.getFusedLocationProviderClient(this);
        userLoggedIn();
        setContentView(R.layout.activity_login_new);


        initializeViews();

        mSend.setOnClickListener(v -> {
            if (mVerificationId != null) {
                verifyPhoneNumberWithCode();
            } else {
                startPhoneNumberVerification();
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(getApplicationContext(), "On verification Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);
//                findViewById(R.id.codeLayout).setVisibility(View.VISIBLE);

                login.setText("OTP");
                phoneNumber.setText("Verify OTP");
                phoneNo=mPhoneNumber.getText().toString();
                mPhoneNumber.setText("");
                mSend.setText("Verify");
                mVerificationId=verificationId;
                mSend.setText("Verify Code");
            }
        };


    }


    //NEW
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    mUserLocation.setGeo_point(geoPoint);
                    mUserLocation.setTimestamp(null);
                    saveUserLocation();
                }
            }
        });

    }


    //NEW
    private void getUserDetails(){
        if(mUserLocation == null){
            mUserLocation = new UserLocation(new UserObject(),new GeoPoint(1.0, 1.0),new Date());
            DatabaseReference userRef = mDb.getReference().child("UserLocation");
            String key=mAuth.getCurrentUser().getUid();
            DatabaseReference us=mDb.getReference().child("Users").child(key);

            us.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String userName = "";
                        String userPhone = "";
                        String userImage = "";
                        String userStatus = "";
                        String chatID = "";
                        String notificationKey = "";

                        boolean isUser = false,isMentor=false,isOrganizer=false;

                        String mentorKey="",organizerKey="";

                        if (snapshot.child("Name").getValue() != null) {
                            userName = Objects.requireNonNull(snapshot.child("Name").getValue()).toString();
                        }
                        if (snapshot.child("Phone Number").getValue() != null) {
                            userPhone = Objects.requireNonNull(snapshot.child("Phone Number").getValue()).toString();
                        }
                        if (snapshot.child("Profile Image Uri").getValue() != null) {
                            userImage = Objects.requireNonNull(snapshot.child("Profile Image Uri").getValue()).toString();
                        }
                        if (snapshot.child("Status").getValue() != null) {
                            userStatus = Objects.requireNonNull(snapshot.child("Status").getValue()).toString();
                        }


                        if (snapshot.child("isUser").getValue() != null) {
                            isUser = true;
                        }
                        if (snapshot.child("isMentor").getValue() != null) {
                            isMentor = true;
                            mentorKey=snapshot.child("isMentor").getValue().toString();
                        }
                        if (snapshot.child("isOrganizer").getValue() != null) {
                            isOrganizer = true;
                            organizerKey=snapshot.child("isOrganizer").getValue().toString();
                        }


                        UserObject userObject = new UserObject(mAuth.getCurrentUser().getUid(), userName, userPhone, userStatus, userImage, chatID, isUser,isOrganizer,isMentor,mentorKey,organizerKey);
                        mUserLocation.setUser(userObject);
                        getLastKnownLocation();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            getLastKnownLocation();
        }
    }

    private void verifyPhoneNumberWithCode() {
        PhoneAuthCredential phoneAuthCredential=PhoneAuthProvider.getCredential(mVerificationId,mPhoneNumber.getText().toString());
        signInWithPhoneAuthCredential(phoneAuthCredential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                if (mUser != null) {
                    final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid());
                    mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                Intent intent = new Intent(getApplicationContext(), NewUserDetailsActivity.class);
                                intent.putExtra("phoneNumber", phoneNo);
                                startActivity(intent);
                                finish();
                            } else {
                                userLoggedIn();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getApplicationContext(), "On Cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }


    private void userLoggedIn() {
        FirebaseUser mUser = mAuth.getCurrentUser();
        if(mUser!=null){

            //new
            getUserDetails();

            Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
            startActivity(intent);
            finish();
        }
    }



    private void startPhoneNumberVerification() {
        PhoneAuthOptions phoneAuthOptions = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(mPhoneNumber.getText().toString())
                .setTimeout(90L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
    }

    private void initializeViews() {

        phoneNumber=findViewById(R.id.textViewPhoneNumber);
        login=findViewById(R.id.loginTextView);

        mPhoneNumber        = findViewById(R.id.editTextPhone);
        //mVerificationCode   = findViewById(R.id.verificationCode);
        mSend       = findViewById((R.id.buttonLogin));
    }


    //NEW
    private void saveUserLocation(){

        if(mUserLocation != null){
            DatabaseReference locationRef = mDb
                    .getReference().child("UserLocation")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            locationRef.setValue(mUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "saveUserLocation: \ninserted user location into database." +
                                "\n latitude: " + mUserLocation.getGeo_point().getLatitude() +
                                "\n longitude: " + mUserLocation.getGeo_point().getLongitude());
                    }
                }
            });
        }
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
            userLoggedIn();;
            //getUserDetails();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                    userLoggedIn();
                    getUserDetails();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }
}