package tech.berjis.mumtomum;

class GroupMembers {
    private String group_id, member_id;
    private long joined_on;

    public GroupMembers(String group_id, String member_id, long joined_on) {
        this.group_id = group_id;
        this.member_id = member_id;
        this.joined_on = joined_on;
    }

    GroupMembers() {
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getMember_id() {
        return member_id;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public long getJoined_on() {
        return joined_on;
    }

    public void setJoined_on(long joined_on) {
        this.joined_on = joined_on;
    }
}
