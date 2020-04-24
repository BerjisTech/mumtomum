package tech.berjis.mumtomum;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

    private List<GroupsList> listData;
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    GroupsAdapter(List<GroupsList> listData) {
        this.listData = listData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final GroupsList ld = listData.get(position);


        Date df = new java.util.Date((ld.getCreated_on() * 1000));
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        String year = new SimpleDateFormat("yyyy").format(df);
        String vv = "";

        if (year.equals(String.valueOf(thisYear))) {
            vv = new SimpleDateFormat("MMM dd").format(df);
        } else {
            vv = new SimpleDateFormat("MMM dd, yyyy").format(df);
        }

        countMembers(holder, ld.getGroup_id());
        holder.group_name.setText(ld.getName());
        holder.created_on.setText("created on " + vv);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupIntent = new Intent(holder.mView.getContext(), GroupActivity.class);
                Bundle groupBundle = new Bundle();
                groupBundle.putString("group_id", ld.getGroup_id());
                groupIntent.putExtras(groupBundle);
                holder.mView.getContext().startActivity(groupIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView group_name, created_on, members_count;
        View mView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            group_name = itemView.findViewById(R.id.group_name);
            created_on = itemView.findViewById(R.id.created_on);
            members_count = itemView.findViewById(R.id.members_count);
            mView = itemView;
        }
    }

    private void countMembers(final ViewHolder holder, String group_id) {
        dbRef.child("GroupMembers").child(group_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long members = dataSnapshot.getChildrenCount();
                holder.members_count.setText(HtmlCompat.fromHtml("<strong><font size=\"15px\" color=\"#18a3fe\">" + members + "</font></strong>" +
                        "<br />" +
                        "members", HtmlCompat.FROM_HTML_MODE_LEGACY));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
