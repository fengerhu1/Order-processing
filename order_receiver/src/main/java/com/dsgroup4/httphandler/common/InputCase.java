package com.dsgroup4.httphandler.common;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;

public class InputCase {
    @JSONField(name = "user_id")
    public int user_id;
    @JSONField(name = "initiator")
    public String initiator;
    @JSONField(name = "time")
    public Long time;
    @JSONField(name = "items")
    public List<InputItem> itemlist;

    public InputCase(int user_id, String initiator,Long time,List<InputItem> itemlist) {
        this.user_id = user_id;
        this.initiator = initiator;
        this.time = time;
        this.itemlist = itemlist;
    }
}