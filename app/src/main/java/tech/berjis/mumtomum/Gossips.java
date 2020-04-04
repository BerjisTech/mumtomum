package tech.berjis.mumtomum;

public class Gossips {
    private Long date;
    private String type, user, gossip, gossipID;

    public Gossips(Long date, String type, String user, String gossip, String gossipID) {
        this.date = date;
        this.type = type;
        this.user = user;
        this.gossip = gossip;
        this.gossipID = gossipID;
    }

    public Gossips() {

    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getGossip() {
        return gossip;
    }

    public void setGossip(String gossip) {
        this.gossip = gossip;
    }

    public String getGossipID() {
        return gossipID;
    }

    public void setGossipID(String gossipID) {
        this.gossipID = gossipID;
    }
}
