package tech.berjis.mumtomum;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class WelcomeActivity extends AppCompatActivity {

    ImageView profile, contributions, products, text;

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String UID;

    View bgView1, bgView2, bgView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        UID = mAuth.getCurrentUser().getUid();

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
                startActivity(new Intent(WelcomeActivity.this, MumWallet.class));
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, ProfileActivity.class));
            }
        });
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, Messaging.class));
            }
        });
        products.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userIntent = new Intent(WelcomeActivity.this, ProductsActivity.class);
                Bundle userBundle = new Bundle();
                userBundle.putString("user_id", UID);
                userIntent.putExtras(userBundle);
                startActivity(userIntent);
            }
        });
        contributions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, CommunityActivity.class));
            }
        });

    }

}
