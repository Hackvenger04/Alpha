package com.example.hackvengers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hackvengers.user.UserAdapter;
import com.example.hackvengers.user.UserObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class CreateNewChatActivity extends AppCompatActivity {


    Button mCreateChat;
    EditText mChatName;

    static Context context;

    RecyclerView mUserList;
    RecyclerView.Adapter<UserAdapter.ViewHolder> mUserListAdapter;
    RecyclerView.LayoutManager mUserListLayoutManager;

    ArrayList<UserObject> userList;

    boolean isSingleChatActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_chat);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        userList = new ArrayList<>();
        context=this;

        isSingleChatActivity = getIntent().getBooleanExtra("isSingleChatActivity", false);

        initializeViews();
        initializeRecyclerViews();

        if (isSingleChatActivity) {
            mCreateChat.setVisibility(View.GONE);
            mChatName.setVisibility(View.GONE);
        }

        if(!isSingleChatActivity) {
            mCreateChat.setOnClickListener(v -> {
                if (mChatName != null) {
                    String s = mChatName.getText().toString().trim();
                    if (s.length() != 0) {
                        createChat();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Give the name to the chat ", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        getUserList();
    }

    private void getUserList() {


        FirebaseDatabase.getInstance().getReference().child("Users").child(AllChatsActivity.curUser.getUid()).child("Connections").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot childSnapshot:snapshot.getChildren()){
                        getUserDetails(childSnapshot.getKey(),childSnapshot.getValue().toString());
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private void getUserDetails(String userKey,String chatId){
        if(chatId.equals("true")){
            chatId="";
        }

        String finalChatId = chatId;
        FirebaseDatabase.getInstance().getReference().child("Users").child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = "";
                    String userPhone = "";
                    String userImage = "";
                    String userStatus = "";




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


                    UserObject newUser = new UserObject(userKey, userName, userPhone, userStatus, userImage, finalChatId);
                    userList.add(newUser);
                    mUserListAdapter.notifyItemInserted(userList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }





    private void createChat() {

        DatabaseReference mChatDB = FirebaseDatabase.getInstance().getReference().child("Chats");
        String key = mChatDB.push().getKey();
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");

        assert key != null;
        mChatDB = mChatDB.child(key).child("info");

        HashMap<String,Object> mChatInfo=new HashMap<>();

        int noOfUsers=1;
        Date date= Calendar.getInstance().getTime();

        userList.add(AllChatsActivity.curUser);
        for(UserObject user:userList){
            if (user.isSelected() && user.getUid() != null) {
                noOfUsers++;
                mChatInfo.put("user/" + user.getUid() + "/lastMessageId", true);
                mUserDB.child(user.getUid()).child("chat").child(key).setValue(-date.getTime());
            }
        }
        mChatInfo.put("Name",mChatName.getText().toString());
        mChatInfo.put("ID",key);
        mChatInfo.put("Number Of Users",noOfUsers);

        FirebaseUser mUser= FirebaseAuth.getInstance().getCurrentUser();
        if(mUser!=null) {
            mChatInfo.put("user/" + mUser.getUid() + "/lastMessageId", true);
            mUserDB.child(mUser.getUid()).child("chat").child(key).setValue(-date.getTime());
        }
        mChatDB.updateChildren(mChatInfo);
    }



    private void initializeViews() {
        mCreateChat = findViewById(R.id.newChat);
        mChatName=findViewById(R.id.chatName);
    }
    private void initializeRecyclerViews() {
        mUserList = findViewById(R.id.recyclerViewList);
        mUserList.setHasFixedSize(false);
        mUserList.setNestedScrollingEnabled(false);

        mUserListAdapter = new UserAdapter(userList, this, isSingleChatActivity, false);
        mUserList.setAdapter(mUserListAdapter);

        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        mUserList.setLayoutManager(mUserListLayoutManager);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                Toast.makeText(getApplicationContext(), "choose a valid button", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}