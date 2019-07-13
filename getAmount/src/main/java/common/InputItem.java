package common;
import com.alibaba.fastjson.annotation.JSONField;

public class InputItem {
    @JSONField(name = "id")
    public String id;
    @JSONField(name = "number")
    public int number;
    public InputItem(String id, int number) {
        this.id = id;
        this.number = number;
    }
}
