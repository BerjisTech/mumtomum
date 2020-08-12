package tech.berjis.mumtomum;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.timqi.sectorprogressview.ColorfulRingProgressView;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

public class GroupsPagerAdapter extends PagerAdapter {

    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private Context mContext;
    private List<GroupsList> listData;

    GroupsPagerAdapter(Context mContext, List<GroupsList> listData) {
        this.mContext = mContext;
        this.listData = listData;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        View mView = LayoutInflater.from(mContext).inflate(R.layout.group_card, container, false);
        final GroupsList ld = listData.get(position);

        /*ImageView imgSlide = mView.findViewById(R.id.intro_img);
        TextView title = mView.findViewById(R.id.intro_title);
        TextView description = mView.findViewById(R.id.intro_description);

        title.setText(listData.get(position).getTitle());
        description.setText(listData.get(position).getDescription());
        imgSlide.setImageResource(listData.get(position).getScreenImg());*/

        TextView groupName = mView.findViewById(R.id.groupName);
        groupName.setText(ld.getName());

        loadGroupData(ld, mView);


        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupIntent = new Intent(mContext, GroupActivity.class);
                Bundle groupBundle = new Bundle();
                groupBundle.putString("group_id", ld.getGroup_id());
                groupIntent.putExtras(groupBundle);
                mContext.startActivity(groupIntent);
            }
        });

        container.addView(mView);

        return mView;


    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView((View) object);

    }

    private void loadGroupData(final GroupsList ld, final View mView) {

        final TextView groupPurpose = mView.findViewById(R.id.groupPurpose);
        final TextView groupGoal = mView.findViewById(R.id.groupGoal);

        dbRef.child("Groups").child(ld.getGroup_id()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currency = snapshot.child("symbol").getValue().toString();
                long goal = Long.parseLong(snapshot.child("goal").getValue().toString());
                String purpose = snapshot.child("description").getValue().toString();
                DecimalFormat formatter = new DecimalFormat("#,###,###");
                groupPurpose.setText(purpose);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    groupGoal.setText(Html.fromHtml("<small>" + currency + "</small> " + formatter.format(goal), Html.FROM_HTML_MODE_COMPACT));
                } else {
                    groupGoal.setText(Html.fromHtml("<small>" + currency + "</small> " + formatter.format(goal)));
                }

                totalBalance(ld.getGroup_id(), mView, currency, goal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void totalBalance(final String group, View mView, final String currency, final long goal) {
        final TextView groupTotal = mView.findViewById(R.id.groupTotal);
        final TextView progressValue = mView.findViewById(R.id.progressValue);
        final ColorfulRingProgressView progressRing = mView.findViewById(R.id.progressRing);

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

                    DecimalFormat formatter = new DecimalFormat("#,###,###");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        groupTotal.setText(Html.fromHtml("<small>" + currency + "</small> " + formatter.format(total), Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        groupTotal.setText(Html.fromHtml("<small>" + currency + "</small> " + formatter.format(total)));
                    }

                    long progress = ((total * 100) / goal);
                    progressValue.setText(progress + "%");
                    progressRing.setPercent(progress);
                }else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        groupTotal.setText(Html.fromHtml("<small>" + currency + "</small> 0", Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        groupTotal.setText(Html.fromHtml("<small>" + currency + "</small> 0"));
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

}