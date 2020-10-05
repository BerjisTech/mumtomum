package tech.berjis.mumtomum;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbRef.child("Cart").child(UID).child(ld.getItem_id()).child("status").setValue(2);
                holder.mView.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public int getItemCount() {
        return listData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mainImage;
        TextView productName, productPrice, remove;
        View mView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            mainImage = itemView.findViewById(R.id.mainImage);
            remove = itemView.findViewById(R.id.remove);
            mView = itemView;
        }
    }

    private void imageLoader(final String productID, final ViewHolder holder) {
        dbRef.child("ProductImages").child(productID).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        Picasso.get().load(Objects.requireNonNull(npsnapshot.child("image").getValue()).toString()).into(holder.mainImage);
                    }
                } else {
                    holder.mainImage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(holder.mView.getContext(), "Kuna shida mahali", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
