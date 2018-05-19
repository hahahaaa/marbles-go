package com.own.marbles.example;


import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class HFJavaExample {


    private static final Logger log = LoggerFactory.getLogger(HFJavaExample.class);

    @Resource
    Util util;

    @RequestMapping("test")
    public void testClient() throws Exception {
        // create fabric-ca client

        HFCAClient caClient = util.getHfCaClient("http://127.0.0.1:7054", null);

        // enroll or load admin
        HFUser admin = util.getAdmin(caClient);
        log.info(admin.toString());

        // register and enroll new user
        HFUser hfUser = util.getUser(caClient, admin, "wesker");
        log.info(hfUser.toString());

        // get HFC client instance
        HFClient client = util.getHfClient();
        // set user context
        client.setUserContext(admin);

        // get HFC channel using the client
        Channel channel = util.getChannel(client);
        log.info("Channel: " + channel.getName());


        // query alll account list
//        queryBlockChain(client, "list");
//
//
//        //update
//        invokeBlockChain(client, "update", "ACCOUNT1", "jill_1");
//
//        // query by condition
//        queryBlockChain(client, "query", "ACCOUNT1");


    }


}
