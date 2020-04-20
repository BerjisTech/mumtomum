package tech.berjis.mumtomum;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<Cart> listData;
    private static DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static String UID = mAuth.getCurrentUser().getUid();
    private List<GossipImages> imageList;
    private GossipImagesPagerAdapter pagerAdapter;

    CartAdapter(List<Cart> listData) {
        this.listData = listData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Cart ld = listData.get(position);


        imageList = new ArrayList<>();


        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String output = "kshs " + nf.format(ld.getPrice());

        imageLoader(ld.getProduct_id(), holder);
        holder.productName.setText(ld.getName());
        holder.productQuantity.setText(String.valueOf(ld.getQuantity()));
        holder.productPrice.setText(output);


        /*holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String product_id = ld.getProduct_id();
                Intent chatIntent = new Intent(holder.mView.getContext(), ProductDetail.class);
                Bundle chatBundle = new Bundle();
                chatBundle.putString("product_id", product_id);
                chatIntent.putExtras(chatBundle);
                holder.mView.getContext().startActivity(chatIntent);
            }
        });*/

        holder.productQuantityPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long quantity = Long.parseLong(holder.productQuantity.getText().toString());
                long new_quantity = quantity + 1;
                holder.productQuantity.setText(String.valueOf(new_quantity));
            }
        });

        holder.productQuantityMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long quantity = Long.parseLong(holder.productQuantity.getText().toString());
                long new_quantity = quantity - 1;
                holder.productQuantity.setText(String.valueOf(new_quantity));
            }
        });
    }


    @Override
    public int getItemCount() {
        return listData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView productQuantityMinus, productQuantityPlus;
        EditText productQuantity;
        TextView productName, productPrice;
        ViewPager mainImage;
        ScrollingPagerIndicator indicator;
        View mView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            mainImage = itemView.findViewById(R.id.mainImage);
            indicator = itemView.findViewById(R.id.indicator);
            productQuantityMinus = itemView.findViewById(R.id.productQuantityMinus);
            productQuantityPlus = itemView.findViewById(R.id.productQuantityPlus);
            productQuantity = itemView.findViewById(R.id.productQuantity);
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
                    holder.mainImage.setVisibility(View.GONE);
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
