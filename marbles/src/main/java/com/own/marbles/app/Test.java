package com.own.marbles.app;

import com.own.marbles.config.ConnectionConfig;
import com.own.marbles.config.MarblesConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class Test {

    @Resource
    MarblesConfig marblesConfig;
    @Resource
    ConnectionConfig connectionConfig;

    @GetMapping(value = "sdf")
    public Object test() {
        System.out.println("adf");
        return connectionConfig.getConfigObject();
    }

}
