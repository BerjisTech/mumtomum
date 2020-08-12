package tech.berjis.mumtomum;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
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
import java.util.Objects;
import java.util.Random;

public class GroupFragmentChat extends Fragment {

    private static final int RESULT_OK = 200;
    private Context mContext;
    private DatabaseReference dbRef;
    private StorageReference storageReference;
    private Uri filePath;

    private String UID, group;
    private EmojiEditText ed_emoji;
    private ImageView btn_link, send, btn_emoji, back;
    private RecyclerView commentsRecycler;
    private List<Comments> listData;
    private CommentsAdapter adapter;
    private ConstraintLayout rootView;

    GroupFragmentChat(Context mContext, String group) {
        this.mContext = mContext;
        this.group = group;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        dbRef.keepSynced(true);

        UID = mAuth.getCurrentUser().getUid();

        send = view.findViewById(R.id.send);
        btn_link = view.findViewById(R.id.btn_link);
        commentsRecycler = view.findViewById(R.id.commentsRecycler);
        ed_emoji = view.findViewById(R.id.ed_emoji);
        btn_emoji = view.findViewById(R.id.btn_emoji);
        rootView = view.findViewById(R.id.root_view);

        commentsRecycler.setFocusable(false);

        final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(rootView).build(ed_emoji);
        btn_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggles visibility of the Popup.
                if (emojiPopup.isShowing()) {
                    Picasso.get().load(R.drawable.emoji).into(btn_emoji);
                    emojiPopup.toggle();
                } else {
                    Picasso.get().load(R.drawable.keyboard).into(btn_emoji);
                    emojiPopup.toggle();
                }
            }
        });

        //emojiPopup.dismiss();  Dismisses the Popup.
        //emojiPopup.isShowing();  Returns true when Popup is showing.

        listData = new ArrayList<>();

        commentsRecycler.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        adapter = new CommentsAdapter(listData);
        commentsRecycler.setAdapter(adapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chat();
            }
        });
        btn_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        loadChats();
        return view;
    }

    public void chat() {

        if (TextUtils.isEmpty(ed_emoji.getText().toString())) {
            Toast.makeText(mContext, "You can't send an empty message", Toast.LENGTH_SHORT).show();
        } else {

            long unixTime = System.currentTimeMillis() / 1000L;
            final DatabaseReference commentsRef_1 = dbRef.child("GroupChats").child(group).push();
            final String comment_key = commentsRef_1.getKey();
            HashMap<String, Object> sendChats_1 = new HashMap<>();

            sendChats_1.put("type", "text");
            sendChats_1.put("text", ed_emoji.getText().toString());
            sendChats_1.put("sender", UID);
            sendChats_1.put("group", group);
            sendChats_1.put("chat_id", comment_key);
            sendChats_1.put("date", unixTime);
            commentsRef_1.updateChildren(sendChats_1).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        ed_emoji.setText("");
                    } else {
                        Toast.makeText(mContext, "An error occured while sending your message", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    public void loadChats() {
        listData.clear();
        dbRef.child("GroupChats").child(group).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Comments messages = dataSnapshot.getValue(Comments.class);

                listData.add(messages);

                adapter.notifyDataSetChanged();

                commentsRecycler.smoothScrollToPosition(listData.size());
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

    private void selectImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(Objects.requireNonNull(getContext()), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
        }*/

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filePath = result.getUri();
                postImage();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(mContext, "Error : " + error, Toast.LENGTH_SHORT).show();
            } else {
                Exception error = result.getError();
                Toast.makeText(mContext, "Error : " + error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void postImage() {
        long unixTime = System.currentTimeMillis() / 1000L;
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        Random random = new Random();

        if (filePath != null) {

            final StorageReference ref = storageReference.child("Group Images/" + group + "/" + random + unixTime + ".jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUrl = uri;
                                    final String image_url = downloadUrl.toString();

                                    long unixTime = System.currentTimeMillis() / 1000L;
                                    final DatabaseReference commentsRef_1 = dbRef.child("GroupChats").child(group).push();
                                    final String comment_key = commentsRef_1.getKey();
                                    HashMap<String, Object> sendChats_1 = new HashMap<>();

                                    sendChats_1.put("type", "photo");
                                    sendChats_1.put("text", image_url);
                                    sendChats_1.put("sender", UID);
                                    sendChats_1.put("group", group);
                                    sendChats_1.put("chat_id", comment_key);
                                    sendChats_1.put("date", unixTime);
                                    commentsRef_1.updateChildren(sendChats_1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                ed_emoji.setText("");
                                            } else {
                                                Toast.makeText(mContext, "An error occured while sending your message", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                    progressDialog.dismiss();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(mContext, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(mContext, "Image Not Detected", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }
}
