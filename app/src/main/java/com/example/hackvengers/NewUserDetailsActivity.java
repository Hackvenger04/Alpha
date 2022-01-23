package com.example.hackvengers;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hackvengers.user.UserObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

public class NewUserDetailsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    EditText mName,
            mStatus;

    Spinner spinner;

    ImageView mImage;

    String imageUri;

    Button mSubmit;

    boolean isUser,isMentor,isOrganizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_details_new);


        initializeViews();

        ArrayAdapter<CharSequence> arrayAdapter=new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,new String[]{"User", "Mentor", "Organizer"});
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(this);

        imageUri = "";


        mImage.setOnClickListener(v -> openGallery());
        mImage.setClipToOutline(true);

        final String phoneNumber = getIntent().getStringExtra("phoneNumber");

        mSubmit.setOnClickListener(v -> {
            if (mName != null && !mName.getText().toString().equals("") && (isOrganizer|| isMentor||isUser)) {
                uploadDetailsOnFirebase(phoneNumber);
            } else {
                Toast.makeText(getApplicationContext(), "Your parents have given you such a beautiful name, don't  be ashamed of using it", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadDetailsOnFirebase(final String phoneNumber) {
        final String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        final StorageReference profileStorage = FirebaseStorage.getInstance().getReference().child("ProfilePhotos").child(userId);
        final DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        final HashMap<String, Object> mUserInfo = new HashMap<>();

        mUserInfo.put("Name", mName.getText().toString());
        mUserInfo.put("Phone Number", phoneNumber);
        mUserInfo.put("Status", mStatus.getText().toString());

        String mentorKey="",organizerKey="";


        if(isUser){
            mUserInfo.put("isUser",true);
        }
        if(isMentor){
            mentorKey=FirebaseDatabase.getInstance().getReference().child("Mentor").push().getKey();
            FirebaseDatabase.getInstance().getReference().child("Mentor").child(mentorKey).setValue(true);
            mUserInfo.put("isMentor",mentorKey);
        }
        if(isOrganizer){
            organizerKey=FirebaseDatabase.getInstance().getReference().child("Organizer").push().getKey();
            FirebaseDatabase.getInstance().getReference().child("Organizer").child(mentorKey).setValue(true);

            mUserInfo.put("isOrganizer",organizerKey);
        }



        if (!imageUri.equals("")) {
            final UploadTask uploadTask = profileStorage.putFile(Objects.requireNonNull(Uri.parse(imageUri)));
            Intent intent = new Intent(getApplicationContext(), LoadingActivity.class);
            intent.putExtra("message", "Your Account is being created \n please wait");
            intent.putExtra("isNewUser", true);
            startActivity(intent);
            String finalOrganizerKey = organizerKey;
            String finalMentorKey = mentorKey;
            uploadTask.addOnSuccessListener(taskSnapshot -> profileStorage.getDownloadUrl().addOnSuccessListener(uri -> {
                mUserInfo.put("Profile Image Uri", uri.toString());
                mUserDb.updateChildren(mUserInfo);
                ((LoadingActivity) LoadingActivity.context).finish();
                UserObject userObject = new UserObject(userId, mName.getText().toString(), phoneNumber, mStatus.getText().toString(), uri.toString(), "",isUser,isOrganizer,isMentor, finalMentorKey, finalOrganizerKey);
                userLoggedIn(userObject);
            }));
        }
        else {
            mUserDb.updateChildren(mUserInfo);
            UserObject userObject = new UserObject(userId, mName.getText().toString(), phoneNumber, mStatus.getText().toString(), "", "",isUser,isOrganizer,isMentor,mentorKey,organizerKey);
            userLoggedIn(userObject);
        }

    }




    private void userLoggedIn(UserObject userObject) {
        Intent intent = new Intent(this, FeedActivity.class);
        intent.putExtra("userObject", userObject);
        startActivity(intent);
        finish();
    }

    final int ADD_PROFILE_PHOTO_CODE=1;
    private void openGallery() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select an image"),ADD_PROFILE_PHOTO_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK){
            if(requestCode==ADD_PROFILE_PHOTO_CODE){
                assert data != null;
                imageUri= Objects.requireNonNull(data.getData()).toString();
                Glide.with(getApplicationContext()).load(data.getData()).into(mImage);
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"something went wrong, please try again later onActivity result",Toast.LENGTH_SHORT).show();
        }
    }


    private void initializeViews() {
        mName  = findViewById(R.id.name);
        mStatus= findViewById(R.id.status);
        mImage = findViewById(R.id.newUserProfileImage);
        mSubmit= findViewById(R.id.submitDetails);

        spinner=findViewById(R.id.spinner);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        switch (position){

            case 0:isUser=true;isOrganizer=false;isMentor=false;break;
            case 1:isUser=false;isOrganizer=false;isMentor=true;break;
            case 2:isUser=false;isOrganizer=true;isMentor=false;break;

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}