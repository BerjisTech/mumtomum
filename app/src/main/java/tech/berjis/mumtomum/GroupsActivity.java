package tech.berjis.mumtomum;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupsActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    RecyclerView groupsRecycler;
    GroupsAdapter groupsAdapter;
    List<GroupsList> listData;
    ImageView invites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        init_variables();
        staticInits();
        loadGroups();
    }

    private void loadGroups() {
        listData = new ArrayList<>();
        listData.clear();
        groupsRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        dbRef.child("MyGroups").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot npsnapshot : snapshot.getChildren()) {
                        if (Objects.requireNonNull(npsnapshot.child("status").getValue()).toString().equals("1")) {
                            GroupsList l = npsnapshot.getValue(GroupsList.class);
                            listData.add(l);
                        }
                    }
                    groupsAdapter = new GroupsAdapter(GroupsActivity.this, listData);
                    groupsRecycler.setAdapter(groupsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void init_variables() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        groupsRecycler = findViewById(R.id.groupsRecycler);
        invites = findViewById(R.id.invites);
    }

    private void staticInits() {
        invites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GroupsActivity.this, InvitesActivity.class));
            }
        });
    }
}
