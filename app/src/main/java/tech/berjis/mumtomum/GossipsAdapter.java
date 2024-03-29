package tech.berjis.mumtomum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator;

public class GossipsAdapter extends RecyclerView.Adapter<GossipsAdapter.ViewHolder> {
    private List<Gossips> listData;
    private static DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static String UID = mAuth.getCurrentUser().getUid();
    private List<GossipImages> imageList;
    private GossipImagesPagerAdapter pagerAdapter;

    GossipsAdapter(List<Gossips> listData) {
        this.listData = listData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gossips, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Gossips ld = listData.get(position);

        if (ld.getGossip() != null && !ld.getGossip().equals("")) {
            holder.post.setText(ld.getGossip());
        } else {
            holder.post.setVisibility(View.GONE);
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
        loadUser(ld.getUser(), holder);
        imageList = new ArrayList<>();
        imageLoader(ld.getGossipID(), holder);
        likeCounter(ld.getGossipID(), holder);
        commentCounter(ld.getGossipID(), holder);
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost(ld.getGossipID(), holder);
            }
        });
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gossipIntent = new Intent(holder.mView.getContext(), CommentsActivity.class);
                Bundle gossipBundle = new Bundle();
                gossipBundle.putString("gossipID", ld.getGossipID());
                gossipIntent.putExtras(gossipBundle);
                holder.mView.getContext().startActivity(gossipIntent);
            }
        });
        /*holder.mainImageCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gossipIntent = new Intent(holder.mView.getContext(), ImagesActivity.class);
                Bundle gossipBundle = new Bundle();
                gossipBundle.putString("gossipID", ld.getGossipID());
                gossipIntent.putExtras(gossipBundle);
                holder.mView.getContext().startActivity(gossipIntent);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView like, comment;
        ViewPager mainImage;
        CircleImageView userImage;
        EmojiTextView userName, postDate, post;
        ConstraintLayout rootView;
        View mView;
        CardView mainImageCard;
        TextView likeCount, commentCount;
        ScrollingPagerIndicator indicator;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mainImage = itemView.findViewById(R.id.mainImage);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
            postDate = itemView.findViewById(R.id.postDate);
            post = itemView.findViewById(R.id.post);
            rootView = itemView.findViewById(R.id.rootView);
            mainImageCard = itemView.findViewById(R.id.mainImageCard);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            likeCount = itemView.findViewById(R.id.likeCount);
            commentCount = itemView.findViewById(R.id.commentCount);
            indicator = itemView.findViewById(R.id.indicator);
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

    private void imageLoader(final String gossipID, final ViewHolder holder) {
        dbRef.child("GossipImages").child(gossipID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        GossipImages l = npsnapshot.getValue(GossipImages.class);
                        imageList.add(l);
                    }
                } else {
                    holder.mainImageCard.setVisibility(View.GONE);
                }
                pagerAdapter = new GossipImagesPagerAdapter(imageList, "small", "view", "gossip");
                pagerAdapter.notifyDataSetChanged();
                holder.mainImage.setAdapter(pagerAdapter);
                holder.indicator.attachToPager(holder.mainImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(holder.mView.getContext(), "Kuna shida mahali", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void likePost(final String gossipID, final ViewHolder holder) {
        dbRef.child("Likes").child(gossipID).child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    dbRef.child("Likes").child(gossipID).child(UID).setValue(true);
                    holder.like.animate()
                            .alpha(0.5f)
                            .setDuration(150)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    holder.like.animate()
                                            .alpha(1.0f)
                                            .setDuration(150)
                                            .setListener(new AnimatorListenerAdapter() {
                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                    super.onAnimationEnd(animation);
                                                }
                                            });
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likeCounter(final String gossipID, final ViewHolder holder) {
        dbRef.child("Likes").child(gossipID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long likes = dataSnapshot.getChildrenCount();
                holder.likeCount.setText(String.valueOf(likes));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void commentCounter(final String gossipID, final ViewHolder holder) {
        dbRef.child("Comments").child(gossipID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long comments = dataSnapshot.getChildrenCount();
                holder.commentCount.setText(String.valueOf(comments));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
