package tech.berjis.mumtomum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Contacts;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Objects;

public class WalletActivity extends AppCompatActivity {

    ImageView profile, contributions, products, text;

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String UID;

    View bgView1, bgView2, bgView3;
    TextView mumWalletBalance, babyWalletBalance;

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
        mumWalletBalance = findViewById(R.id.mumWalletBalance);
        babyWalletBalance = findViewById(R.id.babyWalletBalance);


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
        WalletTotalBalance();

    }

    private void checkPaymentDetails() {
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

    private void WalletTotalBalance() {
        dbRef.child("BabyWallet").child(UID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    long total = 0;
                    Long value;

                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (!npsnapshot.child("status").exists() && npsnapshot.child("text_ref").exists()) {
                            String text_ref = Objects.requireNonNull(npsnapshot.child("text_ref").getValue()).toString();
                            long end_time = System.currentTimeMillis() / 1000L;
                            dbRef.child("BabyWallet").child(UID).child(text_ref).child("end_time").setValue(end_time);
                            dbRef.child("BabyWallet").child(UID).child(text_ref).child("status").setValue("cancelled");
                        }
                        if (npsnapshot.child("status").exists() && Objects.equals(npsnapshot.child("status").getValue(), "success")) {
                            value = (Long) npsnapshot.child("amount").getValue();

                            if(Objects.equals(npsnapshot.child("type").getValue(), "deposit")){
                                total = total + value;
                            }else{
                                total = total - value;
                            }
                        }
                    }
                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setMinimumFractionDigits(0);
                    nf.setMaximumFractionDigits(0);
                    String output = "KES " + nf.format(total);
                    babyWalletBalance.setText(output);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        dbRef.child("MumWallet").child(UID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    long total = 0;
                    Long value;

                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                            if (!npsnapshot.child("status").exists() && npsnapshot.child("text_ref").exists()) {
                                String text_ref = Objects.requireNonNull(npsnapshot.child("text_ref").getValue()).toString();
                                long end_time = System.currentTimeMillis() / 1000L;
                                dbRef.child("MumWallet").child(UID).child(text_ref).child("end_time").setValue(end_time);
                                dbRef.child("MumWallet").child(UID).child(text_ref).child("status").setValue("cancelled");
                            }
                            if (npsnapshot.child("status").exists() && Objects.equals(npsnapshot.child("status").getValue(), "success")) {
                                value = (Long) npsnapshot.child("amount").getValue();

                                if(Objects.equals(npsnapshot.child("type").getValue(), "deposit")){
                                    total = total + value;
                                }else{
                                    total = total - value;
                                }
                            }
                    }

                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setMinimumFractionDigits(0);
                    nf.setMaximumFractionDigits(0);
                    String output = nf.format(total);
                    mumWalletBalance.setText("KES " + output);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
