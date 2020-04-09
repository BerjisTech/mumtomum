package tech.berjis.mumtomum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Contacts;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WalletActivity extends AppCompatActivity {

    ImageView profile, contributions, products, text;

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String UID;

    View bgView1, bgView2, bgView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        UID = mAuth.getCurrentUser().getUid();
        checkPaymentDetails();

        profile = findViewById(R.id.profile);
        contributions = findViewById(R.id.contributions);
        products = findViewById(R.id.products);
        text = findViewById(R.id.text);
        bgView1 = findViewById(R.id.bgView1);
        bgView2 = findViewById(R.id.bgView2);
        bgView3 = findViewById(R.id.bgView3);


        bgView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WalletActivity.this, MumWallet.class));
            }
        });
        bgView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WalletActivity.this, BabyActivity.class));
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WalletActivity.this, ProfileActivity.class));
            }
        });
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WalletActivity.this, Messaging.class));
            }
        });
        products.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userIntent = new Intent(WalletActivity.this, ProductsActivity.class);
                Bundle userBundle = new Bundle();
                userBundle.putString("user_id", UID);
                userIntent.putExtras(userBundle);
                startActivity(userIntent);
            }
        });
        contributions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WalletActivity.this, CommunityActivity.class));
            }
        });

    }

    void checkPaymentDetails() {
        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child("email").exists() ||
                        !dataSnapshot.child("first_name").exists() ||
                        !dataSnapshot.child("last_name").exists()) {
                    startActivity(new Intent(WalletActivity.this, UserWalletDetails.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
