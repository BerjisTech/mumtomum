package tech.berjis.mumtomum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.iid.FirebaseInstanceId;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView editprofileImage, profileImage;
    EmojiTextView userName;
    ImageView editUser, goHome, contributions, products, text, lol, terms, privacy, info, contact_us, faq, share;
    TextView termsText, privacyText, infoText, contact_usText, faqText, shareText, cancel, save;
    View view;
    EmojiEditText editUserName, editFirstName, editLastName, editEmail, editMpesaNumber;
    ConstraintLayout editnameView;
    ScrollView scrollShit;

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String UID;
    StorageReference storageReference;
    Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        storageReference = FirebaseStorage.getInstance().getReference();
        UID = mAuth.getCurrentUser().getUid();

        editprofileImage = findViewById(R.id.editprofileImage);
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
        editUser = findViewById(R.id.editUser);
        goHome = findViewById(R.id.goHome);
        contributions = findViewById(R.id.contributions);
        products = findViewById(R.id.products);
        text = findViewById(R.id.text);
        lol = findViewById(R.id.lol);
        save = findViewById(R.id.save);
        cancel = findViewById(R.id.cancel);
        editUserName = findViewById(R.id.editUserName);
        editnameView = findViewById(R.id.editnameView);
        view = findViewById(R.id.view);
        termsText = findViewById(R.id.termsText);
        terms = findViewById(R.id.terms);
        privacyText = findViewById(R.id.privacyText);
        privacy = findViewById(R.id.privacy);
        infoText = findViewById(R.id.infoText);
        info = findViewById(R.id.info);
        contact_usText = findViewById(R.id.contact_usText);
        contact_us = findViewById(R.id.contact_us);
        faqText = findViewById(R.id.faqText);
        faq = findViewById(R.id.faq);
        shareText = findViewById(R.id.shareText);
        share = findViewById(R.id.share);
        scrollShit = findViewById(R.id.scrollShit);
        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editEmail = findViewById(R.id.editEmail);
        editMpesaNumber = findViewById(R.id.editMpesaNumber);

        final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(editnameView).build(editUserName);
        lol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emojiPopup.isShowing()) {
                    Picasso.get().load(R.drawable.lol).into(lol);
                    emojiPopup.toggle();
                } else {
                    Picasso.get().load(R.drawable.keyboard).into(lol);
                    emojiPopup.toggle();
                }
            }
        });
        //emojiPopup.dismiss();  Dismisses the Popup.
        //emojiPopup.isShowing();  Returns true when Popup is showing.

        checkName();
        loadUserProfile();


        editUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditUser();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideEditUser();
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSelector();
            }
        });
        editprofileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserName();
            }
        });
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, Messaging.class));
            }
        });
        products.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userIntent = new Intent(ProfileActivity.this, UserProducts.class);
                Bundle userBundle = new Bundle();
                userBundle.putString("user_id", UID);
                userIntent.putExtras(userBundle);
                startActivity(userIntent);
            }
        });
        contributions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, CommunityActivity.class));
            }
        });
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, WalletActivity.class));
            }
        });

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, TermsActivity.class));
            }
        });
        termsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, TermsActivity.class));
            }
        });

        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, PrivacyActivity.class));
            }
        });
        privacyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, PrivacyActivity.class));
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, AppInfo.class));
            }
        });
        infoText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, AppInfo.class));
            }
        });

        contact_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, ContactActivity.class));
            }
        });
        contact_usText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, ContactActivity.class));
            }
        });

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, Faq.class));
            }
        });
        faqText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, Faq.class));
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareBody = "Hi. Buy and sell baby stuff and maternity necessities easily with MumToMum\nhttps://berjis.tech/mum";
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share app");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share with"));
            }
        });
        shareText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareBody = "Hi. Buy and sell baby stuff and maternity necessities easily with MumToMum\nhttps://berjis.tech/mum";
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share app");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share with"));
            }
        });
    }

    public void showEditUser() {
        editnameView.setVisibility(View.VISIBLE);
        editnameView.animate()
                .alpha(0.0f)
                .setDuration(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        editnameView.animate()
                                .alpha(1.0f)
                                .setDuration(300)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        scrollShit.setVisibility(View.GONE);

                                    }
                                });
                    }
                });
    }

    public void hideEditUser() {
        editnameView.animate()
                .alpha(0.0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        editnameView.setVisibility(View.GONE);
                        scrollShit.setVisibility(View.VISIBLE);
                    }
                });
    }

    public void checkName() {

        dbRef.child("Users").child(UID).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(ProfileActivity.this, "Please choose a username", Toast.LENGTH_SHORT).show();
                    showEditUser();
                }
                if (dataSnapshot.exists()) {
                    saveToken(UID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void saveUserName() {
        final String newName = editUserName.getText().toString();
        if (newName.isEmpty()) {
            Toast.makeText(this, "You need to type something here", Toast.LENGTH_SHORT).show();
        } else {
            dbRef.child("Users").child(UID).child("name").setValue(newName).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isComplete()) {
                        Toast.makeText(ProfileActivity.this, "Hi " + newName, Toast.LENGTH_SHORT).show();
                        userName.setText(newName);
                        editUserName.setText(newName);
                        hideEditUser();
                    }
                }
            });
        }

        if(!editUserName.getText().toString().isEmpty()){
            dbRef.child("Users").child(UID).child("name").setValue(editUserName.getText().toString());
        }

        if(!editFirstName.getText().toString().isEmpty()){
            dbRef.child("Users").child(UID).child("first_name").setValue(editFirstName.getText().toString());
        }

        if(!editLastName.getText().toString().isEmpty()){
            dbRef.child("Users").child(UID).child("last_name").setValue(editLastName.getText().toString());
        }

        if(!editEmail.getText().toString().isEmpty()){
            dbRef.child("Users").child(UID).child("email").setValue(editEmail.getText().toString());
        }

        if(!editMpesaNumber.getText().toString().isEmpty()){
            dbRef.child("Users").child(UID).child("mpesa_phone").setValue(editMpesaNumber.getText().toString());
        }
    }

    public void loadUserProfile() {
        dbRef.child("Users").child(UID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("image").exists()) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profileImage);
                    }
                    if (dataSnapshot.child("name").exists()) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        userName.setText(name);
                        editUserName.setText(name);
                    }
                    if (dataSnapshot.child("first_name").exists()) {
                        editFirstName.setText(dataSnapshot.child("first_name").getValue().toString());
                    }
                    if (dataSnapshot.child("last_name").exists()) {
                        editLastName.setText(dataSnapshot.child("last_name").getValue().toString());
                    }
                    if (dataSnapshot.child("email").exists()) {
                        editEmail.setText(dataSnapshot.child("email").getValue().toString());
                    }
                    if (dataSnapshot.child("mpesa_phone").exists()) {
                        editMpesaNumber.setText(dataSnapshot.child("mpesa_phone").getValue().toString());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void showImageSelector() {
        editprofileImage.setVisibility(View.VISIBLE);
        editprofileImage.animate()
                .alpha(0.0f)
                .setDuration(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        editprofileImage.animate()
                                .alpha(0.9f)
                                .setDuration(300)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        view.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                hideImageSelector();
                                            }
                                        });
                                    }
                                });
                    }
                });
    }

    public void hideImageSelector() {
        editprofileImage.animate()
                .alpha(0.0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        editprofileImage.setVisibility(View.GONE);
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
                .setAspectRatio(1, 1)
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

        if (filePath != null) {

            final StorageReference ref = storageReference.child("Profile Images/" + UID + ".jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUrl = uri;
                                    final String image_url = downloadUrl.toString();

                                    dbRef.child("Users").child(UID).child("image").setValue(image_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
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
                            Toast.makeText(ProfileActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void saveToken(String user) {
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        dbRef.child("Users").child(user).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                }
            }
        });
    }
}
