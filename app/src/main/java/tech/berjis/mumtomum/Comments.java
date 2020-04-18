package tech.berjis.mumtomum;

public class Comments {
    private String type, text, sender, gossip_id, chat_id;
    long date;

    public Comments(String type, String text, String sender, String gossip_id, String chat_id, long date) {
        this.type = type;
        this.text = text;
        this.sender = sender;
        this.gossip_id = gossip_id;
        this.chat_id = chat_id;
        this.date = date;
    }

    public Comments() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getGossip_id() {
        return gossip_id;
    }

    public void setGossip_id(String gossip_id) {
        this.gossip_id = gossip_id;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
