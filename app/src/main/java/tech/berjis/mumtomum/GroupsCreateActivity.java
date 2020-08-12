package tech.berjis.mumtomum;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.mynameismidori.currencypicker.CurrencyPicker;
import com.mynameismidori.currencypicker.CurrencyPickerListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsCreateActivity extends AppCompatActivity {

    TextView bigTitle, smallTitle, groupButton, nextButton, noGroupMessage, joinGroup, createGroup, orText;
    EditText groupText, groupGoal, groupDescription;
    ImageView groupVector, backButton;
    CircleImageView groupLogo;
    TextView groupCurrency;
    View half;
    String tab = "", UID, phone, group_logo = "", c_name = "KENYA", c_code = "KES", c_symbol = "Ksh";

    DatabaseReference dbRef;
    FirebaseAuth mAuth;
    StorageReference storageReference;
    Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_create);

        initLayout();
        loadCurrency();
        checkUser(UID);
        initOnClicks();

    }

    private void initLayout() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        UID = mAuth.getCurrentUser().getUid();
        phone = mAuth.getCurrentUser().getPhoneNumber();
        dbRef.keepSynced(true);

        bigTitle = findViewById(R.id.bigTitle);
        smallTitle = findViewById(R.id.smallTitle);
        groupButton = findViewById(R.id.groupButton);
        nextButton = findViewById(R.id.nextButton);
        noGroupMessage = findViewById(R.id.noGroupMessage);
        joinGroup = findViewById(R.id.joinGroup);
        createGroup = findViewById(R.id.createGroup);
        orText = findViewById(R.id.orText);
        groupText = findViewById(R.id.groupText);
        groupGoal = findViewById(R.id.groupGoal);
        groupCurrency = findViewById(R.id.groupCurrency);
        groupDescription = findViewById(R.id.groupDescription);
        groupLogo = findViewById(R.id.groupLogo);
        groupVector = findViewById(R.id.groupVector);
        backButton = findViewById(R.id.backButton);
        half = findViewById(R.id.half);
    }

    private void initOnClicks() {
        groupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateOrJoin();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tab.equals("createGroup")) {
                    showCreateOrJoin();
                }
                if (tab.equals("createGroupExist")) {
                    GroupsCreateActivity.super.finish();
                }
            }
        });
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateGroup();
            }
        });
        joinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GroupsCreateActivity.this, GroupsActivity.class));
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkGroupName(groupText.getText().toString());
                // nextButton.setVisibility(View.GONE);
            }
        });
        groupCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CurrencyPicker picker = CurrencyPicker.newInstance("Select Currency");  // dialog title
                picker.setListener(new CurrencyPickerListener() {
                    @Override
                    public void onSelectCurrency(String name, String code, String symbol, int flagDrawableResID) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            groupCurrency.setText(Html.fromHtml(symbol + " <small>(" + code + ")</small>", Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            groupCurrency.setText(Html.fromHtml(symbol + " <small>(" + code + ")</small>"));
                        }
                        c_name = name;
                        c_code = code;
                        c_symbol = symbol;
                        picker.dismiss();
                    }
                });
                picker.show(getSupportFragmentManager(), "CURRENCY_PICKER");
            }
        });
        groupLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    private void checkUser(final String user) {
        dbRef.child("MyGroups").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    showCreateGroupExistingUser();
                } else {
                    showWelcomeMessage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showWelcomeMessage() {
        tab = "welcome";
        bigTitle.setVisibility(View.VISIBLE);
        smallTitle.setVisibility(View.VISIBLE);
        groupButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
        noGroupMessage.setVisibility(View.GONE);
        joinGroup.setVisibility(View.GONE);
        createGroup.setVisibility(View.GONE);
        orText.setVisibility(View.GONE);
        groupText.setVisibility(View.GONE);
        groupGoal.setVisibility(View.GONE);
        groupCurrency.setVisibility(View.GONE);
        groupDescription.setVisibility(View.GONE);
        groupLogo.setVisibility(View.GONE);
        groupVector.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.GONE);
        half.setVisibility(View.GONE);
    }

    private void showCreateOrJoin() {
        tab = "createJoin";
        bigTitle.setVisibility(View.GONE);
        smallTitle.setVisibility(View.GONE);
        groupButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        noGroupMessage.setVisibility(View.VISIBLE);
        joinGroup.setVisibility(View.VISIBLE);
        createGroup.setVisibility(View.VISIBLE);
        orText.setVisibility(View.VISIBLE);
        groupText.setVisibility(View.GONE);
        groupCurrency.setVisibility(View.GONE);
        groupGoal.setVisibility(View.GONE);
        groupDescription.setVisibility(View.GONE);
        groupLogo.setVisibility(View.GONE);
        groupVector.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.GONE);
        half.setVisibility(View.VISIBLE);
    }

    private void showCreateGroup() {
        tab = "createGroup";
        bigTitle.setVisibility(View.GONE);
        smallTitle.setVisibility(View.GONE);
        groupButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.VISIBLE);
        noGroupMessage.setVisibility(View.GONE);
        joinGroup.setVisibility(View.GONE);
        createGroup.setVisibility(View.GONE);
        orText.setVisibility(View.GONE);
        groupText.setVisibility(View.VISIBLE);
        groupCurrency.setVisibility(View.VISIBLE);
        groupGoal.setVisibility(View.VISIBLE);
        groupDescription.setVisibility(View.VISIBLE);
        groupLogo.setVisibility(View.VISIBLE);
        groupVector.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        half.setVisibility(View.GONE);
    }

    private void showCreateGroupExistingUser() {
        tab = "createGroupExist";
        bigTitle.setVisibility(View.GONE);
        smallTitle.setVisibility(View.GONE);
        groupButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.VISIBLE);
        noGroupMessage.setVisibility(View.GONE);
        joinGroup.setVisibility(View.GONE);
        createGroup.setVisibility(View.GONE);
        orText.setVisibility(View.GONE);
        groupText.setVisibility(View.VISIBLE);
        groupGoal.setVisibility(View.VISIBLE);
        groupCurrency.setVisibility(View.VISIBLE);
        groupDescription.setVisibility(View.VISIBLE);
        groupLogo.setVisibility(View.VISIBLE);
        groupVector.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        half.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {

        if (tab.equals("welcome")) {
            GroupsCreateActivity.super.finish();
        }
        if (tab.equals("createJoin")) {
            showWelcomeMessage();
        }
        if (tab.equals("createGroup")) {
            showCreateOrJoin();
        }
        if (tab.equals("joinGroup")) {
            showCreateOrJoin();
        }
        if (tab.equals("createGroupExist")) {
            GroupsCreateActivity.super.finish();
        }
        if (tab.equals("groupsAll")) {
            GroupsCreateActivity.super.finish();
        }
        if (tab.equals("")) {
            GroupsCreateActivity.super.finish();
        }
    }

    private void checkGroupName(final String query) {
        String goal = groupGoal.getText().toString();
        String desc = groupDescription.getText().toString();

        if (query.equals("")) {
            groupText.setError("You need to add a group name");
            nextButton.setVisibility(View.VISIBLE);
            return;
        }
        if (goal.equals("")) {
            groupGoal.setError("You need to add a group goal eg 3000");
            nextButton.setVisibility(View.VISIBLE);
            return;
        }
        if (desc.equals("")) {
            groupDescription.setError("You need to add a group description");
            nextButton.setVisibility(View.VISIBLE);
            return;
        }
        if (group_logo.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Logo needed")
                    .setMessage("You need to choose a log first")
                    .setPositiveButton("Choose image", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            selectImage();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return;
        }
        dbRef.child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (npsnapshot.child("name").getValue().toString().equals(query) ||
                                npsnapshot.child("name").getValue().toString().toUpperCase().equals(query.toUpperCase()) ||
                                npsnapshot.child("name").getValue().toString().toLowerCase().equals(query.toLowerCase())) {
                            groupText.setError(
                                    Html.fromHtml(("A group with this name <strong>(" + query + ")</strong> already exists"))
                            );
                            nextButton.setVisibility(View.VISIBLE);
                            return;
                        }
                    }

                    groupText.setText("");
                    addGroup(query);
                    return;
                }

                groupText.setText("");
                addGroup(query);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addGroup(final String query) {
        DatabaseReference groupRef = dbRef.child("Groups").push();
        final String group_id = groupRef.getKey();
        long unixTime = System.currentTimeMillis() / 1000L;

        HashMap<String, Object> groupHash = new HashMap<>();

        groupHash.put("chair", UID);
        groupHash.put("created_on", unixTime);
        groupHash.put("group_id", group_id);
        groupHash.put("name", query);
        groupHash.put("logo", group_logo);
        groupHash.put("country", c_name);
        groupHash.put("code", c_code);
        groupHash.put("symbol", c_symbol);
        groupHash.put("description", groupDescription.getText().toString());
        groupHash.put("goal", Long.parseLong(groupGoal.getText().toString()));
        groupHash.put("owner", UID);
        groupHash.put("secretary", "");
        groupHash.put("treasurer", "");

        groupRef.updateChildren(groupHash).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                addGroupMember(group_id, UID, query);
            }
        });
    }

    private void addGroupMember(final String group_id, final String user, final String group_name) {

        long unixTime = System.currentTimeMillis() / 1000L;

        HashMap<String, Object> groupHash = new HashMap<>();

        groupHash.put("member_id", UID);
        groupHash.put("joined_on", unixTime);
        groupHash.put("group_id", group_id);
        groupHash.put("status", 1);

        dbRef.child("GroupMembers").child(group_id).child(user).updateChildren(groupHash).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                addToMyGroups(group_id, user, group_name);
            }
        });
    }

    private void addToMyGroups(final String group_id, String user, String group_name) {
        long unixTime = System.currentTimeMillis() / 1000L;

        HashMap<String, Object> groupHash = new HashMap<>();

        groupHash.put("chair", UID);
        groupHash.put("created_on", unixTime);
        groupHash.put("group_id", group_id);
        groupHash.put("name", group_name);
        groupHash.put("status", 1);

        dbRef.child("MyGroups").child(user).child(group_id).updateChildren(groupHash).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(GroupsCreateActivity.this, "Group Added", Toast.LENGTH_SHORT).show();
                // showGroupsRecyclerExistingUser();
                Intent groupIntent = new Intent(GroupsCreateActivity.this, GroupActivity.class);
                Bundle groupBundle = new Bundle();
                groupBundle.putString("group_id", group_id);
                groupIntent.putExtras(groupBundle);
                startActivity(groupIntent);

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
                                    Uri downloadUrl = uri;
                                    final String image_url = downloadUrl.toString();
                                    group_logo = image_url;
                                    Picasso.get().load(image_url).into(groupLogo);
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(GroupsCreateActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void loadCurrency() {
        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String symbol = snapshot.child("currency_symbol").getValue().toString();
                String code = snapshot.child("currency_code").getValue().toString();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    groupCurrency.setText(Html.fromHtml(symbol + " <small>(" + code + ")</small>", Html.FROM_HTML_MODE_COMPACT));
                } else {
                    groupCurrency.setText(Html.fromHtml(symbol + " <small>(" + code + ")</small>"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}