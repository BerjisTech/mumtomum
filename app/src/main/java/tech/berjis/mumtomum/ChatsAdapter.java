package tech.berjis.mumtomum;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    public List<Chats> listData;
    public FirebaseAuth mAuth;
    public DatabaseReference dbRef;
    public String uid;

    public ChatsAdapter(List<Chats> listData) {
        this.listData = listData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats, parent, false);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        uid = mAuth.getCurrentUser().getUid();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Chats ld = listData.get(position);

        holder.senderText.setVisibility(View.GONE);
        holder.senderTime.setVisibility(View.GONE);
        holder.senderView.setVisibility(View.GONE);
        holder.senderOrderView.setVisibility(View.GONE);
        holder.senderOrderTitle.setVisibility(View.GONE);
        holder.senderOrderImage.setVisibility(View.GONE);
        holder.senderOrderText.setVisibility(View.GONE);
        holder.receiverText.setVisibility(View.GONE);
        holder.receiverTime.setVisibility(View.GONE);
        holder.receiverView.setVisibility(View.GONE);
        holder.receiverOrderView.setVisibility(View.GONE);
        holder.receiverOrderTitle.setVisibility(View.GONE);
        holder.receiverOrderImage.setVisibility(View.GONE);
        holder.receiverOrderText.setVisibility(View.GONE);
        holder.senderImage.setVisibility(View.GONE);
        holder.receiverImage.setVisibility(View.GONE);
        holder.tick.setVisibility(View.GONE);

        if(uid.equals(ld.getSender())){
            holder.tick.setVisibility(View.VISIBLE);
            if (ld.getRead().equals("false")) {
                holder.tick.setColorFilter(ContextCompat.getColor(holder.mView.getContext(), R.color.greyTick), PorterDuff.Mode.SRC_ATOP);
            }
            if (ld.getRead().equals("true")) {
                holder.tick.setColorFilter(ContextCompat.getColor(holder.mView.getContext(), R.color.blueTick), PorterDuff.Mode.SRC_ATOP);
            }
        }

        if (ld.getType().equals("text")) {

            if (uid.equals(ld.getSender())) {

                holder.senderText.setVisibility(View.VISIBLE);
                holder.senderView.setVisibility(View.VISIBLE);
                holder.senderTime.setVisibility(View.VISIBLE);

                holder.senderText.setText(ld.getText());
                holder.senderTime.setText(ld.getTime());
            }
            if (uid.equals(ld.getReceiver())) {

                holder.receiverText.setVisibility(View.VISIBLE);
                holder.receiverView.setVisibility(View.VISIBLE);
                holder.receiverTime.setVisibility(View.VISIBLE);

                holder.receiverText.setText(ld.getText());
                holder.receiverTime.setText(ld.getTime());
            }
        }

        if (ld.getType().equals("order")) {

            dbRef.child("Products").child(ld.getText()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (uid.equals(ld.getSender())) {

                            holder.senderTime.setVisibility(View.VISIBLE);
                            holder.senderOrderView.setVisibility(View.VISIBLE);
                            holder.senderOrderTitle.setVisibility(View.VISIBLE);
                            holder.senderOrderImage.setVisibility(View.VISIBLE);
                            holder.senderOrderText.setVisibility(View.VISIBLE);

                            holder.senderTime.setText(ld.getTime());
                            holder.senderOrderTitle.setText(dataSnapshot.child("name").getValue().toString());
                            Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(holder.senderOrderImage);
                            holder.senderOrderText.setText(dataSnapshot.child("description").getValue().toString());

                        }
                        if (uid.equals(ld.getReceiver())) {

                            holder.receiverTime.setVisibility(View.VISIBLE);
                            holder.receiverOrderView.setVisibility(View.VISIBLE);
                            holder.receiverOrderTitle.setVisibility(View.VISIBLE);
                            holder.receiverOrderImage.setVisibility(View.VISIBLE);
                            holder.receiverOrderText.setVisibility(View.VISIBLE);

                            holder.receiverTime.setText(ld.getTime());
                            holder.receiverOrderTitle.setText(dataSnapshot.child("name").getValue().toString());
                            Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(holder.receiverOrderImage);
                            holder.receiverOrderText.setText(dataSnapshot.child("description").getValue().toString());
                        }
                    } else {
                        if (uid.equals(ld.getSender())) {

                            holder.senderText.setVisibility(View.VISIBLE);
                            holder.senderView.setVisibility(View.VISIBLE);
                            holder.senderTime.setVisibility(View.VISIBLE);

                            holder.senderText.setText("Deleted order");
                            holder.senderText.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                            holder.senderTime.setText(ld.getTime());
                        }
                        if (uid.equals(ld.getReceiver())) {

                            holder.receiverText.setVisibility(View.VISIBLE);
                            holder.receiverView.setVisibility(View.VISIBLE);
                            holder.receiverTime.setVisibility(View.VISIBLE);

                            holder.receiverText.setText("Deleted order");
                            holder.receiverText.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                            holder.receiverTime.setText(ld.getTime());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        if (ld.getType().equals("image")) {

            if (uid.equals(ld.getSender())) {
                holder.senderTime.setVisibility(View.VISIBLE);
                holder.senderImage.setVisibility(View.VISIBLE);

                Glide.with(holder.mView.getContext()).load(ld.getText()).into(holder.senderImage);
                holder.senderTime.setText(ld.getTime());
            }

            if (uid.equals(ld.getReceiver())) {
                holder.receiverTime.setVisibility(View.VISIBLE);
                holder.receiverImage.setVisibility(View.VISIBLE);

                Glide.with(holder.mView.getContext()).load(ld.getText()).into(holder.receiverImage);
                holder.receiverTime.setText(ld.getTime());
            }
        }

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        EmojiTextView senderText, receiverText, senderTime, receiverTime, senderOrderTitle, senderOrderText, receiverOrderTitle, receiverOrderText;
        ImageView tick, senderImage, receiverImage, senderOrderImage, receiverOrderImage;
        View senderView, receiverView, senderOrderView, receiverOrderView;
        View mView;

        ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            senderText = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
            senderView = itemView.findViewById(R.id.senderView);
            senderOrderView = itemView.findViewById(R.id.SenderOrderView);
            senderOrderTitle = itemView.findViewById(R.id.SenderOrderTitle);
            senderOrderImage = itemView.findViewById(R.id.SenderOrderImg);
            senderOrderText = itemView.findViewById(R.id.SenderOrderText);
            receiverText = itemView.findViewById(R.id.receiverText);
            receiverTime = itemView.findViewById(R.id.receiverTime);
            receiverView = itemView.findViewById(R.id.receiverView);
            receiverOrderView = itemView.findViewById(R.id.RecieverOrderView);
            receiverOrderTitle = itemView.findViewById(R.id.RecieverOrderTitle);
            receiverOrderImage = itemView.findViewById(R.id.RecieverOrderImg);
            receiverOrderText = itemView.findViewById(R.id.RecieverOrderText);
            senderImage = itemView.findViewById(R.id.senderImage);
            receiverImage = itemView.findViewById(R.id.receiverImage);
            tick = itemView.findViewById(R.id.tick);

        }
    }
}
