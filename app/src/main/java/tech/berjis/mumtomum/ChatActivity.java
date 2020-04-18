package tech.berjis.mumtomum;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    public CircleImageView userImage;
    public EmojiTextView userName;
    public EmojiEditText ed_emoji;
    public View rootView;
    public ImageView link, send, btn_emoji;
    public RecyclerView chats;
    public FirebaseAuth mAuth;
    public DatabaseReference dbRef;
    public String uid;
    public List<Chats> listData;
    public ChatsAdapter adapter;
    public StorageReference storageReference;
    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        uid = mAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();

        userImage = findViewById(R.id.userImage);
        userName = findViewById(R.id.userName);
        link = findViewById(R.id.link);
        send = findViewById(R.id.send);
        chats = findViewById(R.id.chats);
        ed_emoji = findViewById(R.id.ed_emoji);
        btn_emoji = findViewById(R.id.btn_emoji);
        rootView = findViewById(R.id.root_view);

        final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(rootView).build(ed_emoji);
        btn_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiPopup.toggle(); // Toggles visibility of the Popup.
                if (emojiPopup.isShowing()) {
                    Picasso.get().load(R.drawable.lol).into(btn_emoji);
                } else {
                    Picasso.get().load(R.drawable.keyboard).into(btn_emoji);
                    emojiPopup.dismiss();
                }
            }
        });
        //emojiPopup.dismiss();  Dismisses the Popup.
        //emojiPopup.isShowing();  Returns true when Popup is showing.

        listData = new ArrayList<>();

        chats.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new ChatsAdapter(listData);
        chats.setAdapter(adapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chat();
            }
        });

        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        loadFriendData();
        changeChatState();
        loadChats();
    }

    public void loadFriendData() {
        Intent userIntent = getIntent();
        Bundle userData = userIntent.getExtras();
        String chatting_with = userData.getString("chatting_with");
        dbRef.child("Users").child(chatting_with).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName.setText(dataSnapshot.child("name").getValue().toString());
                if(dataSnapshot.child("image").exists()) {
                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(userImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void chat() {

        Intent userIntent = getIntent();
        Bundle userData = userIntent.getExtras();
        final String chatting_with = userData.getString("chatting_with");
        if (TextUtils.isEmpty(ed_emoji.getText())) {
            Toast.makeText(this, "You can't send an empty message", Toast.LENGTH_SHORT).show();
        } else {

            Calendar calendar = Calendar.getInstance();

            @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
            final String date = currentDate.format(calendar.getTime());

            @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
            final String time = currentTime.format(calendar.getTime());

            long unixTime = System.currentTimeMillis() / 1000L;

            final String timeStamp = new SimpleDateFormat("dd MMM, yyyy ( h:mm a )").format(Calendar.getInstance().getTime());

            dbRef.child("ChatsMetaData").child(uid).child(chatting_with).child("last_update").setValue(unixTime);
            dbRef.child("ChatsMetaData").child(chatting_with).child(uid).child("last_update").setValue(unixTime);

            DatabaseReference sender = dbRef.child("Chats").child(uid).child(chatting_with).push();
            final DatabaseReference receiver = dbRef.child("Chats").child(chatting_with).child(uid).push();

            final String senderKey = sender.getKey();
            final String receiverKey = receiver.getKey();

            HashMap<String, String> sendchats = new HashMap<>();

            sendchats.put("type", "text");
            sendchats.put("text", ed_emoji.getText().toString());
            sendchats.put("sender", uid);
            sendchats.put("receiver", chatting_with);
            sendchats.put("chat_id", senderKey);
            sendchats.put("date", date);
            sendchats.put("time", time);
            sendchats.put("read", "false");

            sender.setValue(sendchats).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        HashMap<String, String> receivechats = new HashMap<>();

                        receivechats.put("type", "text");
                        receivechats.put("text", ed_emoji.getText().toString());
                        receivechats.put("sender", uid);
                        receivechats.put("receiver", chatting_with);
                        receivechats.put("chat_id", receiverKey);
                        receivechats.put("date", date);
                        receivechats.put("time", time);
                        receivechats.put("read", "false");

                        receiver.setValue(receivechats).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    HashMap<String, String> chatNotification = new HashMap<>();

                                    chatNotification.put("title", "New Message");
                                    chatNotification.put("from", uid);
                                    chatNotification.put("image", "");
                                    chatNotification.put("body", ed_emoji.getText().toString());
                                    chatNotification.put("time", timeStamp);
                                    chatNotification.put("type", "chat");

                                    dbRef.child("Notifications").child(chatting_with).push().setValue(chatNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                            }
                                        }
                                    });

                                }
                                ed_emoji.setText("");
                            }
                        });
                    }
                }
            });
        }
    }

    public void loadChats() {
        listData.clear();
        Intent userIntent = getIntent();
        Bundle userData = userIntent.getExtras();
        String chatting_with = userData.getString("chatting_with");
        dbRef.child("Chats").child(chatting_with).child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chats messages = dataSnapshot.getValue(Chats.class);

                listData.add(messages);

                adapter.notifyDataSetChanged();

                chats.smoothScrollToPosition(listData.size());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void selectImage() {
        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);*/

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
        }*/

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filePath = result.getUri();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error : " + error, Toast.LENGTH_SHORT).show();
            }
        }

        postImage();
    }

    public void postImage() {

        Intent userIntent = getIntent();
        Bundle userData = userIntent.getExtras();
        final String chatting_with = userData.getString("chatting_with");
        final String timeStamp = new SimpleDateFormat("dd MMM, yyyy ( h:mm a )").format(Calendar.getInstance().getTime());

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        Calendar calendar = Calendar.getInstance();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        final String date = currentDate.format(calendar.getTime());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        final String time = currentTime.format(calendar.getTime());

        long unixTime = System.currentTimeMillis() / 1000L;
        dbRef.child("ChatsMetaData").child(uid).child(chatting_with).child("last_update").setValue(unixTime);
        dbRef.child("ChatsMetaData").child(chatting_with).child(uid).child("last_update").setValue(unixTime);

        final DatabaseReference sender = dbRef.child("Chats").child(uid).child(chatting_with).push();
        final DatabaseReference receiver = dbRef.child("Chats").child(chatting_with).child(uid).push();

        final String senderKey = sender.getKey();
        final String receiverKey = receiver.getKey();

        if (filePath != null) {

            final StorageReference ref = storageReference.child("Chat Images/" + unixTime + ".jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUrl = uri;
                                    final String image_url = downloadUrl.toString();

                                    HashMap<String, String> sendchats = new HashMap<>();

                                    sendchats.put("type", "image");
                                    sendchats.put("text", image_url);
                                    sendchats.put("sender", uid);
                                    sendchats.put("receiver", chatting_with);
                                    sendchats.put("chat_id", senderKey);
                                    sendchats.put("date", date);
                                    sendchats.put("time", time);
                                    sendchats.put("read", "false");

                                    sender.setValue(sendchats).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String, String> receivechats = new HashMap<>();

                                                receivechats.put("type", "image");
                                                receivechats.put("text", image_url);
                                                receivechats.put("sender", uid);
                                                receivechats.put("receiver", chatting_with);
                                                receivechats.put("chat_id", receiverKey);
                                                receivechats.put("date", date);
                                                receivechats.put("time", time);
                                                receivechats.put("read", "false");

                                                receiver.setValue(receivechats).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            if (task.isSuccessful()) {

                                                                HashMap<String, String> chatNotification = new HashMap<>();

                                                                chatNotification.put("title", "New Message");
                                                                chatNotification.put("from", uid);
                                                                chatNotification.put("image", image_url);
                                                                chatNotification.put("body", "New Image \uD83D\uDDBC ");
                                                                chatNotification.put("time", timeStamp);
                                                                chatNotification.put("type", "Image");

                                                                dbRef.child("Notifications").child(chatting_with).push().setValue(chatNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {

                                                                        }
                                                                    }
                                                                });

                                                                progressDialog.dismiss();
                                                            } else {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(ChatActivity.this, "There was an error posting your job", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ChatActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        } else {
            progressDialog.dismiss();
        }
    }

    public void changeChatState() {


        Intent userIntent = getIntent();
        Bundle userData = userIntent.getExtras();
        final String chatting_with = userData.getString("chatting_with");

        dbRef.child("Chats").child(uid).child(chatting_with).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                    if (npsnapshot.hasChildren()) {

                        if (npsnapshot.child("read").getValue().toString().equals("false")) {
                            String chat_id = npsnapshot.getKey();
                            dbRef.child("Chats").child(uid).child(chatting_with).child(chat_id).child("read").setValue("true");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
