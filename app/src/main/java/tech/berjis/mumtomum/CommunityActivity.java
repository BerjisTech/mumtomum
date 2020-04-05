package tech.berjis.mumtomum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CommunityActivity extends AppCompatActivity {

    ImageView newPost, back, newImage, send, lol;
    TextView closeGossipPanel;
    EmojiEditText gossip;
    ConstraintLayout gossipPanel;

    FirebaseAuth mAuth;
    DatabaseReference dbRef, gossipRef;
    StorageReference storageReference;
    Uri filePath;
    RecyclerView imageRecycler, postRecycler;
    List<GossipImages> gossipImagesData;
    List<Gossips> gossipData;
    GossipImagesAdapter imagesAdapter;
    GossipsAdapter gossipAdapter;
    String UID, gossipID, location = "", hasImage = "", water = "false", electricity = "false", parking = "false", security = "false", panelState = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        mAuth = FirebaseAuth.getInstance();
        UID = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        dbRef.keepSynced(true);

        gossipPanel = findViewById(R.id.gossipPanel);
        closeGossipPanel = findViewById(R.id.closeGossipPanel);
        back = findViewById(R.id.back);
        newPost = findViewById(R.id.newPost);
        send = findViewById(R.id.send);
        lol = findViewById(R.id.lol);
        gossip = findViewById(R.id.gossip);
        newImage = findViewById(R.id.newImage);
        imageRecycler = findViewById(R.id.imageRecycler);
        postRecycler = findViewById(R.id.postRecycler);

        gossipImagesData = new ArrayList<>();
        gossipData = new ArrayList<>();
        imageRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        imageRecycler.setHasFixedSize(true);
        postRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        postRecycler.setHasFixedSize(true);


        loadGossip();
        newPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGossip();
            }
        });
        closeGossipPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideGossip();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommunityActivity.super.finish();
            }
        });

        newImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postGossip();
            }
        });

        final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(gossipPanel).build(gossip);
        lol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiPopup.toggle(); // Toggles visibility of the Popup.
                if (emojiPopup.isShowing()) {
                    Picasso.get().load(R.drawable.lol).into(lol);
                } else {
                    Picasso.get().load(R.drawable.keyboard).into(lol);
                    emojiPopup.dismiss();
                }
            }
        });
        //emojiPopup.dismiss();  Dismisses the Popup.
        //emojiPopup.isShowing();  Returns true when Popup is showing.

    }

    public void hideGossip() {
        panelState = "closed";
        removeGossipNode();
        loadGossip();
        gossipImagesData.clear();
        gossip.setText("");
        imageRecycler.setVisibility(View.GONE);
        gossipPanel.animate()
                .translationY(gossipPanel.getHeight())
                .alpha(0.0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        gossipPanel.setVisibility(View.GONE);
                    }
                });
    }

    public void showGossip() {
        panelState = "open";
        gossipPanel.setVisibility(View.VISIBLE);
        gossipPanel.animate()
                .translationY(0)
                .alpha(1.0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        createGossipNode();
                    }
                });
    }

    public void createGossipNode() {
        gossipRef = dbRef.child("Gossip").push();
        gossipID = gossipRef.getKey();

        long unixTime = System.currentTimeMillis() / 1000L;
        gossipRef.child("gossipID").setValue(gossipID);
        gossipRef.child("user").setValue(UID);
        gossipRef.child("date").setValue(Math.toIntExact(unixTime));
        gossipRef.child("type").setValue("original");
    }

    public void loadImages() {
        imageRecycler.setVisibility(View.VISIBLE);
        gossipImagesData.clear();
        dbRef.child("GossipImages").child(gossipID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        GossipImages l = npsnapshot.getValue(GossipImages.class);
                        gossipImagesData.add(l);
                    }
                    Collections.reverse(gossipImagesData);
                    imagesAdapter = new GossipImagesAdapter(gossipImagesData, "show");
                    imageRecycler.setAdapter(imagesAdapter);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CommunityActivity.this, "Kuna shida mahali", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadGossip() {
        postRecycler.setVisibility(View.VISIBLE);
        gossipData.clear();
        dbRef.child("Gossip").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        Gossips l = npsnapshot.getValue(Gossips.class);
                        gossipData.add(l);
                    }
                    Collections.reverse(gossipData);
                    gossipAdapter = new GossipsAdapter(gossipData);
                    postRecycler.setAdapter(gossipAdapter);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CommunityActivity.this, "Kuna shida mahali", Toast.LENGTH_SHORT).show();
            }
        });
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

        long unixTime = System.currentTimeMillis() / 1000L;
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        if (filePath != null) {

            final StorageReference ref = storageReference.child("Gossip Images/" + gossipID + unixTime + ".jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUrl = uri;
                                    final String image_url = downloadUrl.toString();

                                    DatabaseReference imageRef = dbRef.child("GossipImages").child(gossipID).push();
                                    String image_id = imageRef.getKey();
                                    imageRef.child("image_id").setValue(image_id);
                                    imageRef.child("gossip_id").setValue(gossipID);
                                    imageRef.child("image").setValue(image_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                hasImage = "hasImage";
                                                loadImages();
                                                progressDialog.dismiss();
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
                            Toast.makeText(CommunityActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        goBack();
    }

    public void goBack() {
        if(panelState.equals("open")){
            removeGossipNode();
            CommunityActivity.super.finish();
        }else{
            CommunityActivity.super.finish();
        }
    }

    public void postGossip() {
        final String gossipText = gossip.getText().toString();
        if (!hasImage.equals("hasImage") && gossipText.equals("")) {
            Drawable customErrorDrawable = getResources().getDrawable(R.drawable.error);
            customErrorDrawable.setBounds(0, 0, customErrorDrawable.getIntrinsicWidth(), customErrorDrawable.getIntrinsicHeight());
            gossip.setError("Sema kitu mama naniiiii", customErrorDrawable);
            return;
        }

        HashMap<String, Object> gossipHash = new HashMap<>();

        gossipHash.put("gossip", gossipText);

        gossipRef.updateChildren(gossipHash).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    gossipID = "";
                    hideGossip();
                    View view = CommunityActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        });
    }

    public void removeGossipNode() {
        final String gossipText = gossip.getText().toString();
        if (!hasImage.equals("hasImage") && gossipText.equals("") && !gossipID.equals("")) {
            dbRef.child("Gossip").child(gossipID).removeValue();
            dbRef.child("GossipImages").child(gossipID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (final DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(npsnapshot.child("image").getValue().toString());
                            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // File deleted successfully
                                    dbRef.child("GossipImages").child(gossipID).child(npsnapshot.child("image_id").getValue().toString()).removeValue();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Uh-oh, an error occurred!
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(CommunityActivity.this, "Kuna shida mahali", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
