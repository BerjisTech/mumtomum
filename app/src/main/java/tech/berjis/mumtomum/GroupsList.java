package tech.berjis.mumtomum;

public class GroupsList {
    private String group_id, owner, chair, treasurer, secretary, name, logo, description, country, code, symbol;
    private long created_on, goal;

    public GroupsList(String group_id, String owner, String chair, String treasurer, String secretary, String name, String logo, String description, String country, String code, String symbol, long created_on, long goal) {
        this.group_id = group_id;
        this.owner = owner;
        this.chair = chair;
        this.treasurer = treasurer;
        this.secretary = secretary;
        this.name = name;
        this.logo = logo;
        this.description = description;
        this.country = country;
        this.code = code;
        this.symbol = symbol;
        this.created_on = created_on;
        this.goal = goal;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public long getCreated_on() {
        return created_on;
    }

    public void setCreated_on(long created_on) {
        this.created_on = created_on;
    }

    public long getGoal() {
        return goal;
    }

    public void setGoal(long goal) {
        this.goal = goal;
    }
}
