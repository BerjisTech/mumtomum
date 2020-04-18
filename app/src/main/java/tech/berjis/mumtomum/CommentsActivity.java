package tech.berjis.mumtomum;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CommentsActivity extends AppCompatActivity {

    DatabaseReference dbRef;
    FirebaseAuth mAuth;

    String gossipID, UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        UID = mAuth.getCurrentUser().getUid();

        Intent gossipIntent = getIntent();
        Bundle gossipBundle = gossipIntent.getExtras();
        gossipID = gossipBundle.getString("gossipID");

    }

}
