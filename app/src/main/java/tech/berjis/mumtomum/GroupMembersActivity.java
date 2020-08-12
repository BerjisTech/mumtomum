package tech.berjis.mumtomum;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GroupMembersActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

    FirebaseAuth mAuth;
    DatabaseReference dbRef;

    GroupMembersAdapter groupMembersAdapter;
    List<GroupMembers> listData;

    String UID, group_id;

    TextView group_name, inviteButton, memberPhoneText;
    EditText memberPhone;
    CountryCodePicker memberPhoneCode;
    RecyclerView groupMembersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);

        initLayout();
        loadGroup();
        onClicks();
        loadGroupMembers();
    }


    private void initLayout() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        UID = mAuth.getCurrentUser().getUid();

        group_name = findViewById(R.id.group_name);
        inviteButton = findViewById(R.id.inviteButton);
        memberPhone = findViewById(R.id.memberPhone);
        memberPhoneCode = findViewById(R.id.memberPhoneCode);
        memberPhoneText = findViewById(R.id.memberPhoneText);
        groupMembersList = findViewById(R.id.groupMembersList);


        listData = new ArrayList<>();

        Intent g_i = getIntent();
        Bundle g_b = g_i.getExtras();
        group_id = g_b.getString("group_id");
    }

    private void onClicks() {
        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUserExists();
            }
        });
    }

    private void loadGroup() {
        dbRef.child("Groups").child(group_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String g_n = snapshot.child("name").getValue().toString();
                String owner = snapshot.child("owner").getValue().toString();
                group_name.setText(g_n);

                if (UID.equals(owner)) {
                    inviteButton.setVisibility(View.VISIBLE);
                    memberPhone.setVisibility(View.VISIBLE);
                    memberPhoneCode.setVisibility(View.VISIBLE);
                    memberPhoneText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserExists() {
        final String code = memberPhoneCode.getSelectedCountryCode();
        final String phone = memberPhone.getText().toString();
        final String g_n = group_name.getText().toString();

        Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("regs_phone").equalTo("+" + code + phone);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        String l = npsnapshot.getKey();
                        checkIfUserIsMember(l);
                    }
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(GroupMembersActivity.this);
                    final EditText edittext = new EditText(GroupMembersActivity.this);
                    edittext.setHint("Type invitation message here");
                    alert.setMessage("The user with this number is not on the Group goals. Invite them over sms to join you now.");
                    alert.setTitle("User not found");

                    alert.setView(edittext);

                    alert.setPositiveButton("Invite", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String message = edittext.getText().toString();
                            sendSMS("+" + code + phone, message);
                        }
                    });

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // what ever you want to do with No option.
                        }
                    });

                    alert.show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkIfUserIsMember(final String l) {
        dbRef.child("GroupMembers").child(group_id).child(l).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(GroupMembersActivity.this, "This user is already a member", Toast.LENGTH_SHORT).show();
                } else {
                    checkIfInviteSent(l);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkIfInviteSent(final String l) {
        dbRef.child("Invites").child(l).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(GroupMembersActivity.this, "You already sent an invite to this user", Toast.LENGTH_SHORT).show();
                } else {
                    HashMap<Object, Object> i_m = new HashMap<>();

                    DatabaseReference i_r = dbRef.child("Invites").child(l).push();
                    String i_c = i_r.getKey();

                    i_m.put("group_id", group_id);
                    i_m.put("inviter", UID);
                    i_m.put("invite_code", i_c);
                    i_m.put("status", "pending");

                    i_r.setValue(i_m).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(GroupMembersActivity.this, "Invite sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupMembers() {
        listData.clear();
        groupMembersList.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        dbRef.child("GroupMembers").child(group_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot npsnapshot : snapshot.getChildren()) {
                    if (Objects.requireNonNull(npsnapshot.child("status").getValue()).toString().equals("1")) {
                        listData.add(npsnapshot.getValue(GroupMembers.class));
                    }
                }

                groupMembersAdapter = new GroupMembersAdapter(GroupMembersActivity.this, listData);
                groupMembersList.setAdapter(groupMembersAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void sendSMS(String phone, String message) {
        Uri uri = Uri.parse("smsto:" + phone);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra("sms_body", message);
        startActivity(Intent.createChooser(intent, "Send via"));
    }
}
