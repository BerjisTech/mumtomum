package tech.berjis.mumtomum;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.timqi.sectorprogressview.ColorfulRingProgressView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;

public class GroupFragment extends Fragment {
    private Context mContext;
    private String UID, group, currency;
    private long goal;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private TextView groupPurpose, groupGoal, groupMembers, groupTotal, myContribution, topup, othersContribution, progressValue, leaveGroup;
    private ImageView membersIcon;
    private ColorfulRingProgressView progressRing;

    public GroupFragment(Context mContext, String group) {
        this.mContext = mContext;
        this.group = group;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        initViews(view);
        loadGroupData();
        totalBalance();
        setMyContribution();
        setOthersContribution();
        onClicks();
        return view;
    }

    private void initViews(View view) {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        UID = mAuth.getCurrentUser().getUid();

        groupPurpose = view.findViewById(R.id.groupPurpose);
        groupGoal = view.findViewById(R.id.groupGoal);
        membersIcon = view.findViewById(R.id.membersIcon);
        groupMembers = view.findViewById(R.id.groupMembers);
        groupTotal = view.findViewById(R.id.groupTotal);
        myContribution = view.findViewById(R.id.myContribution);
        topup = view.findViewById(R.id.topup);
        othersContribution = view.findViewById(R.id.othersContribution);
        progressValue = view.findViewById(R.id.progressValue);
        progressRing = view.findViewById(R.id.progressRing);
        leaveGroup = view.findViewById(R.id.leaveGroup);
    }

    private void loadGroupData() {

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("Groups").child(group).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currency = snapshot.child("symbol").getValue().toString();
                goal = Long.parseLong(snapshot.child("goal").getValue().toString());
                String purpose = snapshot.child("description").getValue().toString();
                DecimalFormat formatter = new DecimalFormat("#,###,###");
                groupPurpose.setText(purpose);
                groupGoal.setText(currency + " " + formatter.format(goal));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    groupTotal.setText(Html.fromHtml("<small>" + currency + "</small> 0 <small>so far</small>", Html.FROM_HTML_MODE_COMPACT));
                } else {
                    groupTotal.setText(Html.fromHtml("<small>" + currency + "</small> 0 <small>so far</small>"));
                }
                progressValue.setText("0%");
                progressRing.setPercent(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void onClicks() {
        membersIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupIntent = new Intent(mContext, GroupMembersActivity.class);
                Bundle groupBundle = new Bundle();
                groupBundle.putString("group_id", group);
                groupIntent.putExtras(groupBundle);
                mContext.startActivity(groupIntent);
            }
        });
        groupMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupIntent = new Intent(mContext, GroupMembersActivity.class);
                Bundle groupBundle = new Bundle();
                groupBundle.putString("group_id", group);
                groupIntent.putExtras(groupBundle);
                mContext.startActivity(groupIntent);
            }
        });
        topup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupIntent = new Intent(mContext, GroupWalletActivity.class);
                Bundle groupBundle = new Bundle();
                groupBundle.putString("group_id", group);
                groupIntent.putExtras(groupBundle);
                mContext.startActivity(groupIntent);
            }
        });
        leaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupIntent = new Intent(mContext, ExitActivity.class);
                Bundle groupBundle = new Bundle();
                groupBundle.putString("group_id", group);
                groupIntent.putExtras(groupBundle);
                mContext.startActivity(groupIntent);
            }
        });
    }

    private void totalBalance() {
        dbRef.child("GroupWallet").child(group).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    long total = 0;
                    Long value;

                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (!npsnapshot.child("status").exists() && npsnapshot.child("text_ref").exists()) {
                            String text_ref = Objects.requireNonNull(npsnapshot.child("text_ref").getValue()).toString();
                            long end_time = System.currentTimeMillis() / 1000L;
                            dbRef.child("GroupWallet").child(group).child(text_ref).child("end_time").setValue(end_time);
                            dbRef.child("GroupWallet").child(group).child(text_ref).child("status").setValue("cancelled");
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
                        groupTotal.setText(Html.fromHtml("<small>" + currency + "</small> " + output + " <small>so far</small>", Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        groupTotal.setText(Html.fromHtml("<small>" + currency + "</small> " + output + " <small>so far</small>"));
                    }

                    long progress = ((total * 100) / goal);
                    progressValue.setText(progress + "%");
                    progressRing.setPercent(progress);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        groupTotal.setText(Html.fromHtml("<small>" + currency + "</small> 0 <small>so far</small>", Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        groupTotal.setText(Html.fromHtml("<small>" + currency + "</small> 0 <small>so far</small>"));
                    }
                    progressValue.setText("0%");
                    progressRing.setPercent(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setMyContribution() {
        dbRef.child("GroupWallet").child(group).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    long total = 0;
                    Long value;

                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (!npsnapshot.child("status").exists() && npsnapshot.child("text_ref").exists()) {
                            String text_ref = Objects.requireNonNull(npsnapshot.child("text_ref").getValue()).toString();
                            long end_time = System.currentTimeMillis() / 1000L;
                            dbRef.child("GroupWallet").child(group).child(text_ref).child("end_time").setValue(end_time);
                            dbRef.child("GroupWallet").child(group).child(text_ref).child("status").setValue("cancelled");
                        }
                        if (npsnapshot.child("status").exists() && Objects.equals(npsnapshot.child("status").getValue(), "success") && Objects.equals(npsnapshot.child("user").getValue(), UID)) {
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
                        myContribution.setText(Html.fromHtml("<small>" + currency + "</small> " + output, Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        myContribution.setText(Html.fromHtml("<small>" + currency + "</small> " + output));
                    }
                }else{

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        myContribution.setText(Html.fromHtml("<small>" + currency + "</small> 0", Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        myContribution.setText(Html.fromHtml("<small>" + currency + "</small> 0"));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setOthersContribution() {
        dbRef.child("GroupWallet").child(group).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    long total = 0;
                    Long value;

                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (!npsnapshot.child("status").exists() && npsnapshot.child("text_ref").exists()) {
                            String text_ref = Objects.requireNonNull(npsnapshot.child("text_ref").getValue()).toString();
                            long end_time = System.currentTimeMillis() / 1000L;
                            dbRef.child("GroupWallet").child(group).child(text_ref).child("end_time").setValue(end_time);
                            dbRef.child("GroupWallet").child(group).child(text_ref).child("status").setValue("cancelled");
                        }
                        if (npsnapshot.child("status").exists() && Objects.equals(npsnapshot.child("status").getValue(), "success") && !Objects.requireNonNull(npsnapshot.child("user").getValue()).toString().equals(UID)) {
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
                        othersContribution.setText(Html.fromHtml("<small>" + currency + "</small> " + output, Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        othersContribution.setText(Html.fromHtml("<small>" + currency + "</small> " + output));
                    }
                }else{

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        othersContribution.setText(Html.fromHtml("<small>" + currency + "</small> 0", Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        othersContribution.setText(Html.fromHtml("<small>" + currency + "</small> 0"));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
