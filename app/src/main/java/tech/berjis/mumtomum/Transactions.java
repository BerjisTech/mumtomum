package tech.berjis.mumtomum;

public class Transactions {

    private long amount, end_time, time_start;
    private String status, text_ref, type, user, narration, group;

    public Transactions(long amount, long end_time, long time_start, String status, String text_ref, String type, String user, String narration, String group) {
        this.amount = amount;
        this.end_time = end_time;
        this.time_start = time_start;
        this.status = status;
        this.text_ref = text_ref;
        this.type = type;
        this.user = user;
        this.narration = narration;
        this.group = group;
    }

    public Transactions(){

    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getEnd_time() {
        return end_time;
    }

    public void setEnd_time(long end_time) {
        this.end_time = end_time;
    }

    public long getTime_start() {
        return time_start;
    }

    public void setTime_start(long time_start) {
        this.time_start = time_start;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getText_ref() {
        return text_ref;
    }

    public void setText_ref(String text_ref) {
        this.text_ref = text_ref;
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

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
