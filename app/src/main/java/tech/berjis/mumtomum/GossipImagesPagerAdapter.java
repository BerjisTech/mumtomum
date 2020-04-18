package tech.berjis.mumtomum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import java.util.List;

public class GossipImagesPagerAdapter extends PagerAdapter {

    private Context context;
    private List<GossipImages> listData;
    private String type;

    public GossipImagesPagerAdapter(List<GossipImages> listData, String type) {
        this.listData = listData;
        this.type = type;
    }

    @Override
    public Object instantiateItem(@NonNull final ViewGroup collection, int position) {
        final GossipImages ld = listData.get(position);
        ViewGroup layout = null;
        if (type.equals("gallery")) {
            layout = (ViewGroup) LayoutInflater.from(collection.getContext()).inflate(R.layout.gossip_gallery, collection, false);
        }
        if (type.equals("small")) {
            layout = (ViewGroup) LayoutInflater.from(collection.getContext()).inflate(R.layout.gossip_image_pager, collection, false);
        }
        ImageView pagerImage = layout.findViewById(R.id.gossipImage);
        pagerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gossipIntent = new Intent(collection.getContext(), ImagesActivity.class);
                Bundle gossipBundle = new Bundle();
                gossipBundle.putString("gossipID", ld.getGossip_id());
                gossipIntent.putExtras(gossipBundle);
                collection.getContext().startActivity(gossipIntent);
            }
        });
        Picasso.get().load(ld.getImage()).into(pagerImage);
        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
        container.removeView((View) view);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
