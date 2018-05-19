package com.own.marbles.app;


import com.own.marbles.example.HFJavaExample;
import com.own.marbles.example.HFUser;
import com.own.marbles.example.Util;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController()
@RequestMapping("/chaincode")
public class InstallChainCode {
    private static final Logger log = LoggerFactory.getLogger(InstallChainCode.class);

    @Resource
    Util util;

    @RequestMapping("install")
    public void install() throws Exception {
        HFCAClient caClient = util.getHfCaClient("http://127.0.0.1:7054", null);

        // enroll or load admin
        HFUser admin = util.getAdmin(caClient);
        log.info(admin.toString());

        // get HFC client instance
        HFClient client = util.getHfClient();
        // set user context
        client.setUserContext(admin);

        // get HFC channel using the client
        Channel channel = util.getChannel(client);
        log.info("Channel: " + channel.getName());


    }
}
