package com.dsgroup4.httphandler.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dsgroup4.httphandler.common.Result;
import com.dsgroup4.httphandler.common.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping
public class GetOrder {

    @Autowired
    ResultRepository resultRepository;

    @RequestMapping("/get_all_orders")
    @ResponseBody
    public String getAllOrder(){
        List<Result> results = resultRepository.getAll();
        return  JSONObject.toJSONString(results);
    }

    @RequestMapping("/get_order")
    @ResponseBody
    public String getOrder(String order_id){
        List<Result> results = resultRepository.getResultById(order_id);
        return JSONObject.toJSONString(results);
    }

    @RequestMapping("/test")
    @ResponseBody
    public String test(){
        return "Hello,world!";
    }
}
