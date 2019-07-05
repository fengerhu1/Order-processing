package src;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.InputCase;
import common.InputItem;
import org.apache.spark.api.java.function.Function;
import org.hibernate.Session;
import sample.Item;
import sample.OrderInfo;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class myFunction implements Function<Tuple2<String, String>, String> {
    private Session session;
    public String call(Tuple2<String, String> v1) throws Exception {
        JSONObject jsonObj = (JSONObject) JSON.parse(v1._2());
        //System.out.println(jsonObj);
        Integer use_id = jsonObj.getInteger("use_id");
        String initiator = jsonObj.getString("initiator");
        Long time = jsonObj.getLong("time");
        JSONArray templist = jsonObj.getJSONArray("item");
        //ArrayList<InputItem> itemlist = new ArrayList<InputItem>();
        Set<Item> itemlist = new HashSet<Item>();
        if (templist != null) {
            for (int i=0;i<templist.size();i++){
                JSONObject tempitem = (JSONObject)JSON.parse(templist.getString(i));
                String id = tempitem.getString("id");
                Integer item_number = tempitem.getInteger("number");
                Item inputItem = new Item();
                inputItem.setId(id);
                inputItem.setNumber(item_number);
                itemlist.add(inputItem);
            }
        }

        OrderInfo orderInfo= new OrderInfo();
        orderInfo.setInitiator(initiator);
        orderInfo.setItems(itemlist);
        orderInfo.setTime(time);
        orderInfo.setUse_id(use_id);
        session.beginTransaction();
        session.save(orderInfo);
        session.getTransaction().commit();
        return v1._2();
    }
    public myFunction(Session session) {
        this.session = session;;
    }
}
