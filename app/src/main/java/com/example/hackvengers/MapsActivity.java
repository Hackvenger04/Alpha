package com.example.hackvengers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.hackvengers.cluster.ClusterMarker;
import com.example.hackvengers.cluster.MyClusterManagerRenderer;
import com.example.hackvengers.location.UserLocation;
import com.example.hackvengers.user.UserObject;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Objects;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,View.OnClickListener,
        GoogleMap.OnInfoWindowClickListener,GoogleMap.OnInfoWindowLongClickListener{

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String TAG = "myDEBUG";
    private MapView mMapView;
    private ArrayList<UserLocation> mUserLocations;
    FirebaseDatabase db;
    FirebaseAuth mAuth;
    private GoogleMap mGoogleMap;
    private UserLocation mUserPosition;
    private LatLngBounds mMapBoundary;
    private ClusterManager mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers;
    private int cnt=0;
    private static int count=0;

    private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
    private static final int MAP_LAYOUT_STATE_EXPANDED = 1;

    private Handler mHandler=new Handler();
    int delay=1000;
    boolean finished=false;



    GoogleMap.OnInfoWindowClickListener MyOnInfoWindowClickListener
            = new GoogleMap.OnInfoWindowClickListener(){
        @Override
        public void onInfoWindowClick(Marker marker) {
            Log.d("hello","ggggggggggggggggggggggggggggggggg");

            Log.d("hello",marker.getId());


            Intent intent=new Intent(MapsActivity.this,UserDetailsActivity.class);
            intent.putExtra("userKey",marker.getSnippet());
            startActivity(intent);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mMapView = findViewById(R.id.user_list_map);
        db=FirebaseDatabase.getInstance();
        mAuth=FirebaseAuth.getInstance();
        mUserLocations=new ArrayList<>();
        mClusterMarkers=new ArrayList<>();
        //getLocations();
        finished=false;
        initGoogleMap(savedInstanceState);
    }

    private void getLocations()
    {
        DatabaseReference ref=db.getReference().child("UserLocation");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists())
                {
                    String userName = "";
                    String userPhone = "";
                    String userImage = "";
                    String userStatus = "";
                    String chatID = "";
                    String notificationKey = "";

                    boolean isUser = false,isMentor=false,isOrganizer=false;

                    String mentorKey="",organizerKey="";

                    if (snapshot.child("User").child("Name").getValue() != null) {
                        userName = Objects.requireNonNull(snapshot.child("Name").getValue()).toString();
                    }
                    if (snapshot.child("User").child("Phone Number").getValue() != null) {
                        userPhone = Objects.requireNonNull(snapshot.child("Phone Number").getValue()).toString();
                    }
                    if (snapshot.child("User").child("Profile Image Uri").getValue() != null) {
                        userImage = Objects.requireNonNull(snapshot.child("Profile Image Uri").getValue()).toString();
                    }
                    if (snapshot.child("User").child("Status").getValue() != null) {
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


                    UserObject user= new UserObject(mAuth.getCurrentUser().getUid(), userName, userPhone, userStatus, userImage, chatID, isUser,isOrganizer,isMentor,mentorKey,organizerKey);
                    double lat=(Double.parseDouble(snapshot.child("geo_point").child("latitude").getValue().toString()));
                    double lon=(Double.parseDouble(snapshot.child("geo_point").child("longitude").getValue().toString()));
                    GeoPoint geoPoint=new GeoPoint(lat,lon);
                    //mUserLocations.add(new UserLocation(user,geoPoint,null));
                    addMapMarkerSingle(new UserLocation(user,geoPoint,null));
                    mGoogleMap.setOnInfoWindowClickListener(MyOnInfoWindowClickListener);
                    cnt++;
                    Log.d("hello"," --"+cnt);
                    if(cnt==count){
                        finished=true;
                        Log.d("hello"," --||"+cnt);

                    }

                    Log.d("Added","Added");
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mUserLocations=new ArrayList<>();
    }

    private void setCameraView() {

        // Set a boundary to start
        double bottomBoundary = mUserPosition.getGeo_point().getLatitude() - .1;
        double leftBoundary = mUserPosition.getGeo_point().getLongitude() - .1;
        double topBoundary = mUserPosition.getGeo_point().getLatitude() + .1;
        double rightBoundary = mUserPosition.getGeo_point().getLongitude() + .1;

        mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
    }

    private void setUserPosition() {
        mUserPosition=new UserLocation(new UserObject(),new GeoPoint(1,1),null);
//        Log.d("gggggg","gsdfdsfgsdfgef");
//        for (UserLocation userLocation : mUserLocations) {
//            Log.d("gggggg","ttttt");
//            if (userLocation.getUser().getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//                mUserPosition = userLocation;
//                Log.d("gggggg","mmmmmmmmmmmmm");
//            }
//        }


        DatabaseReference reference=db.getReference().child("UserLocation").child(mAuth.getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    Log.d("sssssss","ssssss");
                    double lat=Double.parseDouble(snapshot.child("geo_point").child("latitude").getValue().toString());
                    Log.d("sssssss",String.valueOf(lat));
                    double lon=Double.parseDouble(snapshot.child("geo_point").child("longitude").getValue().toString());
                    Log.d("sssssss",String.valueOf(lat));
                    String userName = "";
                    String userPhone = "";
                    String userImage = "";
                    String userStatus = "";
                    String chatID = "";
                    String notificationKey = "";

                    boolean isUser = false,isMentor=false,isOrganizer=false;

                    String mentorKey="",organizerKey="";

                    if (snapshot.child("User").child("Name").getValue() != null) {
                        userName = Objects.requireNonNull(snapshot.child("Name").getValue()).toString();
                    }
                    if (snapshot.child("User").child("Phone Number").getValue() != null) {
                        userPhone = Objects.requireNonNull(snapshot.child("Phone Number").getValue()).toString();
                    }
                    if (snapshot.child("User").child("Profile Image Uri").getValue() != null) {
                        userImage = Objects.requireNonNull(snapshot.child("Profile Image Uri").getValue()).toString();
                    }
                    if (snapshot.child("User").child("Status").getValue() != null) {
                        userStatus = Objects.requireNonNull(snapshot.child("Status").getValue()).toString();
                    }
                    if (snapshot.child("User").child("notificationKey").getValue() != null) {
                        notificationKey = Objects.requireNonNull(snapshot.child("notificationKey").getValue()).toString();
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


                    UserObject user= new UserObject(mAuth.getCurrentUser().getUid(), userName, userPhone, userStatus, userImage, chatID,isUser,isOrganizer,isMentor,mentorKey,organizerKey);
                    mUserPosition=new UserLocation(user,new GeoPoint(lat,lon),null);
                    //addMapMarkerSingle(mUserPosition);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initGoogleMap(Bundle savedInstanceState){
        // * IMPORTANT *
        // MapView requires that the Bundle you pass contain ONLY MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }


    @Override
    public void onMapReady(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Toast.makeText(MapsActivity.this, "Click on the launcher icon for your location", Toast.LENGTH_SHORT).show();
        map.setMyLocationEnabled(true);
        //map.setOnInfoWindowClickListener(this);
        mGoogleMap=map;
        getCount();
        setUserPosition();
        //mUserPosition.setGeo_point(new GeoPoint(mGoogleMap.getMyLocation().getLatitude(),mGoogleMap.getMyLocation().getLatitude()));
        addMapMarkers();
        mGoogleMap.setOnInfoWindowClickListener(this);
    }

    public void getCount()
    {
        DatabaseReference ref= db.getReference().child("UserLocation");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    count = (int)(snapshot.getChildrenCount());
                    Log.d("helllllllll",String.valueOf(count));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        finished=true;
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void addMapMarkerSingle(UserLocation userLocation)
    {
        Log.d("hello","4444444");

        Log.d(TAG, "addMapMarkers: location: " + userLocation.getGeo_point().toString());
        try{
            Log.d("hello","55555555");
            String snippet = "";
            if(userLocation.getUser().getUid().equals(mAuth.getCurrentUser().getUid())){
                snippet = mAuth.getUid();
            }
            else{
                snippet = userLocation.getUser().getUid();
            }

            String avatar =  "drawable://" + R.drawable.cartman_cop; // set the default avatar
            try{
                avatar = (userLocation.getUser().getProfileImageUri());
            }catch (Exception e){
                Log.d("hello","No Avator");
                Log.d(TAG, "addMapMarkers: no avatar for " + userLocation.getUser().getName() + ", setting default.");
            }
            ClusterMarker newClusterMarker = new ClusterMarker(
                    new LatLng(userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude()),
                    userLocation.getUser().getName(),
                    snippet,
                    avatar,
                    userLocation.getUser()

            );
            mClusterManager.addItem(newClusterMarker);
            Log.d("hello","goodgood");
            mClusterMarkers.add(newClusterMarker);

        }catch (NullPointerException e){
            Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage() );
            Log.d("hello","66666666666");
        }
    }

    private void addMapMarkers(){

        if(mGoogleMap != null){
            Log.d("hello","111111");
            if(mClusterManager == null){
                Log.d("hello","22222");
                mClusterManager = new ClusterManager<ClusterMarker>(MapsActivity.this, mGoogleMap);
            }
            if(mClusterManagerRenderer == null){
                Log.d("hello","33333");
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        MapsActivity.this,
                        mGoogleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }
            //addMapMarkerSingle(mUserPosition);
            getLocations();
//            for(UserLocation userLocation:mUserLocations)
//            {
//                addMapMarkerSingle(userLocation);
//            }
            Log.d("hello","something");
//            while(cnt!=count)
//            {
//                Log.d("hello","crashed");
//            }
//            if(cnt==count)
//                Log.d("hello","jiiiiiiiiiiiii");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    mClusterManager.cluster();
                    if(finished)
                        return;
                    Log.d("hello","mzaa aa gya");
                    mHandler.postDelayed(this,delay);
                }
            },delay);
            mClusterManager.cluster();

            setCameraView();
        }
    }

    @Override
    public void onClick(View view) {
        Log.d("ghhhhhhhhhhhhhhhhh","wwwwwwwwwww");
    }

    @Override
    public void onInfoWindowLongClick(@NonNull Marker marker) {
        Log.d("ghhhhhhhhhhhhhhhh","sssssssss");
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        Log.d("hello","gggggggggggggggggggggggggggggggggggggggggggggggggggggg");
    }
}