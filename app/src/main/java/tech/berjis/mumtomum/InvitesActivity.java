package tech.berjis.mumtomum;

import android.os.Bundle;
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

public class InvitesActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    RecyclerView groupsRecycler;
    InvitesAdapter invitesAdapter;
    List<Invites> listData;
    ImageView invites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invites);

        init_variables();
        loadGroups();
    }

    private void loadGroups() {
        listData = new ArrayList<>();
        listData.clear();
        groupsRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        dbRef.child("Invites").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot npsnapshot : snapshot.getChildren()) {
                        if (npsnapshot.child("status").getValue().toString().equals("pending")) {
                            Invites l = npsnapshot.getValue(Invites.class);
                            listData.add(l);
                        }

                    }
                    invitesAdapter = new InvitesAdapter(InvitesActivity.this, listData);
                    groupsRecycler.setAdapter(invitesAdapter);
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
}
