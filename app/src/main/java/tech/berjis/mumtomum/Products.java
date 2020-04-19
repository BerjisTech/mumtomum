package tech.berjis.mumtomum;

public class Products {

    private String category, description, seller, status, product_id, name;
    private boolean pickup, delivery;
    private long price, date;

    public Products(String category, String description, String seller, String status, String product_id, String name, boolean pickup, boolean delivery, long price, long date) {
        this.category = category;
        this.description = description;
        this.seller = seller;
        this.status = status;
        this.product_id = product_id;
        this.name = name;
        this.pickup = pickup;
        this.delivery = delivery;
        this.price = price;
        this.date = date;
    }

    public Products() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPickup() {
        return pickup;
    }

    public void setPickup(boolean pickup) {
        this.pickup = pickup;
    }

    public boolean isDelivery() {
        return delivery;
    }

    public void setDelivery(boolean delivery) {
        this.delivery = delivery;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
