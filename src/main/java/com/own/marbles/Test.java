package com.own.marbles;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test {

    @GetMapping(value = "sdf")
    public String test(){
        System.out.println("adf");
        return "test";
    }

}
