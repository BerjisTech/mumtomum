package tech.berjis.mumtomum;

public class Invites {

    private String group_id, invite_code, inviter, status;

    public Invites(String group_id, String invite_code, String inviter, String status) {
        this.group_id = group_id;
        this.invite_code = invite_code;
        this.inviter = inviter;
        this.status = status;
    }

    public Invites() {
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getInvite_code() {
        return invite_code;
    }

    public void setInvite_code(String invite_code) {
        this.invite_code = invite_code;
    }

    public String getInviter() {
        return inviter;
    }

    public void setInviter(String inviter) {
        this.inviter = inviter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
