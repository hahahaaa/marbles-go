package com.own.marbles.app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class Test {


    @GetMapping(value = "sdf")
    public Object test() {
        System.out.println("adf");
        return "";
    }

}
