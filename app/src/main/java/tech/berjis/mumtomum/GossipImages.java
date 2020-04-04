package tech.berjis.mumtomum;

public class GossipImages {
    private String image, image_id, gossip_id;

    public GossipImages(String image, String image_id, String gossip_id) {
        this.image = image;
        this.image_id = image_id;
        this.gossip_id = gossip_id;
    }

    public GossipImages(){

    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public String getGossip_id() {
        return gossip_id;
    }

    public void setGossip_id(String gossip_id) {
        this.gossip_id = gossip_id;
    }
}
