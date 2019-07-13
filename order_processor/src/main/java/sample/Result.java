package sample;

public class Result {

    private Long id;
    private int user_id;
    private String initiator;
    private boolean success;
    private double paid;
    private String order_id;

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public boolean isSuccess() {
        return success;
    }

    public double getPaid() {
        return paid;
    }

    public int getUser_id() {
        return user_id;
    }

    public Long getId() {
        return id;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public void setPaid(double paid) {
        this.paid = paid;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
