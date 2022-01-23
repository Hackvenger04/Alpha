package com.example.hackvengers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hackvengers.feed.FeedAdapter;
import com.example.hackvengers.feed.FeedObject;
import com.example.hackvengers.user.UserObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class FeedActivity extends AppCompatActivity {


    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle toggle;
    private NavigationView navigationView;


    public static RecyclerView.Adapter<FeedAdapter.ViewHolder> feedAdapter;
    RecyclerView feedList;
    RecyclerView.LayoutManager feedListLayoutManager;

    public static UserObject curUser;

    ArrayList<FeedObject> feed;

    FloatingActionButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        feed = new ArrayList<>();
        intializeViews();
        initializeRecyclerViews();

//        getUserDetails(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
//

        curUser=AllChatsActivity.curUser;

        drawerLayout=findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.navlayout);





        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull  MenuItem item) {
                Intent intent;

                switch (item.getItemId()){
                    case R.id.navProfile:
                        intent = new Intent(FeedActivity.this, UserDetailsActivity.class);
                        Log.i("Hello","Here");
                        intent.putExtra("userObject", AllChatsActivity.curUser);
                        startActivity(intent);
                        break;

                    case R.id.navMap:
                        intent = new Intent(FeedActivity.this, MapsActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.navRentalShop:
                        intent = new Intent(FeedActivity.this, RentalActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.navDonation:
                        intent = new Intent(FeedActivity.this, paymentActivity.class);
                        startActivity(intent);
                        break;


                    case R.id.navChat:
                        intent = new Intent(FeedActivity.this, AllChatsActivity.class);
                        intent.putExtra("isFromFeed",true);
                        startActivity(intent);
                        break;
                    case R.id.navLogout:
                        FirebaseAuth.getInstance().signOut();
                        intent = new Intent(FeedActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        break;


                }
                return false;
            }
        });



        getFeedList();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FeedActivity.this,CreatePostActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getFeedList() {
        DatabaseReference mFeed = FirebaseDatabase.getInstance().getReference().child("Feed");

        mFeed.orderByValue().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists() && snapshot.getKey()!=null){
                    //Toast.makeText(FeedActivity.this,"1",Toast.LENGTH_SHORT).show();
                    getFeedDetails(snapshot.getKey());
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
    }

    private void getFeedDetails(final String key) {

        //Toast.makeText(FeedActivity.this,"2",Toast.LENGTH_SHORT).show();

        final String[] e_name = new String[1];
        final String[] e_details = new String[1];
        final String[] e_link = new String[1];
        final String[] e_poster = new String[1];
        final String[] o_name = new String[1];
        final String[] o_image = new String[1];
        final String[] category = new String[1];


        final DatabaseReference mFeedDb = FirebaseDatabase.getInstance().getReference().child("Feed");
        //Toast.makeText(FeedActivity.this,"3",Toast.LENGTH_SHORT).show();

        mFeedDb.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //Toast.makeText(FeedActivity.this,"4",Toast.LENGTH_SHORT).show();
                    e_name[0] = Objects.requireNonNull(snapshot.child("event name").getValue()).toString();
                    e_details[0] = Objects.requireNonNull(snapshot.child("details").getValue()).toString();
                    e_link[0] = Objects.requireNonNull(snapshot.child("link").getValue()).toString();
                    e_poster[0] = Objects.requireNonNull(snapshot.child("poster image").getValue()).toString();
                    o_name[0] = Objects.requireNonNull(snapshot.child("Organiser Name").getValue()).toString();
                    o_image[0] = Objects.requireNonNull(snapshot.child("Organiser Image").getValue()).toString();
                    category[0] =Objects.requireNonNull(snapshot.child("category").getValue()).toString();
                    FeedObject feedObject = new FeedObject(o_name[0],o_image[0],e_name[0],e_poster[0],e_details[0],e_link[0], category[0]);
                    feed.add(feedObject);
                    //Toast.makeText(FeedActivity.this,"5",Toast.LENGTH_SHORT).show();
                    feedAdapter.notifyItemInserted(feed.size()-1);
                    //Toast.makeText(FeedActivity.this,"6",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getUserDetails(final String userKey) {
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


                    curUser = new UserObject(userKey, userName, userPhone, userStatus, userImage, chatID,isUser,isOrganizer,isMentor,mentorKey,organizerKey);
                    fillViews();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void fillViews(){
        View headerView = navigationView.getHeaderView(0);
        TextView headerName=headerView.findViewById(R.id.headerProfileName);
        headerName.setText(curUser.getName());

        ImageView imageView=headerView.findViewById(R.id.headerProfilePic);

        if(!curUser.getProfileImageUri().equals("")){
            Glide.with(this).load(Uri.parse(curUser.getProfileImageUri())).into(imageView);
        }

        if(!curUser.isOrganizer()){
            button.setVisibility(View.GONE);
        }


    }

    private void initializeRecyclerViews() {
        feedList = findViewById(R.id.recyclerViewListChats);
        feedList.setHasFixedSize(false);
        feedList.setNestedScrollingEnabled(false);

        feedList.addItemDecoration(new DividerItemDecoration(feedList.getContext(), DividerItemDecoration.VERTICAL));

        feedAdapter = new FeedAdapter(feed, this);
        feedAdapter.setHasStableIds(true);
        feedList.setAdapter(feedAdapter);

        feedListLayoutManager=new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL,false);
        feedList.setLayoutManager(feedListLayoutManager);
    }

    private void intializeViews() {
        button = findViewById(R.id.button);
    }
}