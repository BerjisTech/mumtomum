package tech.berjis.mumtomum;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.net.InternetDomainName;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter <CommentsAdapter.ViewHolder> {

    List<Comments> listData;
    private static DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    public CommentsAdapter (List<Comments> listData){
        this.listData = listData;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comments ld = listData.get(position);
        loadUser(ld.getSender(), holder);
        if(ld.getType().equals("text")){
            holder.mainImageCard.setVisibility(View.GONE);
            holder.post.setText(ld.getText());
        }
        if(ld.getType().equals("image")){
            holder.post.setVisibility(View.GONE);
            Picasso.get().load(ld.getText()).into(holder.mainImage);
        }
        Date df = new java.util.Date((ld.getDate() * 1000));
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        String year = new SimpleDateFormat("yyyy").format(df);
        String vv = "";

        if (year.equals(String.valueOf(thisYear))) {
            vv = new SimpleDateFormat("MMM dd").format(df);
        } else {
            vv = new SimpleDateFormat("MMM dd, yyyy").format(df);
        }
        holder.postDate.setText(vv);
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mainImage;
        CircleImageView userImage;
        EmojiTextView userName, postDate, post;
        ConstraintLayout rootView;
        View mView;
        CardView mainImageCard;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mainImage = itemView.findViewById(R.id.mainImage);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
            postDate = itemView.findViewById(R.id.postDate);
            post = itemView.findViewById(R.id.post);
            rootView = itemView.findViewById(R.id.rootView);
            mainImageCard = itemView.findViewById(R.id.mainImageCard);
            mView = itemView;
        }
    }

    private static void loadUser(String user, final ViewHolder holder) {
        dbRef.child("Users").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                holder.userName.setText(username);
                if (dataSnapshot.child("image").exists()) {
                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(holder.userImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
