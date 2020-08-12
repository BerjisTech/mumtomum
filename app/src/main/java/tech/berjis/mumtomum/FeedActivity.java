package tech.berjis.mumtomum;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FeedActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String UID, symbol = "";

    private ViewPager groupsPager;
    GroupsPagerAdapter groupsPagerAdapter;
    WormDotsIndicator dots_indicator;
    List<GroupsList> listData;
    View groupsTotal, personalTotal;
    ImageView profile;
    TextView createGroupsText, allGroupsText, latestGroupsText, personalTotalAmount, groupCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        UID = mAuth.getCurrentUser().getUid();
        dbRef.keepSynced(true);

        init_vars();

        newUserState();
    }

    private void init_vars() {
        groupsTotal = findViewById(R.id.groupsTotal);
        groupCount = findViewById(R.id.groupCount);
        latestGroupsText = findViewById(R.id.latestGroupsText);
        profile = findViewById(R.id.profile);
        personalTotal = findViewById(R.id.personalTotal);
        createGroupsText = findViewById(R.id.createGroupsText);
        allGroupsText = findViewById(R.id.allGroupsText);
        groupsPager = findViewById(R.id.groupsPager);
        dots_indicator = findViewById(R.id.dots_indicator);
        personalTotalAmount = findViewById(R.id.personalTotalAmount);
    }

    private void newUserState() {
        dbRef.child("Users").child(UID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child("first_name").exists() ||
                        !dataSnapshot.child("last_name").exists() ||
                        !dataSnapshot.child("name").exists() ||
                        !dataSnapshot.child("email").exists()) {
                    startActivity(new Intent(FeedActivity.this, ProfileActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                } else {
                    loadUserData();
                    loadGroups();
                    staticOnclicks();
                    checkGroup();
                    personalTotalBalance();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkGroup() {
        dbRef.child("MyGroups").child(UID).orderByChild("status").equalTo(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    dots_indicator.setVisibility(View.GONE);
                    groupsPager.setVisibility(View.GONE);
                    createGroupsText.setText("Create A Group\nor\nJoin one");
                    latestGroupsText.setVisibility(View.GONE);
                    allGroupsText.setVisibility(View.GONE);
                } else {
                    groupCount.setText(dataSnapshot.getChildrenCount() + " Groups");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadGroups() {
        listData = new ArrayList<>();
        listData.clear();
        dbRef.child("MyGroups").child(UID).limitToLast(4).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot npsnapshot : snapshot.getChildren()) {
                        if (Objects.requireNonNull(npsnapshot.child("status").getValue()).toString().equals("1")) {
                            GroupsList l = npsnapshot.getValue(GroupsList.class);
                            listData.add(l);
                        }
                    }
                    // setup viewpager
                    Collections.reverse(listData);
                    groupsPagerAdapter = new GroupsPagerAdapter(FeedActivity.this, listData);
                    groupsPager.setAdapter(groupsPagerAdapter);
                    dots_indicator.setViewPager(groupsPager);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserData() {
        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                symbol = Objects.requireNonNull(snapshot.child("currency_symbol").getValue()).toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void staticOnclicks() {
        groupsTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FeedActivity.this, GroupsActivity.class));
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FeedActivity.this, ProfileActivity.class));
            }
        });
        personalTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FeedActivity.this, PersonalActivity.class));
            }
        });
        allGroupsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FeedActivity.this, GroupsActivity.class));
            }
        });
        createGroupsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FeedActivity.this, GroupsCreateActivity.class));
            }
        });
    }

    private void personalTotalBalance() {
        dbRef.child("PersonalWallet").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    long total = 0;
                    Long value;

                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (!npsnapshot.child("status").exists() && npsnapshot.child("text_ref").exists()) {
                            String text_ref = Objects.requireNonNull(npsnapshot.child("text_ref").getValue()).toString();
                            long end_time = System.currentTimeMillis() / 1000L;
                            dbRef.child("PersonalWallet").child(UID).child(text_ref).child("end_time").setValue(end_time);
                            dbRef.child("PersonalWallet").child(UID).child(text_ref).child("status").setValue("cancelled");
                        }
                        if (npsnapshot.child("status").exists() && Objects.equals(npsnapshot.child("status").getValue(), "success")) {
                            value = (Long) npsnapshot.child("amount").getValue();

                            if (Objects.equals(npsnapshot.child("type").getValue(), "deposit")) {
                                total = total + value;
                            } else {
                                total = total - value;
                            }
                        }
                    }
                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setMinimumFractionDigits(0);
                    nf.setMaximumFractionDigits(0);
                    String output = nf.format(total);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        personalTotalAmount.setText(Html.fromHtml("<small>" + symbol + "</small> " + output, Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        personalTotalAmount.setText(Html.fromHtml("<small>" + symbol + "</small> " + output));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
