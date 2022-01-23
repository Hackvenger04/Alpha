package com.example.hackvengers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

public class CreatePostActivity extends AppCompatActivity {

    private EditText eventName, details, link;
    private ImageView poster;
    private Button submit;

    private final String[] category = {"Basketball","Cricket","Volleyball","Football","Badminton"};

    AutoCompleteTextView dropDown;

    ArrayAdapter<String> adapterItems;

    private String item = "";
    String imageUri="";

    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        intializeViews();

        loadingBar = new ProgressDialog(this);

        adapterItems = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,category);
        dropDown.setAdapter(adapterItems);

        poster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        dropDown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                item = adapterView.getItemAtPosition(i).toString();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(CreatePostActivity.this,"0",Toast.LENGTH_SHORT).show();
                loadingBar.setTitle("Creating Post");
                loadingBar.setMessage("Please wait...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                checkDetails();
            }
        });
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
                Glide.with(getApplicationContext()).load(data.getData()).into(poster);
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"something went wrong, please try again later onActivity result",Toast.LENGTH_SHORT).show();
        }
    }

    private void checkDetails() {

        //Toast.makeText(CreatePostActivity.this,"11",Toast.LENGTH_SHORT).show();
        String orgKey = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        //Toast.makeText(CreatePostActivity.this,"12",Toast.LENGTH_SHORT).show();
        DatabaseReference eventRef= FirebaseDatabase.getInstance().getReference().child("Feed");
        DatabaseReference orgRef=FirebaseDatabase.getInstance().getReference().child("Users");
        //Toast.makeText(CreatePostActivity.this,"13",Toast.LENGTH_SHORT).show();
        StorageReference profileStorage = FirebaseStorage.getInstance().getReference().child("Feed Photos");
        //Toast.makeText(CreatePostActivity.this,"14",Toast.LENGTH_SHORT).show();
        String org_event = eventName.getText().toString();
        String org_details = details.getText().toString();
        String org_link = link.getText().toString();

        final HashMap<String, Object> feed = new HashMap<>();

        if(!org_event.equals("")){
            feed.put("event name",org_event);
        }
        else{
            Toast.makeText(CreatePostActivity.this, "Please add Event Name", Toast.LENGTH_SHORT).show();
        }
        if(!org_details.equals("")){
            feed.put("details",org_details);
        }
        else{
            Toast.makeText(CreatePostActivity.this, "Please add details", Toast.LENGTH_SHORT).show();
        }
        if(!org_link.equals(""))
            feed.put("link",org_link);
        else{
            Toast.makeText(CreatePostActivity.this, "Please add link", Toast.LENGTH_SHORT).show();
        }

        feed.put("category",item);

        String org_name =AllChatsActivity.curUser.getName();
        String org_image = AllChatsActivity.curUser.getProfileImageUri();

        feed.put("Organiser Name",org_name);
        feed.put("Organiser Image",org_image);

        final String feedId = eventRef.push().getKey();
        //Toast.makeText(CreatePostActivity.this,feedId,Toast.LENGTH_SHORT).show();
        assert feedId != null;
        if(!imageUri.equals("")){
            //Toast.makeText(CreatePostActivity.this,"3",Toast.LENGTH_SHORT).show();
            final UploadTask uploadTask = profileStorage.child(feedId).putFile(Objects.requireNonNull(Uri.parse(imageUri)));
            uploadTask.addOnSuccessListener(taskSnapshot -> profileStorage.child(feedId).getDownloadUrl().addOnSuccessListener(uri -> {
                //Toast.makeText(CreatePostActivity.this,"4",Toast.LENGTH_SHORT).show();
                feed.put("poster image", uri.toString());
                eventRef.child(feedId).updateChildren(feed);
                //Toast.makeText(CreatePostActivity.this,"5",Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                Intent intent = new Intent(CreatePostActivity.this, FeedActivity.class);
                startActivity(intent);
            }));
        }
        else{
            Toast.makeText(CreatePostActivity.this, "Please add an image", Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();
        }

    }

    private void intializeViews() {
        eventName = findViewById(R.id.event_name);
        details = findViewById(R.id.details);
        poster = findViewById(R.id.poster);
        link = findViewById(R.id.link);
        dropDown = findViewById(R.id.category);
        submit = findViewById(R.id.submit_post);
    }
}