package tech.berjis.mumtomum;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Messages extends RecyclerView.Adapter<Messages.ViewHolder> {
    public String uid;
    private List<String> listData;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    public Messages(List<String> listData) {
        this.listData = listData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages, parent, false);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        uid = mAuth.getCurrentUser().getUid();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final String ld = listData.get(position);

        holder.tick.setVisibility(View.GONE);

        dbRef.child("Users").child(ld).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("image").exists()) {
                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(holder.userImage);
                }
                holder.userName.setText(dataSnapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dbRef.child("Chats").child(ld).child(uid).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                    if (npsnapshot.hasChildren()) {
                        holder.lastTextTime.setText(npsnapshot.child("time").getValue().toString());

                        if (npsnapshot.child("read").getValue().toString().equals("false")) {
                            holder.lastText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                        }

                        if (npsnapshot.child("sender").getValue().toString().equals(uid)) {
                            holder.tick.setVisibility(View.VISIBLE);
                            if (npsnapshot.child("read").getValue().toString().equals("false")) {
                                holder.tick.setColorFilter(ContextCompat.getColor(holder.mView.getContext(), R.color.greyTick), PorterDuff.Mode.SRC_ATOP);
                            }
                            if (npsnapshot.child("read").getValue().toString().equals("true")) {
                                holder.tick.setColorFilter(ContextCompat.getColor(holder.mView.getContext(), R.color.blueTick), PorterDuff.Mode.SRC_ATOP);
                            }
                        }

                        if (npsnapshot.child("type").getValue().toString().equals("text")) {
                            if (!TextUtils.isEmpty(npsnapshot.child("text").getValue().toString())) {
                                if (npsnapshot.child("text").getValue().toString().length() > 35) {
                                    holder.lastText.setText(npsnapshot.child("text").getValue().toString().substring(0, 34) + "...");
                                } else {
                                    holder.lastText.setText(npsnapshot.child("text").getValue().toString());
                                }
                            }
                        }

                        if (npsnapshot.child("type").getValue().toString().equals("order")) {
                            holder.lastText.setText("Order \uD83D\uDED2 \uD83D\uDCB5");
                        }
                        if (npsnapshot.child("type").getValue().toString().equals("image")) {
                            holder.lastText.setText("Image \uD83D\uDDBC");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.mView.getContext(), ChatActivity.class);

                Bundle extras = new Bundle();

                extras.putString("chatting_with", ld);

                intent.putExtras(extras);

                holder.mView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView userImage;
        EmojiTextView lastText, lastTextTime, userName;
        ImageView tick;


        private View mView;

        ViewHolder(View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
            lastText = itemView.findViewById(R.id.lastText);
            lastTextTime = itemView.findViewById(R.id.lastTextTime);
            tick = itemView.findViewById(R.id.tick);

            mView = itemView;

        }
    }
}