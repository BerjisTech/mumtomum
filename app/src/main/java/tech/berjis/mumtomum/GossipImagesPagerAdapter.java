package tech.berjis.mumtomum;

import android.content.Context;
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

    public GossipImagesPagerAdapter(List<GossipImages> listData) {
        this.listData = listData;
    }

    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, int position) {
        GossipImages ld = listData.get(position);
        ViewGroup layout = (ViewGroup) LayoutInflater.from(collection.getContext()).inflate(R.layout.gossip_image_pager, collection, false);
        ImageView pagerImage = layout.findViewById(R.id.gossipImage);
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
