package tech.berjis.mumtomum;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

public class GossipImagesAdapter extends RecyclerView.Adapter<GossipImagesAdapter.ViewHolder> {
    private List<GossipImages> listData;
    private String del;

    GossipImagesAdapter(List<GossipImages> listData, String del) {
        this.listData = listData;
        this.del = del;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gossip_images, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final GossipImages ld = listData.get(position);

        if (del.equals("hide")) {
            holder.removeImage.setVisibility(View.GONE);
        }
        Glide.with(holder.mView.getContext()).load(ld.getImage()).into(holder.gossipImage);
        holder.removeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(ld.getImage());
                final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // File deleted successfully
                        dbRef.child("GossipImages").child(ld.getParent_id()).child(ld.getImage_id()).removeValue();
                        holder.mView.setVisibility(View.GONE);
                        holder.gossipImage.setVisibility(View.GONE);
                        holder.removeImage.setVisibility(View.GONE);
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

    @Override
    public int getItemCount() {
        return listData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView gossipImage, removeImage;
        View mView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            gossipImage = itemView.findViewById(R.id.gossipImage);
            removeImage = itemView.findViewById(R.id.removeImage);
            mView = itemView;
        }
    }
}
