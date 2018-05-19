package com.own.marbles.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Component
public class MarblesConfig {
    private String configPath = "classpath:config/marbles_local.json";

    private JSONObject configObject;

    @PostConstruct
    public void init() {
        try {
            File cfgFile = ResourceUtils.getFile(configPath);
            InputStream inputStream = new FileInputStream(cfgFile);
            String text = IOUtils.toString(inputStream, "utf8");
            configObject = JSON.parseObject(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject getConfigObject() {
        return configObject;
    }

}
