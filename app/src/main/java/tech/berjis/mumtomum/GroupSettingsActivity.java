package tech.berjis.mumtomum;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupSettingsActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String UID, group = "";
    StorageReference storageReference;
    Uri filePath;

    EditText groupName, groupPurpose, groupGoal;
    TextView currency, save;
    CircleImageView groupImage, editImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_settings);

        initVars();
    }

    private void initVars() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        dbRef.keepSynced(true);

        groupName = findViewById(R.id.groupName);
        groupImage = findViewById(R.id.groupImage);
        editImage = findViewById(R.id.editImage);
        groupPurpose = findViewById(R.id.groupPurpose);
        currency = findViewById(R.id.currency);
        groupGoal = findViewById(R.id.groupGoal);
        save = findViewById(R.id.save);

        loadGroup();
    }

    private void loadGroup() {
        Intent groupIntent = getIntent();
        Bundle groupBundle = groupIntent.getExtras();
        assert groupBundle != null;
        group = groupBundle.getString("group_id");

        assert group != null;
        dbRef.child("Groups").child(group).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                String logo = Objects.requireNonNull(snapshot.child("logo").getValue()).toString();
                String goal = Objects.requireNonNull(snapshot.child("goal").getValue()).toString();
                String purpose = Objects.requireNonNull(snapshot.child("description").getValue()).toString();
                String code = Objects.requireNonNull(snapshot.child("code").getValue()).toString();
                String symbol = Objects.requireNonNull(snapshot.child("symbol").getValue()).toString();
                // String owner = Objects.requireNonNull(snapshot.child("owner").getValue()).toString();

                groupName.setText(name);
                groupPurpose.setText(purpose);
                groupGoal.setText(goal);
                Glide.with(GroupSettingsActivity.this).load(logo).thumbnail(0.25f).into(groupImage);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    currency.setText(Html.fromHtml(code + " <small>(" + symbol + ")</small>", Html.FROM_HTML_MODE_COMPACT));
                } else {
                    currency.setText(Html.fromHtml(code + " <small>(" + symbol + ")</small>"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        staticOnClicks();
    }

    private void staticOnClicks() {
        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGroupInfo();
            }
        });
    }

    private void selectImage() {
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

    private void postImage() {
        long unixTime = System.currentTimeMillis() / 1000L;
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        if (filePath != null) {

            final StorageReference ref = storageReference.child("Group Images/" + UID + "_group_" + unixTime + ".jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String image_url = uri.toString();
                                    Picasso.get().load(image_url).into(groupImage);
                                    dbRef.child("MyGroups").child(UID).child(group).child("logo").setValue(image_url);
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(GroupSettingsActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        } else {
            progressDialog.dismiss();
        }
    }

    private void saveGroupInfo() {

        String name = groupName.getText().toString();
        String purpose = groupPurpose.getText().toString();
        String goal = groupGoal.getText().toString();

        dbRef.child("MyGroups").child(UID).child(group).child("name").setValue(name);

        dbRef.child("Groups").child(group).child("name").setValue(name);
        dbRef.child("Groups").child(group).child("description").setValue(purpose);
        dbRef.child("Groups").child(group).child("goal").setValue(Long.parseLong(goal));
    }

}
