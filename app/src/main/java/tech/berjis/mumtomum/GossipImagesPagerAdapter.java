package tech.berjis.mumtomum;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GossipImagesPagerAdapter extends PagerAdapter {

    private List<GossipImages> listData;
    private String type;
    private String mode;
    private String variant;
    private boolean doNotifyDataSetChangedOnce = false;

    public GossipImagesPagerAdapter(List<GossipImages> listData, String type, String mode, String variant) {
        this.listData = listData;
        this.type = type;
        this.mode = mode;
        this.variant = variant;
        //notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(@NonNull final ViewGroup collection, int position) {
        final GossipImages ld = listData.get(position);
        ViewGroup layout = null;

        if (type.equals("large")) {
            layout = (ViewGroup) LayoutInflater.from(collection.getContext()).inflate(R.layout.gossip_gallery, collection, false);
        }

        if (type.equals("small") || type.equals("smally")) {
            layout = (ViewGroup) LayoutInflater.from(collection.getContext()).inflate(R.layout.gossip_image_pager, collection, false);
        }

        ImageView pagerImage = layout.findViewById(R.id.gossipImage);
        ImageView trashIcon = layout.findViewById(R.id.delete);

        if (mode.equals("edit")) {
            trashIcon.setVisibility(View.VISIBLE);
        }

        if (mode.equals("view")) {
            trashIcon.setVisibility(View.GONE);
            if (variant.equals("gossip") && type.equals("small")) {
                pagerImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent gossipIntent = new Intent(collection.getContext(), ImagesActivity.class);
                        Bundle gossipBundle = new Bundle();
                        gossipBundle.putString("gossipID", ld.getParent_id());
                        gossipBundle.putString("mode", "view");
                        gossipBundle.putString("variant", "gossip");
                        gossipIntent.putExtras(gossipBundle);
                        collection.getContext().startActivity(gossipIntent);
                    }
                });
            }
            if (variant.equals("product") && type.equals("small")) {
                pagerImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent gossipIntent = new Intent(collection.getContext(), ImagesActivity.class);
                        Bundle gossipBundle = new Bundle();
                        gossipBundle.putString("gossipID", ld.getParent_id());
                        gossipBundle.putString("mode", "view");
                        gossipBundle.putString("variant", "product");
                        gossipIntent.putExtras(gossipBundle);
                        collection.getContext().startActivity(gossipIntent);
                    }
                });
            }
            if (variant.equals("product") && type.equals("smally")) {
                pagerImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String product_id = ld.getParent_id();
                        Intent chatIntent = new Intent(collection.getContext(), ProductDetail.class);
                        Bundle chatBundle = new Bundle();
                        chatBundle.putString("product_id", product_id);
                        chatIntent.putExtras(chatBundle);
                        collection.getContext().startActivity(chatIntent);
                    }
                });
            }
        }

        if (variant.equals("gossip")) {
            final ViewGroup finalLayout = layout;
            trashIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(ld.getImage());
                    final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // File deleted successfully
                            dbRef.child("GossipImages").child(ld.getParent_id()).child(ld.getImage_id()).removeValue();
                            finalLayout.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                        }
                    });
                }
            });
        }

        if (variant.equals("product")) {
            final ViewGroup finalLayout = layout;
            trashIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(ld.getImage());
                    final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // File deleted successfully
                            dbRef.child("ProductImages").child(ld.getParent_id()).child(ld.getImage_id()).removeValue();
                            finalLayout.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                        }
                    });
                }
            });
        }

        Picasso.get().load(ld.getImage()).into(pagerImage);
        collection.addView(layout);
        return layout;
    }

    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
        container.removeView((View) view);
    }

    @Override
    public int getCount() {
        /*if (doNotifyDataSetChangedOnce) {
            doNotifyDataSetChangedOnce = false;
            notifyDataSetChanged();
        }*/
        return listData.size();
    }



    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
