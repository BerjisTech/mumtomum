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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.timqi.sectorprogressview.ColorfulRingProgressView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

    private DatabaseReference dbRef;
    private List<GroupsList> listData;
    private Context mContext;

    GroupsAdapter(Context mContext, List<GroupsList> listData) {
        this.listData = listData;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        dbRef = FirebaseDatabase.getInstance().getReference();

        View view = LayoutInflater.from(mContext).inflate(R.layout.groups, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final GroupsList ld = listData.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupIntent = new Intent(mContext, GroupActivity.class);
                Bundle groupBundle = new Bundle();
                groupBundle.putString("group_id", ld.getGroup_id());
                groupIntent.putExtras(groupBundle);
                mContext.startActivity(groupIntent);
            }
        });
        loadGroupData(ld, holder);
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ColorfulRingProgressView colorfulRingProgressView;
        CircleImageView groupLogo;
        TextView groupName, groupProgress;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorfulRingProgressView = itemView.findViewById(R.id.colorfulRingProgressView);
            groupLogo = itemView.findViewById(R.id.groupLogo);
            groupName = itemView.findViewById(R.id.groupName);
            groupProgress = itemView.findViewById(R.id.groupProgress);
        }
    }


    private void loadGroupData(final GroupsList ld, final ViewHolder holder) {

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("Groups").child(ld.getGroup_id()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String logo = snapshot.child("logo").getValue().toString();
                String name = snapshot.child("name").getValue().toString();
                String currency = snapshot.child("symbol").getValue().toString();
                long goal = Long.parseLong(snapshot.child("goal").getValue().toString());
                DecimalFormat formatter = new DecimalFormat("#,###,###");
                Glide.with(mContext).load(logo).placeholder(R.drawable.image_placeholder).into(holder.groupLogo);
                holder.groupName.setText(name);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    holder.groupProgress.setText(Html.fromHtml("<small>" + currency + "</small> 0 / <small>" + currency + "</small> " + formatter.format(goal), Html.FROM_HTML_MODE_COMPACT));
                } else {
                    holder.groupProgress.setText(Html.fromHtml("<small>" + currency + "</small> 0 / <small>" + currency + "</small> " + formatter.format(goal)));
                }

                holder.colorfulRingProgressView.setPercent(0);

                totalBalance(ld.getGroup_id(), holder, currency, goal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void totalBalance(final String group, final ViewHolder holder, final String currency, final long goal) {
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
                        holder.groupProgress.setText(Html.fromHtml("<small>" + currency + "</small> " + formatter.format(total) + "/ <small>" + currency + "</small> " + formatter.format(goal), Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        holder.groupProgress.setText(Html.fromHtml("<small>" + currency + "</small> " + formatter.format(total) + "/ <small>" + currency + "</small> " + formatter.format(goal)));
                    }

                    long progress = ((total * 100) / goal);
                    holder.colorfulRingProgressView.setPercent(progress);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
