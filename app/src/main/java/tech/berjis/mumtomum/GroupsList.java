package tech.berjis.mumtomum;

public class GroupsList {
    String group_id, owner, chair, treasurer, secretary, name, logo;
    long created_on;

    public GroupsList(String group_id, String owner, String chair, String treasurer, String secretary, String name, String logo, long created_on) {
        this.group_id = group_id;
        this.owner = owner;
        this.chair = chair;
        this.treasurer = treasurer;
        this.secretary = secretary;
        this.name = name;
        this.logo = logo;
        this.created_on = created_on;
    }

    public GroupsList() {
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getChair() {
        return chair;
    }

    public void setChair(String chair) {
        this.chair = chair;
    }

    public String getTreasurer() {
        return treasurer;
    }

    public void setTreasurer(String treasurer) {
        this.treasurer = treasurer;
    }

    public String getSecretary() {
        return secretary;
    }

    public void setSecretary(String secretary) {
        this.secretary = secretary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public long getCreated_on() {
        return created_on;
    }

    public void setCreated_on(long created_on) {
        this.created_on = created_on;
    }
}
