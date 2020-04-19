package tech.berjis.mumtomum;

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
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

    private List<Products> listData;
    private static DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static String UID = mAuth.getCurrentUser().getUid();
    private List<GossipImages> imageList;
    private GossipImagesPagerAdapter pagerAdapter;
    private String delete;

    ProductsAdapter(List<Products> listData, String delete) {
        this.listData = listData;
        this.delete = delete;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.products, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Products ld = listData.get(position);


        imageList = new ArrayList<>();
        imageLoader(ld.getProduct_id(), holder);
        holder.productTitle.setText(ld.getName());

        if (delete.equals("show")) {
            holder.delete.setVisibility(View.VISIBLE);
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbRef.child("Products").child(ld.getProduct_id()).removeValue();
                    dbRef.child("ProductImages").child(ld.getProduct_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (final DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                                    StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(npsnapshot.child("image").getValue().toString());
                                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // File deleted successfully
                                            dbRef.child("ProductImages").child(ld.getProduct_id()).child(npsnapshot.child("image_id").getValue().toString()).removeValue();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Uh-oh, an error occurred!
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(holder.mView.getContext(), "Kuna shida mahali", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        if (mAuth.getCurrentUser() == null) {
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.mView.getContext().startActivity(new Intent(holder.mView.getContext(), RegisterActivity.class));
                }
            });
        }else{
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String product_id = ld.getProduct_id();
                    Intent chatIntent = new Intent(holder.mView.getContext(), ProductDetail.class);
                    Bundle chatBundle = new Bundle();
                    chatBundle.putString("product_id", product_id);
                    chatIntent.putExtras(chatBundle);
                    holder.mView.getContext().startActivity(chatIntent);
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return listData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView delete;
        TextView productTitle;
        ViewPager mainImage;
        CardView mainImageCard;
        ScrollingPagerIndicator indicator;
        View mView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            delete = itemView.findViewById(R.id.delete);
            productTitle = itemView.findViewById(R.id.productTitle);
            mainImageCard = itemView.findViewById(R.id.mainImageCard);
            mainImage = itemView.findViewById(R.id.mainImage);
            indicator = itemView.findViewById(R.id.indicator);
            mView = itemView;
        }
    }

    private void imageLoader(final String productID, final ViewHolder holder) {
        dbRef.child("ProductImages").child(productID).addListenerForSingleValueEvent(new ValueEventListener() {
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
                pagerAdapter = new GossipImagesPagerAdapter(imageList, "smally", "view", "product");
                holder.mainImage.setAdapter(pagerAdapter);
                holder.indicator.attachToPager(holder.mainImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(holder.mView.getContext(), "Kuna shida mahali", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
