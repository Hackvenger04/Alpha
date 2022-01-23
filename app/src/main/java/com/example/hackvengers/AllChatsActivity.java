package com.example.hackvengers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.hackvengers.chat.ChatAdapter;
import com.example.hackvengers.chat.ChatObject;
import com.example.hackvengers.user.UserObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class AllChatsActivity extends AppCompatActivity {



    public static RecyclerView.Adapter<ChatAdapter.ViewHolder> mChatListAdapter;
    RecyclerView mChatList;
    RecyclerView.LayoutManager mChatListLayoutManager;

    ArrayList<ChatObject> chatList;


    public static Context context;
    String curDate;
    DatabaseReference mUserDB;


    public static UserObject curUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_chats);
        context = this;

        String userkey= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        getUserDetails(userkey);


        boolean isFromFeed=getIntent().getBooleanExtra("isFromFeed",false);

//        curUser=FeedActivity.curUser;


        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("EEE, MMM dd, yyyy");
        curDate = simpleDateFormatDate.format(date).toUpperCase();


        chatList = new ArrayList<>();
        initializeViews();
        initializeRecyclerViews();
        getChatList();



        if(!isFromFeed){
            startActivity(new Intent(this,FeedActivity.class));
        }





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


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getChatList() {

        FirebaseUser mUser=FirebaseAuth.getInstance().getCurrentUser();
        mUserDB= FirebaseDatabase.getInstance().getReference().child("Users");

        if(mUser!=null){
            mUserDB=mUserDB.child(mUser.getUid()).child("chat");
            mUserDB.orderByValue().addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if(snapshot.exists() && snapshot.getKey()!=null){
                        getChatDetails(snapshot.getKey());
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
    }

    private void getChatDetails(final String key) {
        final DatabaseReference chatDb = FirebaseDatabase.getInstance().getReference().child("Chats").child(key);
        chatDb.child("info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {

                final String[] name = {""};
                final String[] imageUri = {""};
                final int[] numberOfUsers = {0};
                boolean isSingleChat = false;
                String curUserKey = curUser.getUid();

                if (snapshot.child("isSingleChat").getValue() != null) {
                    isSingleChat = true;
                }
                if (snapshot.child("Name").getValue() != null) {
                    name[0] = Objects.requireNonNull(snapshot.child("Name").getValue()).toString();
                }
                if (snapshot.child("Chat Profile Image Uri").getValue() != null) {
                    imageUri[0] = Objects.requireNonNull(snapshot.child("Chat Profile Image Uri").getValue()).toString();
                }
                if (snapshot.child(curUserKey).child("Name").getValue() != null) {
                    name[0] = Objects.requireNonNull(snapshot.child(curUserKey).child("Name").getValue()).toString();
                }
                if (snapshot.child(curUserKey).child("Chat Profile Image Uri").getValue() != null) {
                    imageUri[0] = Objects.requireNonNull(snapshot.child(curUserKey).child("Chat Profile Image Uri").getValue()).toString();
                }
                if (snapshot.child("Number Of Users").getValue() != null) {
                    numberOfUsers[0] = Integer.parseInt(Objects.requireNonNull(snapshot.child("Number Of Users").getValue()).toString());
                }


                chatDb.child("info/Name").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot nameSnapshot) {
                        if (nameSnapshot.exists()) {
                            ChatObject tempChat = new ChatObject(key);
                            int indexOfChat = chatList.indexOf(tempChat);

                            if (indexOfChat > -1) {
                                chatList.get(indexOfChat).setName(Objects.requireNonNull(nameSnapshot.getValue()).toString());
                                mChatListAdapter.notifyItemChanged(indexOfChat);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                chatDb.child("info").child(curUserKey).child("Chat Profile Image Uri").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot imageSnapshot) {
                        if (imageSnapshot.exists()) {
                            ChatObject tempChat = new ChatObject(key);
                            int indexOfChat = chatList.indexOf(tempChat);

                            if (indexOfChat > -1) {
                                chatList.get(indexOfChat).setImageUri(Objects.requireNonNull(imageSnapshot.getValue()).toString());
                                mChatListAdapter.notifyItemChanged(indexOfChat);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                chatDb.child("info/Chat Profile Image Uri").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot imageSnapshot) {
                        if (imageSnapshot.exists()) {
                            ChatObject tempChat = new ChatObject(key);
                            int indexOfChat = chatList.indexOf(tempChat);
                            if (indexOfChat > -1) {
                                chatList.get(indexOfChat).setImageUri(Objects.requireNonNull(imageSnapshot.getValue()).toString());
                                mChatListAdapter.notifyItemChanged(indexOfChat);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                final boolean finalIsSingleChat = isSingleChat;
                chatDb.child("info/user/" + curUserKey + "/lastMessageId").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot lastMessageSnapshot) {
                        if (lastMessageSnapshot.exists() && !Objects.requireNonNull(lastMessageSnapshot.getValue()).toString().equals("true")) {
                            String lastMessageId = Objects.requireNonNull(lastMessageSnapshot.getValue()).toString();
                            getMessageData(key, name[0], imageUri[0], numberOfUsers[0], finalIsSingleChat, lastMessageId);
                        } else {
                            ChatObject tempChat = new ChatObject(key);
                            int indexOfChat = chatList.indexOf(tempChat);
                            ChatObject chatObject = new ChatObject(key, name[0], imageUri[0], numberOfUsers[0], finalIsSingleChat);
                            if (indexOfChat > -1) {
                                chatList.set(indexOfChat, chatObject);
                                mChatListAdapter.notifyItemChanged(indexOfChat);
                            } else {
                                chatList.add(chatObject);
                                mChatListAdapter.notifyItemInserted(chatList.size() - 1);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void getMessageData(final String key, final String name, final String imageUri, final int numberOfUsers, final boolean finalIsSingleChat, final String lastMessageId) {
        DatabaseReference messageDB = FirebaseDatabase.getInstance().getReference().child("Chats").child(key).child("Messages").child(lastMessageId);
        messageDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String lastSenderId = "";
                    String lastSenderName = "";
                    String lastMessageText = "Photo";
                    String lastMessageTime = "";
                    String lastMessageDate = "";

                    if (snapshot.child("Sender").getValue() != null) {
                        lastSenderId = Objects.requireNonNull(snapshot.child("Sender").getValue()).toString();
                    }
                    if (snapshot.child("Sender Name").getValue() != null) {
                        lastSenderName = Objects.requireNonNull(snapshot.child("Sender Name").getValue()).toString();
                    }
                    if (snapshot.child("text").getValue() != null) {
                        lastMessageText = Objects.requireNonNull(snapshot.child("text").getValue()).toString();
                    }
                    if (snapshot.child("timestamp").getValue() != null) {
                        lastMessageTime = Objects.requireNonNull(snapshot.child("timestamp").getValue()).toString();
                    }
                    if (snapshot.child("date").getValue() != null) {
                        lastMessageDate = Objects.requireNonNull(snapshot.child("date").getValue()).toString();
                    }
                    if (!lastMessageDate.equals(curDate)) {
                        lastMessageTime = lastMessageTime + " " + lastMessageDate;
                    }

                    if (snapshot.child("Deleted For Everyone").getValue() != null) {
                        if (Objects.requireNonNull(snapshot.child("Deleted For Everyone").getValue()).toString().equals(curUser.getUid())) {
                            lastMessageText = "You Deleted this Message";
                        } else {
                            lastMessageText = "This Message was Deleted";
                        }
                    }


                    FirebaseDatabase.getInstance().getReference().child("Chats/" + key + "/Messages/" + lastMessageId + "/Deleted For Everyone").addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                ChatObject chatObject = new ChatObject(key);
                                int indexOfChat = chatList.indexOf(chatObject);
                                String deletedText = "This Message Was Deleted";
                                if (Objects.requireNonNull(snapshot.getValue()).toString().equals(AllChatsActivity.curUser.getUid())) {
                                    deletedText = "You Deleted This Message";
                                }
                                if (indexOfChat > -1 && lastMessageId.equals(chatList.get(indexOfChat).getLastMessageId())) {
                                    chatList.get(indexOfChat).setLastMessageText(deletedText);
                                    mChatListAdapter.notifyItemChanged(indexOfChat);
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                    ChatObject chatObject = new ChatObject(key, name, imageUri, lastMessageText, lastSenderName, lastSenderId, lastMessageTime, numberOfUsers, lastMessageId, finalIsSingleChat);
                    int indexOfObject = chatList.indexOf(chatObject);
                    if (indexOfObject == -1) {
                        chatList.add(chatObject);
                        mChatListAdapter.notifyItemInserted(chatList.size() - 1);
                    } else {
                        chatList.remove(indexOfObject);
                        chatList.add(0, chatObject);
                        mChatListAdapter.notifyItemRangeChanged(0, indexOfObject + 1);
                    }
                } else {
                    getMessageData(key, name, imageUri, numberOfUsers, finalIsSingleChat, lastMessageId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }






    private void requestUserPermission() {
        String[] permissions={Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE};
        requestPermissions(permissions,1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(getApplicationContext(), "you didn't give the permission to access the contacts, So Fuck Off", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }







    private void viewProfile() {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra("userObject", curUser);
        startActivity(intent);
    }

    private void createNewGroup() {
        Intent intent = new Intent(this, CreateNewChatActivity.class);
        intent.putExtra("isSingleChatActivity", false);
        startActivity(intent);
    }

    private void singleChats() {
        Intent intent = new Intent(this, CreateNewChatActivity.class);
        intent.putExtra("isSingleChatActivity", true);
        startActivity(intent);
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }








    private void initializeViews() {
    }

    private void initializeRecyclerViews() {

        mChatList = findViewById(R.id.recyclerViewListChats);
        mChatList.setHasFixedSize(false);
        mChatList.setNestedScrollingEnabled(false);

        mChatList.addItemDecoration(new DividerItemDecoration(mChatList.getContext(), DividerItemDecoration.VERTICAL));


        mChatListAdapter = new ChatAdapter(chatList, this);
        mChatListAdapter.setHasStableIds(true);
        mChatList.setAdapter(mChatListAdapter);

        mChatListLayoutManager=new LinearLayoutManager(getApplicationContext(),RecyclerView.VERTICAL,false);
        mChatList.setLayoutManager(mChatListLayoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.all_chats_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logoutMenu:
                logOut();
                break;

            case R.id.createNewGroupMenu:
                createNewGroup();
                break;


            case R.id.viewProfileMenu:
                viewProfile();
                break;

            case R.id.singleChatsMenu:
                singleChats();
                break;


            default:
                Toast.makeText(getApplicationContext(), "please select a valid option", Toast.LENGTH_SHORT).show();
                break;

        }
        return true;
    }
}