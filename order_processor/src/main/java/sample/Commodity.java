package sample;

public class Commodity {
    private int Id;
    private String Name;
    private float price;
    private String currency;
    private int inventory;

    public void setId(int id) {
        Id = id;
    }

    public int getId() {
        return Id;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getName() {
        return Name;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }

    public float getPrice() {
        return price;
    }

    public int getInventory() {
        return inventory;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
