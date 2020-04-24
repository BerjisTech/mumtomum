package tech.berjis.mumtomum;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GroupsActivity extends AppCompatActivity {

    TextView bigTitle, smallTitle, chamaButton, nextButton, noGroupMessage, joinGroup, createGroup, orText, newGroup;
    EditText chamaText;
    ImageView chamaVector, backButton;
    RecyclerView groupsRecycler;
    SearchView searchGroups;
    View half;
    String tab = "", UID, phone;

    List<GroupsList> listData;
    GroupsAdapter groupsAdapter;

    DatabaseReference dbRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        UID = mAuth.getCurrentUser().getUid();
        phone = mAuth.getCurrentUser().getPhoneNumber();
        dbRef.keepSynced(true);

        bigTitle = findViewById(R.id.bigTitle);
        smallTitle = findViewById(R.id.smallTitle);
        chamaButton = findViewById(R.id.chamaButton);
        nextButton = findViewById(R.id.nextButton);
        noGroupMessage = findViewById(R.id.noGroupMessage);
        joinGroup = findViewById(R.id.joinGroup);
        createGroup = findViewById(R.id.createGroup);
        orText = findViewById(R.id.orText);
        chamaText = findViewById(R.id.chamaText);
        chamaVector = findViewById(R.id.chamaVector);
        backButton = findViewById(R.id.backButton);
        half = findViewById(R.id.half);
        groupsRecycler = findViewById(R.id.groupsRecycler);
        searchGroups = findViewById(R.id.searchGroup);
        newGroup = findViewById(R.id.newGroup);

        chamaButton.setOnClickListener(new View.OnClickListener() {
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
                    showGroupsRecyclerExistingUser();
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
                showGroupsRecycler();
            }
        });
        newGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateGroupExistingUser();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkGroupName(chamaText.getText().toString());
                nextButton.setVisibility(View.GONE);
            }
        });
        searchGroups.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadGroupSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadGroupSearch(newText);
                return false;
            }
        });

        checkUser(UID);
        listData = new ArrayList<>();
        groupsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void checkUser(String user) {
        dbRef.child("MyGroups").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    showGroupsRecyclerExistingUser();
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
        chamaButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
        noGroupMessage.setVisibility(View.GONE);
        joinGroup.setVisibility(View.GONE);
        createGroup.setVisibility(View.GONE);
        orText.setVisibility(View.GONE);
        chamaText.setVisibility(View.GONE);
        chamaVector.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.GONE);
        half.setVisibility(View.GONE);
        groupsRecycler.setVisibility(View.GONE);
        searchGroups.setVisibility(View.GONE);
        newGroup.setVisibility(View.GONE);
    }

    private void showCreateOrJoin() {
        tab = "createJoin";
        bigTitle.setVisibility(View.GONE);
        smallTitle.setVisibility(View.GONE);
        chamaButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        noGroupMessage.setVisibility(View.VISIBLE);
        joinGroup.setVisibility(View.VISIBLE);
        createGroup.setVisibility(View.VISIBLE);
        orText.setVisibility(View.VISIBLE);
        chamaText.setVisibility(View.GONE);
        chamaVector.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.GONE);
        half.setVisibility(View.VISIBLE);
        groupsRecycler.setVisibility(View.GONE);
        searchGroups.setVisibility(View.GONE);
        newGroup.setVisibility(View.GONE);
    }

    private void showCreateGroup() {
        tab = "createGroup";
        bigTitle.setVisibility(View.GONE);
        smallTitle.setVisibility(View.GONE);
        chamaButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.VISIBLE);
        noGroupMessage.setVisibility(View.GONE);
        joinGroup.setVisibility(View.GONE);
        createGroup.setVisibility(View.GONE);
        orText.setVisibility(View.GONE);
        chamaText.setVisibility(View.VISIBLE);
        chamaVector.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        half.setVisibility(View.GONE);
        groupsRecycler.setVisibility(View.GONE);
        searchGroups.setVisibility(View.GONE);
        newGroup.setVisibility(View.GONE);
    }

    private void showCreateGroupExistingUser() {
        tab = "createGroupExist";
        bigTitle.setVisibility(View.GONE);
        smallTitle.setVisibility(View.GONE);
        chamaButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.VISIBLE);
        noGroupMessage.setVisibility(View.GONE);
        joinGroup.setVisibility(View.GONE);
        createGroup.setVisibility(View.GONE);
        orText.setVisibility(View.GONE);
        chamaText.setVisibility(View.VISIBLE);
        chamaVector.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        half.setVisibility(View.GONE);
        groupsRecycler.setVisibility(View.GONE);
        searchGroups.setVisibility(View.GONE);
        newGroup.setVisibility(View.GONE);
    }

    private void showGroupsRecycler() {
        tab = "joinGroup";
        bigTitle.setVisibility(View.GONE);
        smallTitle.setVisibility(View.GONE);
        chamaButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        noGroupMessage.setVisibility(View.GONE);
        joinGroup.setVisibility(View.GONE);
        createGroup.setVisibility(View.GONE);
        orText.setVisibility(View.GONE);
        chamaText.setVisibility(View.GONE);
        chamaVector.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);
        half.setVisibility(View.GONE);
        groupsRecycler.setVisibility(View.VISIBLE);
        searchGroups.setVisibility(View.VISIBLE);
        newGroup.setVisibility(View.GONE);
        loadAllGroups();
    }

    private void showGroupsRecyclerExistingUser() {
        tab = "groupsAll";
        bigTitle.setVisibility(View.GONE);
        smallTitle.setVisibility(View.GONE);
        chamaButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        noGroupMessage.setVisibility(View.GONE);
        joinGroup.setVisibility(View.GONE);
        createGroup.setVisibility(View.GONE);
        orText.setVisibility(View.GONE);
        chamaText.setVisibility(View.GONE);
        chamaVector.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);
        half.setVisibility(View.GONE);
        groupsRecycler.setVisibility(View.VISIBLE);
        searchGroups.setVisibility(View.VISIBLE);
        newGroup.setVisibility(View.VISIBLE);
        loadAllGroups();
    }

    @Override
    public void onBackPressed() {

        if (tab.equals("welcome")) {
            GroupsActivity.super.finish();
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
            showGroupsRecyclerExistingUser();
        }
        if (tab.equals("groupsAll")) {
            GroupsActivity.super.finish();
        }
        if (tab.equals("")) {
            GroupsActivity.super.finish();
        }
    }

    private void loadAllGroups() {
        listData.clear();
        dbRef.child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        GroupsList l = npsnapshot.getValue(GroupsList.class);
                        listData.add(l);
                    }
                    Collections.reverse(listData);
                    groupsAdapter = new GroupsAdapter(listData);
                    groupsRecycler.setAdapter(groupsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadGroupSearch(final String query) {
        listData.clear();
        final DatabaseReference nm = FirebaseDatabase.getInstance().getReference("Groups");
        Query firebaseSearchQuery = nm.orderByChild("name").startAt(query.toUpperCase()).endAt(query.toLowerCase() + "\uf8ff");
        firebaseSearchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        GroupsList l = npsnapshot.getValue(GroupsList.class);
                        listData.add(l);
                    }
                    Collections.reverse(listData);
                    groupsAdapter = new GroupsAdapter(listData);
                    groupsRecycler.setAdapter(groupsAdapter);
                    newGroup.setText("CREATE NEW GROUP");
                }else{
                    listData.clear();
                    newGroup.setText("There are no groups named " + query);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkGroupName(final String query) {

        if (query.equals("")) {
            chamaText.setError("You need to add a group name");
            nextButton.setVisibility(View.VISIBLE);
            return;
        }

        listData.clear();
        dbRef.child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (npsnapshot.child("name").getValue().toString().equals(query)) {
                            chamaText.setError(
                                    Html.fromHtml(("A group with this name <strong>(" + query + ")</strong> already exists"))
                            );
                            nextButton.setVisibility(View.VISIBLE);
                        } else {
                            chamaText.setText("");
                            addGroup(query);
                        }
                    }
                } else {
                    chamaText.setText("");
                    addGroup(query);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addGroup(String query) {
        DatabaseReference groupRef = dbRef.child("Groups").push();
        final String group_id = groupRef.getKey();
        long unixTime = System.currentTimeMillis() / 1000L;

        HashMap<String, Object> groupHash = new HashMap<>();

        groupHash.put("chair", UID);
        groupHash.put("created_on", unixTime);
        groupHash.put("group_id", group_id);
        groupHash.put("name", query);
        groupHash.put("owner", "");
        groupHash.put("secretary", "");
        groupHash.put("treasurer", "");

        groupRef.updateChildren(groupHash).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                addGroupMember(group_id, UID);
            }
        });
    }

    private void addGroupMember(final String group_id, final String user){

        long unixTime = System.currentTimeMillis() / 1000L;

        HashMap<String, Object> groupHash = new HashMap<>();

        groupHash.put("member_id", UID);
        groupHash.put("joined_on", unixTime);
        groupHash.put("group_id", group_id);

        dbRef.child("GroupMembers").child(group_id).child(user).updateChildren(groupHash).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                addToMyGroups(group_id, user);
            }
        });
    }

    private void addToMyGroups(final String group_id, String user){
        long unixTime = System.currentTimeMillis() / 1000L;

        HashMap<String, Object> groupHash = new HashMap<>();

        groupHash.put("chair", UID);
        groupHash.put("created_on", unixTime);
        groupHash.put("group_id", group_id);

        showGroupsRecyclerExistingUser();

        dbRef.child("MyGroups").child(user).child(group_id).updateChildren(groupHash).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent groupIntent = new Intent(GroupsActivity.this, GroupActivity.class);
                Bundle groupBundle = new Bundle();
                groupBundle.putString("group_id", group_id);
                groupIntent.putExtras(groupBundle);
                startActivity(groupIntent);
            }
        });
    }
}
