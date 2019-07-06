package src;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.InputCase;
import common.InputItem;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import sample.Item;
import sample.Movie;
import sample.OrderInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Launcher {
    public static void main(String[] args) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure()
                .build();
        SessionFactory sessionFactory = null;
        Session session = null;
        try {
            sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
            session = sessionFactory.openSession();

            Movie movie = new Movie();
            movie.setId(1);
            movie.setName("速度与激情8");
            movie.setDescription("多米尼克（范·迪塞尔 Vin Diesel 饰）与莱蒂（米歇尔·罗德里格兹 Michelle Rodriguez 饰）共度蜜月，布莱恩与米娅退出了赛车界，这支曾环游世界的顶级飞车家族队伍的生活正渐趋平淡。然而，一位神秘女子Cipher（查理兹·塞隆 Charlize T heron 饰）的出现，令整个队伍卷入信任与背叛的危机，面临前所未有的考验。");

            ArrayList<InputItem> itemlist = new ArrayList<InputItem>();
            InputItem item1 = new InputItem("1", 2);
            InputItem item2 = new InputItem("3", 1);
            itemlist.add(item1);
            itemlist.add(item2);
            InputCase inputcase = new InputCase(123456,"RMB",1234567L,itemlist);
            String msg = JSON.toJSONString(inputcase);
            JSONObject jsonObj = (JSONObject) JSON.parse(msg);
            //System.out.println(jsonObj);
            Integer use_id = jsonObj.getInteger("use_id");
            String initiator = jsonObj.getString("initiator");
            Long time = jsonObj.getLong("time");
            JSONArray templist = jsonObj.getJSONArray("item");
            //ArrayList<InputItem> itemlist = new ArrayList<InputItem>();
            Set<Item> itemlist2 = new HashSet<Item>();
            if (templist != null) {
                for (int i=0;i<templist.size();i++){
                    JSONObject tempitem = (JSONObject)JSON.parse(templist.getString(i));
                    String id = tempitem.getString("id");
                    Integer item_number = tempitem.getInteger("number");
                    Item inputItem = new Item();
                    inputItem.setId(id);
                    inputItem.setNumber(item_number);
                    itemlist2.add(inputItem);
                }
            }

            OrderInfo orderInfo= new OrderInfo();
            orderInfo.setInitiator(initiator);
            orderInfo.setItems(itemlist2);
            orderInfo.setTime(time);
            orderInfo.setUse_id(use_id);
            session.beginTransaction();
            session.save(orderInfo);


            //session.save(movie);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }

            if(sessionFactory != null) {
                sessionFactory.close();
            }
        }
    }
}