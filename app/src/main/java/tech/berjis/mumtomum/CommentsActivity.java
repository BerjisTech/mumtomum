package tech.berjis.mumtomum;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    DatabaseReference dbRef;
    FirebaseAuth mAuth;

    String gossipID, UID;
    public EmojiEditText ed_emoji;
    public ImageView link, send, btn_emoji;
    public RecyclerView comments;
    public List<Comments> listData;
    public CommentsAdapter adapter;
    public StorageReference storageReference;
    private Uri filePath;
    public ConstraintLayout rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        dbRef.keepSynced(true);

        UID = mAuth.getCurrentUser().getUid();

        Intent gossipIntent = getIntent();
        Bundle gossipBundle = gossipIntent.getExtras();
        gossipID = gossipBundle.getString("gossipID");

        link = findViewById(R.id.link);
        send = findViewById(R.id.send);
        comments = findViewById(R.id.comments);
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

        comments.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new CommentsAdapter(listData);
        comments.setAdapter(adapter);

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
        loadChats();

    }

    public void chat() {

        if (TextUtils.isEmpty(ed_emoji.getText().toString())) {
            Toast.makeText(this, "You can't send an empty message", Toast.LENGTH_SHORT).show();
        } else {

            long unixTime = System.currentTimeMillis() / 1000L;
            final DatabaseReference commentsRef_1 = dbRef.child("Comments").child(gossipID).push();
            final String comment_key = commentsRef_1.getKey();
            HashMap<String, Object> sendChats_1 = new HashMap<>();

            sendChats_1.put("type", "text");
            sendChats_1.put("text", ed_emoji.getText().toString());
            sendChats_1.put("sender", UID);
            sendChats_1.put("gossip_id", gossipID);
            sendChats_1.put("chat_id", comment_key);
            sendChats_1.put("date", unixTime);
            commentsRef_1.updateChildren(sendChats_1).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        ed_emoji.setText("");
                    }else{
                        Toast.makeText(CommentsActivity.this, "An error occured while sending your message", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    public void loadChats() {
        listData.clear();
        dbRef.child("Comments").child(gossipID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Comments messages = dataSnapshot.getValue(Comments.class);

                listData.add(messages);

                adapter.notifyDataSetChanged();

                comments.smoothScrollToPosition(listData.size());
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

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        final long unixTime = System.currentTimeMillis() / 1000L;

        final DatabaseReference commentsRef = dbRef.child("Comments").child(gossipID).push();

        final String comment_key = commentsRef.getKey();

        if (filePath != null) {

            final StorageReference ref = storageReference.child("Comment Images/" + unixTime + ".jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUrl = uri;
                                    final String image_url = downloadUrl.toString();

                                    HashMap<String, Object> sendchats = new HashMap<>();

                                    sendchats.put("type", "image");
                                    sendchats.put("text", image_url);
                                    sendchats.put("sender", UID);
                                    sendchats.put("gossip_id", gossipID);
                                    sendchats.put("chat_id", comment_key);
                                    sendchats.put("date", unixTime);

                                    commentsRef.updateChildren(sendchats).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                /*HashMap<String, Object> chatNotification = new HashMap<>();

                                                chatNotification.put("title", "New Message");
                                                chatNotification.put("from", UID);
                                                chatNotification.put("image", image_url);
                                                chatNotification.put("body", "New Comment");
                                                chatNotification.put("time", unixTime);
                                                chatNotification.put("type", "Image");

                                                dbRef.child("Notifications").child(chatting_with).push().setValue(chatNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                        }
                                                    }
                                                });*/

                                                progressDialog.dismiss();
                                            } else {
                                                progressDialog.dismiss();
                                                Toast.makeText(CommentsActivity.this, "There was an error posting your job", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(CommentsActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

}
