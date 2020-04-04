package tech.berjis.mumtomum;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

    private List<Products> listData;
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


        Glide.with(holder.mView.getContext()).load(ld.getImage()).into(holder.background);
        holder.productTitle.setText(ld.getName());

        if (delete.equals("show")) {
            holder.delete.setVisibility(View.VISIBLE);
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(ld.getImage());
                    final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // File deleted successfully
                            dbRef.child("Products").child(ld.getProduct_id()).removeValue();
                            holder.mView.setVisibility(View.GONE);
                            holder.delete.setVisibility(View.GONE);
                            holder.productTitle.setVisibility(View.GONE);
                            holder.background.setVisibility(View.GONE);
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

    @Override
    public int getItemCount() {
        return listData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView background, delete;
        TextView productTitle;
        View mView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            delete = itemView.findViewById(R.id.delete);
            background = itemView.findViewById(R.id.background);
            productTitle = itemView.findViewById(R.id.productTitle);
            mView = itemView;
        }
    }
}
