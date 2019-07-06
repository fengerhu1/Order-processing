package sample;
import sample.Item;

import java.util.HashSet;
import java.util.Set;

public class OrderInfo {
    private Integer order_id;
    private Integer use_id;
    private String initiator;
    private Long time;


    private Set<Item> items = new HashSet<Item>();

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getTime() {
        return time;
    }

    public void setOrder_id(Integer order_id) {
        this.order_id = order_id;
    }

    public Integer getOrder_id() {
        return order_id;
    }

    public void setItems(Set<Item> items) {
        this.items = items;
    }

    public Set<Item> getItems() {
        return items;
    }

    public void setUse_id(Integer use_id) {
        this.use_id = use_id;
    }

    public Integer getUse_id() {
        return use_id;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getInitiator() {
        return initiator;
    }
}
