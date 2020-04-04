package tech.berjis.mumtomum;

public class Products {

    private String category, date, description, image, seller, status, time, product_id, name;
    private Long price;

    public Products(String category, String date, String description, String image, String seller, String status, String time, String product_id, String name, Long price) {
        this.category = category;
        this.date = date;
        this.description = description;
        this.image = image;
        this.seller = seller;
        this.status = status;
        this.time = time;
        this.product_id = product_id;
        this.name = name;
        this.price = price;
    }

    public Products() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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
}
