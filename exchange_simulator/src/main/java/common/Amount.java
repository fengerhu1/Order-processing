package common;

import com.alibaba.fastjson.annotation.JSONField;

public class Amount {
    @JSONField(name = "USD")
    public float USD;
    @JSONField(name = "RMB")
    public float RMB;
    @JSONField(name = "JPY")
    public float JPY;
    @JSONField(name = "EUR")
    public float EUR;
    @JSONField(name = "CNY")
    public float CNY;

    public Amount(float USD,float RMB,float JPY,float EUR,float CNY)
    {
        this.CNY = CNY;
        this.EUR = EUR;
        this.JPY = JPY;
        this.RMB = RMB;
        this.USD = USD;
    }
}
