package com.dsgroup4.httphandler.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dsgroup4.httphandler.common.Result;
import com.dsgroup4.httphandler.common.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

public class GetOrder {

    @Autowired
    ResultRepository resultRepository;

    @RequestMapping("/get_all_orders")
    public String getAllOrder(){
        List<Result> results = resultRepository.getAll();
        return  JSONObject.toJSONString(results);
    }

    @RequestMapping("/get_order")
    public String getOrder(String id){
        Result result = resultRepository.getResultById(id);
        return JSONObject.toJSONString(result);
    }
}
