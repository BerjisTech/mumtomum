package tech.berjis.mumtomum;

public class Products {

    private String category, description, seller, status, product_id, name;
    private long price, date;

    public Products(String category, String description, String seller, String status, String product_id, String name, Long price, Long date) {
        this.category = category;
        this.description = description;
        this.seller = seller;
        this.status = status;
        this.product_id = product_id;
        this.name = name;
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

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }
}
