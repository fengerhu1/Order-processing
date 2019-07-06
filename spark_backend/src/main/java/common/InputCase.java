package common;
import java.util.ArrayList;
import com.alibaba.fastjson.annotation.JSONField;
import common.InputItem;
public class InputCase {
    @JSONField(name = "use_id")
    public int use_id;
    @JSONField(name = "initiator")
    public String initiaiator;
    @JSONField(name = "time")
    public Long time;
    @JSONField(name = "item")
    public  ArrayList<InputItem> itemlist;

    public InputCase(int use_id, String initiaiator,Long time,ArrayList<InputItem> itemlist) {
        this.use_id = use_id;
        this.initiaiator = initiaiator;
        this.time = time;
        this.itemlist = itemlist;
    }
}