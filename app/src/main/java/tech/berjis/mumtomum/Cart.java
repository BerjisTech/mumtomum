package tech.berjis.mumtomum;

public class Cart {
    private String product_id, name, user, item_id;
    private long price, quantity, time;

    public Cart(String product_id, String name, String user, String item_id, long price, long quantity, long time) {
        this.product_id = product_id;
        this.name = name;
        this.user = user;
        this.item_id = item_id;
        this.price = price;
        this.quantity = quantity;
        this.time = time;
    }

    public Cart() {
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
