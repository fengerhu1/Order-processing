package sample;

public class Item {
    private Integer item_id;
    private String id;
    private Integer number;
    private OrderInfo OrderInfo;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Integer getItem_id() {
        return item_id;
    }

    public void setItem_id(Integer item_id) {
        this.item_id = item_id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public void setOrderInfo(OrderInfo OrderInfo) {
        this.OrderInfo = OrderInfo;
    }

    public OrderInfo getOrderInfo() {
        return OrderInfo;
    }
}
