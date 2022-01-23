package com.example.hackvengers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hackvengers.user.UserAdapter;
import com.example.hackvengers.user.UserObject;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class UserDetailsActivity extends AppCompatActivity {

    ImageView profileImage,
                arrow;

    TextView profileName,
                connections,
                rating,
                editAboutMe,
                profileAboutMe,
                experience,
                editExperience,
                addCertificate;

    Context context;


    ArrayList<String> certificates;

    LinearLayout ratingLayout,
                    certificateLayout;

    RelativeLayout userContainer,mentorContainer;

    ImageButton addInterestButton;

    ImageView addPlanButton;

    LinearLayout plansLayout;

    String mentorKey="";


    Button connectButton;

    RecyclerView mUserList;
    RecyclerView.Adapter<UserAdapter.ViewHolder> mUserListAdapter;
    RecyclerView.LayoutManager mUserListLayoutManager;

//    RecyclerView mImageList;
//    RecyclerView.Adapter<ImageAdapter.ViewHolder> mImageListAdapter;
//    RecyclerView.LayoutManager mImageListLayoutManager;


    ArrayList<UserObject> userList;

    String uid;

    long noOfConnections=0;

    boolean isAConnection;


    UserObject userObject;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);


        context=getApplicationContext();


        userObject= (UserObject) getIntent().getSerializableExtra("userObject");
        if(userObject==null){
            uid=getIntent().getStringExtra("userKey");

        }
        else{
            uid=userObject.getUid();
        }

        userList=new ArrayList<>();
        initializeViews();
        initializeRecyclerViews();

        if(userObject==null){
            getUserDetails(uid);
        }

        getMentorList(uid);

        getNumberOfConnections(uid);


    }

    private void getNumberOfConnections(String userKey) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(userKey).child("Connections").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                if(snapshot.exists()){
                    noOfConnections=snapshot.getChildrenCount();
                    if(snapshot.child(AllChatsActivity.curUser.getUid()).getValue()!=null){
                        isAConnection=true;
                    }
                    fillViews();
                }
                else{
                    fillViews();
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }


    private void getUserDetails(String userKey) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = "";
                    String userPhone = "";
                    String userImage = "";
                    String userStatus = "";
                    String chatID = "";

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
                    userObject = new UserObject(userKey, userName, userPhone, userStatus, userImage, chatID,isUser,isOrganizer,isMentor,mentorKey,organizerKey);
                    fillViews();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMentorList(String userKey){
        FirebaseDatabase.getInstance().getReference().child("Users").child(userKey).child("Mentors").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot childSnapshot:snapshot.getChildren()){
                        String childKey=childSnapshot.getKey();
                        FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(childKey)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot newSnapshot) {
                                if(newSnapshot.exists()){
                                    String mentorName="";
                                    String mentorImage="";

                                    if (newSnapshot.child("Name").getValue() != null) {
                                        mentorName = Objects.requireNonNull(newSnapshot.child("Name").getValue()).toString();
                                    }
                                    if (newSnapshot.child("Profile Image Uri").getValue() != null) {
                                        mentorImage = Objects.requireNonNull(newSnapshot.child("Profile Image Uri").getValue()).toString();
                                    }

                                    UserObject mentor=new UserObject(childKey,mentorName,"","",mentorImage,"");

                                    userList.add(mentor);
                                    mUserListAdapter.notifyItemInserted(userList.size() - 1);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fillViews() {

        mentorKey=userObject.getMentorKey();

        if(!userObject.getProfileImageUri().equals("")){
            profileImage.setClipToOutline(true);
            Glide.with(this).load(Uri.parse(userObject.getProfileImageUri())).into(profileImage);
        }
        profileName.setText(userObject.getName());
        if(userObject.isMentor()){
            //arrow.setVisibility(View.VISIBLE);
            ratingLayout.setVisibility(View.VISIBLE);
            mentorContainer.setVisibility(View.VISIBLE);
            userContainer.setVisibility(View.GONE);

            getMentorDetails(userObject.getMentorKey());
        }
        profileAboutMe.setText(userObject.getStatus());

        connections.setText(noOfConnections+" Connections");

        if(!isAConnection && !uid.equals(AllChatsActivity.curUser.getUid())){
            connectButton.setVisibility(View.VISIBLE);
            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateConnections(AllChatsActivity.curUser.getUid(),uid);
                    connectButton.setVisibility(View.GONE);
                }
            });
        }

        if(userObject.getUid().equals(AllChatsActivity.curUser.getUid())){
            editAboutMe.setVisibility(View.VISIBLE);
            addPlanButton.setVisibility(View.VISIBLE);
            addInterestButton.setVisibility(View.VISIBLE);
            editExperience.setVisibility(View.VISIBLE);

            addPlanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(),"This feature is only available for verified Mentors",Toast.LENGTH_LONG).show();
                }
            });
            addCertificate.setVisibility(View.VISIBLE);



            addCertificate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openGalleryAddCertificate();
                }
            });

            boolean edit=true;

            editExperience.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(edit){
                        editExperience.setText("Save");

                    }
                }
            });

            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openGallery();
                }
            });
        }




    }

    private void getMentorDetails(String mentorKey) {


        FirebaseDatabase.getInstance().getReference().child("Mentor").child(mentorKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                Log.i("Hello","here0");


                String experience="";
                String rating="";
                certificates=new ArrayList<>();

                if(snapshot.exists()){

                    Log.i("Hello","here1");
                    if(snapshot.child("experience").getValue()!=null){
                        experience= Objects.requireNonNull(snapshot.child("experience").getValue()).toString();
                    }
                    if(snapshot.child("rating").getValue()!=null){
                        rating= Objects.requireNonNull(snapshot.child("rating").getValue()).toString();
                    }

                    String finalExperience = experience;
                    String finalRating = rating;
                    FirebaseDatabase.getInstance().getReference().child("Mentor").child(mentorKey).child("Certificates").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull  DataSnapshot newSnapshot) {
                            Log.i("Hello","here2");
                            if(newSnapshot.exists()){
                                Log.i("Hello","here3");
                                for(DataSnapshot childSnapshot: newSnapshot.getChildren()){
                                    certificates.add(Objects.requireNonNull(childSnapshot.getValue()).toString());
                                    Log.i("Hello",childSnapshot.getValue().toString());
//                                    mImageListAdapter.notifyItemInserted(certificates.size()-1);
                                    //fillMentorViews(finalExperience, finalRating);

                                }
                            }
                            else{
                                //fillMentorViews(finalExperience, finalRating);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull  DatabaseError error) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    private void fillMentorViews(String finalExperience, String finalRating) {
//        rating.setText(finalRating);
//        experience.setText(finalExperience);
//
////        for(String imageUri:certificates){
////            View view=getLayoutInflater().inflate(R.layout.activity_image_view,null,false);
////
////            ImageView imageView=view.findViewById(R.id.imageView);
////
//////            imageView.setImageResource(R.drawable.arrow);
////
////            Glide.with(context).load(Uri.parse(imageUri)).into(imageView);
////            certificateLayout.addView(view);
////
////
////        }
//
//
//    }



    private void updateConnections(String curUserKey, String userKey) {
        HashMap<String,Object> curUserMap=new HashMap<>();
        HashMap<String,Object> userMap=new HashMap<>();

        curUserMap.put(userKey,true);
        userMap.put(curUserKey,true);

        FirebaseDatabase.getInstance().getReference().child("Users").child(curUserKey).child("Connections").updateChildren(curUserMap);
        FirebaseDatabase.getInstance().getReference().child("Users").child(userKey).child("Connections").updateChildren(userMap);
    }

    private void initializeViews(){
        profileImage=findViewById(R.id.profile_image);

        profileName=findViewById(R.id.nameHolder);
        profileAboutMe=findViewById(R.id.aboutMeText);
        rating=findViewById(R.id.ratingContainer);
        connections=findViewById(R.id.connectionCount);
        arrow=findViewById(R.id.arrowContainer);


        experience=findViewById(R.id.experienceText);
        editExperience=findViewById(R.id.editBtn2);

        editAboutMe=findViewById(R.id.editBtn);
        connectButton=findViewById(R.id.connectBtn);

        addCertificate=findViewById(R.id.addCertificate);

        addPlanButton=findViewById(R.id.insertPlan);

        addInterestButton=findViewById(R.id.interestAddBtn);

        certificateLayout=findViewById(R.id.certificateContainer);
        plansLayout=findViewById(R.id.plansContainer);

        userContainer=findViewById(R.id.containerforUsers);
        mentorContainer=findViewById(R.id.containerForMentors);


        ratingLayout=findViewById(R.id.ratingLayout);
    }
    private void initializeRecyclerViews() {
        mUserList = findViewById(R.id.mentorRecyclerView);
        mUserList.setHasFixedSize(false);
        mUserList.setNestedScrollingEnabled(false);

        if(uid.equals(AllChatsActivity.curUser.getUid())){
            mUserListAdapter = new UserAdapter(userList, this, false, false,true);

        }
        else{
            mUserListAdapter = new UserAdapter(userList, this, false, false,false);

        }
        mUserList.setAdapter(mUserListAdapter);

        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        mUserList.setLayoutManager(mUserListLayoutManager);


//
//        mImageList = findViewById(R.id.certificateContainer);
//        mImageList.setHasFixedSize(false);
//        mImageList.setNestedScrollingEnabled(false);
//        mImageListAdapter = new ImageAdapter(certificates,this);
//
//        mImageList.setAdapter(mUserListAdapter);
//
//        mImageListLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false);
//        mImageList.setLayoutManager(mImageListLayoutManager);
    }




    final int CHANGE_PROFILE_PHOTO_CODE=1;
    final int CANCEL_UPLOAD_TASK=3;
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an image"), CHANGE_PROFILE_PHOTO_CODE);
    }

    final int ADD_MEDIA=2;
    private void openGalleryAddCertificate() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an image"), ADD_MEDIA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            final UploadTask uploadTask;
            Intent intent;
            switch (requestCode) {
                case CHANGE_PROFILE_PHOTO_CODE:
                    final StorageReference profileStorage = FirebaseStorage.getInstance().getReference().child("ProfilePhotos").child(uid);
                    final DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    assert data != null;
                    uploadTask = profileStorage.putFile(Objects.requireNonNull(data.getData()));
                    intent = new Intent(getApplicationContext(), LoadingActivity.class);
                    intent.putExtra("message", "Your Image is being uploaded \\n please wait");
                    intent.putExtra("isNewUser", false);
                    startActivityForResult(intent, CANCEL_UPLOAD_TASK);
                    uploadTask.addOnSuccessListener(taskSnapshot -> profileStorage.getDownloadUrl().addOnSuccessListener(uri -> {
                        changeProfilePhoto(uri);
                        mUserDb.child("Profile Image Uri").setValue(uri.toString());
                        AllChatsActivity.curUser.setProfileImageUri(uri.toString());
                        Glide.with(getApplicationContext()).load(uri).into(profileImage);
                        ((LoadingActivity) LoadingActivity.context).finish();
                    }));

                    break;

                case ADD_MEDIA:
                    final StorageReference mProfileStorage = FirebaseStorage.getInstance().getReference().child("Certificates").child(uid);
                    final DatabaseReference mentorDb = FirebaseDatabase.getInstance().getReference().child("Mentor").child(mentorKey).child("Certificates");


                    assert data != null;
                    uploadTask = mProfileStorage.putFile(Objects.requireNonNull(data.getData()));
                    intent = new Intent(getApplicationContext(), LoadingActivity.class);
                    intent.putExtra("message", "Your Image is being uploaded \\n please wait");
                    intent.putExtra("isNewUser", false);
                    startActivityForResult(intent, CANCEL_UPLOAD_TASK);
                    uploadTask.addOnSuccessListener(taskSnapshot -> mProfileStorage.getDownloadUrl().addOnSuccessListener(uri -> {
                        changeProfilePhoto(uri);
                        String key=mentorDb.push().getKey();
                        mentorDb.child(key).setValue(uri.toString());
                        //Glide.with(getApplicationContext()).load(uri).into(profileImage);

                        certificates.add(uri.toString());
//                        mImageListAdapter.notifyItemInserted(certificates.size()-1);
                        ((LoadingActivity) LoadingActivity.context).finish();
                    }));

                    break;

                default:
                    Toast.makeText(getApplicationContext(), "something went wrong, please try again later onActivity result", Toast.LENGTH_SHORT).show();

            }

        }
    }

    private void changeProfilePhoto(final Uri uri) {
        final DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Single chats");
        final DatabaseReference mChatDb = FirebaseDatabase.getInstance().getReference().child("Chats");
        mUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String userKey = childSnapshot.getKey();
                        String chatKey = Objects.requireNonNull(childSnapshot.getValue()).toString();
                        assert userKey != null;
                        mChatDb.child(chatKey + "/info/" + userKey + "/Chat Profile Image Uri").setValue(uri.toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}