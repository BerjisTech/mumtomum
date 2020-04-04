package tech.berjis.mumtomum;

public class ChatList {
    public String userId;

    public ChatList(String userId) {
        this.userId = userId;
    }

    public ChatList() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
