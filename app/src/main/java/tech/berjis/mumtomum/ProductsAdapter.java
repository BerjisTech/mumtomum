package tech.berjis.mumtomum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator;

public class ProductsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> listData;
    private static DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private List<GossipImages> productImageList;
    private GossipImagesPagerAdapter pagerAdapter;
    private String delete;
    private static final int MENU_ITEM_VIEW_TYPE = 0;
    private static final int UNIFIED_NATIVE_AD_VIEW_TYPE = 1;

    ProductsAdapter(List<Object> listData, String delete) {
        this.listData = listData;
        this.delete = delete;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case UNIFIED_NATIVE_AD_VIEW_TYPE:
                View unifiedNativeLayoutView = LayoutInflater.from(
                        viewGroup.getContext()).inflate(R.layout.ad_layout,
                        viewGroup, false);
                return new UnifiedNativeAdViewHolder(unifiedNativeLayoutView);
            case MENU_ITEM_VIEW_TYPE:
                // Fall through.
            default:
                View menuItemLayoutView = LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.products, viewGroup, false);
                return new ProductItemViewHolder(menuItemLayoutView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case UNIFIED_NATIVE_AD_VIEW_TYPE:
                UnifiedNativeAd nativeAd = (UnifiedNativeAd) listData.get(position);
                populateNativeAdView(nativeAd, ((UnifiedNativeAdViewHolder) holder).getAdView());
                break;
            case MENU_ITEM_VIEW_TYPE:
                // fall through
            default:
                ProductItemViewHolder pHolder = (ProductItemViewHolder) holder;
                Products ld = (Products) listData.get(position);
                productImageList = new ArrayList<>();
                imageLoader(ld.getProduct_id(), pHolder);
                productLoader(ld, pHolder);
        }
    }


    @Override
    public int getItemViewType(int position) {

        Object recyclerViewItem = listData.get(position);
        if (recyclerViewItem instanceof UnifiedNativeAd) {
            return UNIFIED_NATIVE_AD_VIEW_TYPE;
        }
        return MENU_ITEM_VIEW_TYPE;
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public static class ProductItemViewHolder extends RecyclerView.ViewHolder {

        ImageView delete;
        TextView productTitle;
        ImageView mainImage;
        CardView mainImageCard;
        ScrollingPagerIndicator indicator;
        View mView;

        ProductItemViewHolder(View view) {
            super(view);
            delete = itemView.findViewById(R.id.delete);
            productTitle = itemView.findViewById(R.id.productTitle);
            mainImageCard = itemView.findViewById(R.id.mainImageCard);
            mainImage = itemView.findViewById(R.id.mainImage);
            indicator = itemView.findViewById(R.id.indicator);
            mView = itemView;
        }
    }

    private void imageLoader(final String productID, final ProductItemViewHolder holder) {
        productImageList.clear();
        dbRef.child("ProductImages").child(productID).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        Picasso.get().load(Objects.requireNonNull(npsnapshot.child("image").getValue()).toString()).into(holder.mainImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(holder.mView.getContext(), "Kuna shida mahali", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void productLoader(final Products ld, final ProductItemViewHolder pHolder) {
        pHolder.productTitle.setText(ld.getName());

        if (delete.equals("show")) {
            pHolder.delete.setVisibility(View.VISIBLE);
            pHolder.delete.setOnClickListener(new View.OnClickListener() {
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
                            Toast.makeText(pHolder.mView.getContext(), "Kuna shida mahali", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        if (mAuth.getCurrentUser() == null) {
            pHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pHolder.mView.getContext().startActivity(new Intent(pHolder.mView.getContext(), RegisterActivity.class));
                }
            });
        } else {
            pHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String product_id = ld.getProduct_id();
                    Intent chatIntent = new Intent(pHolder.mView.getContext(), ProductDetail.class);
                    Bundle chatBundle = new Bundle();
                    chatBundle.putString("product_id", product_id);
                    chatIntent.putExtras(chatBundle);
                    pHolder.mView.getContext().startActivity(chatIntent);
                }
            });
        }
    }

    private void populateNativeAdView(UnifiedNativeAd nativeAd,
                                      UnifiedNativeAdView adView) {
        // Some assets are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        NativeAd.Image icon = nativeAd.getIcon();

        if (icon == null) {
            adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(icon.getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd);
    }
}
