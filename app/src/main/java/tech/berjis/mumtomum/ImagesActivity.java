package tech.berjis.mumtomum;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ImagesActivity extends AppCompatActivity {

    private List<GossipImages> imageList;
    private GossipImagesPagerAdapter pagerAdapter;
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    ViewPager images;
    ImageView close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        dbRef.keepSynced(true);

        images = findViewById(R.id.images);
        close = findViewById(R.id.close);

        imageList = new ArrayList<>();
        imageLoader();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagesActivity.super.finish();
            }
        });
    }

    private void imageLoader() {
        Intent gossipIntent = getIntent();
        Bundle gossipBundle = gossipIntent.getExtras();
        String gossipID = gossipBundle.getString("gossipID");
        dbRef.child("GossipImages").child(gossipID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        GossipImages l = npsnapshot.getValue(GossipImages.class);
                        imageList.add(l);
                    }
                    pagerAdapter = new GossipImagesPagerAdapter(imageList, "gallery");
                    images.setAdapter(pagerAdapter);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ImagesActivity.this, "Kuna shida mahali", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
