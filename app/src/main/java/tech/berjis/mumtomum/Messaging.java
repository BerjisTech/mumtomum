package tech.berjis.mumtomum;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.List;

public class Messaging extends AppCompatActivity {

    public FirebaseAuth mAuth;
    public DatabaseReference dbRef;
    public String uid;
    public ImageView back;
    private List<String> listData;
    private RecyclerView rv;
    private Messages adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
		
        uid = mAuth.getCurrentUser().getUid();

        back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Messaging.super.finish();
            }
        });

        rv = findViewById(R.id.messages);


    }

    @Override
    protected void onStart() {
        super.onStart();
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        listData = new ArrayList<>();
        dbRef.child("Chats").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        String l = npsnapshot.getKey();
                        listData.add(l);
                    }
                    Collections.reverse(listData);
                    adapter = new Messages(listData);
                    rv.setAdapter(adapter);
                    //rv.scrollToPosition(listData.size() - 1);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Messaging.this, "Kuna shida mahali", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
