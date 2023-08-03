package com.study.gmallpublisher1.controller;

/**
 * @Author wangziyu1
 * @Date 2022/9/6 19:03
 * @Version 1.0
 */
import com.study.gmallpublisher1.service.GmvService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

//@Controller
@RestController
@RequestMapping("/api/sugar")
public class SugarController {

    @Autowired
    private GmvService gmvService;

    @RequestMapping("/test")
    //@ResponseBody
    public String test1() {
        System.out.println("1111111111");
        return "success";
    }

    @RequestMapping("/test2")
    public String test2(@RequestParam("name") String nn,
                        @RequestParam(value = "age", defaultValue = "18") int age) {
        System.out.println(nn + ":" + age);
        return "success";
    }

    @RequestMapping("/gmv")
    public String getGmv(@RequestParam(value = "date", defaultValue = "0") int date) {

        if (date == 0) {
            date = today();
        }

        //查询数据
        BigDecimal gmv = gmvService.getGmv(date);

        //封装JSON返回
        return "{" +
                "  \"status\": 0," +
                "  \"msg\": \"\"," +
                "  \"data\": " + gmv + "" +
                "}";
    }

    @RequestMapping("/trademark")
    public String getGmvByTm(@RequestParam(value = "date", defaultValue = "0") int date,
                             @RequestParam(value = "limit", defaultValue = "5") int limit) {

        if (date == 0) {
            date = today();
        }

        Map gmvByTm = gmvService.getGmvByTm(date, limit);

        Set tmNames = gmvByTm.keySet();
        Collection gmvs = gmvByTm.values();

        return "{" +
                "  \"status\": 0," +
                "  \"msg\": \"\"," +
                "  \"data\": {" +
                "    \"categories\": [\"" +
                StringUtils.join(tmNames, "\",\"") +
                "\"]," +
                "    \"series\": [" +
                "      {" +
                "        \"name\": \"商品品牌\"," +
                "        \"data\": [" +
                StringUtils.join(gmvs, ",") +
                "        ]" +
                "      }" +
                "    ]" +
                "  }" +
                "}";
    }

    private int today() {
        long ts = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return Integer.parseInt(sdf.format(ts));
    }
}
