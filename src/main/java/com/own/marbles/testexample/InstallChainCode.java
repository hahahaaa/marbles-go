package com.own.marbles.testexample;


import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;

@RestController()
@RequestMapping("/test")
public class InstallChainCode {
    private static final Logger log = LoggerFactory.getLogger(InstallChainCode.class);

    @Resource
    Util util;

    @RequestMapping("query")
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
        Peer peer = util.getPeer(client);
        Channel channel = util.getChannel(client, peer);
        log.info("Channel: " + channel.getName());


        String key = "";
        // 构建proposal
        QueryByChaincodeRequest req = client.newQueryProposalRequest();
        // 指定要调用的chaincode
        ChaincodeID cid = ChaincodeID.newBuilder().setName("fabcar").build();
        req.setChaincodeID(cid);
        req.setFcn("queryAllCars");
        req.setArgs(key);
        System.out.println("Querying for " + key);
        Collection<ProposalResponse> resps = channel.queryByChaincode(req);
        for (ProposalResponse resp : resps) {
            String payload = new String(resp.getChaincodeActionResponsePayload());
            log.info("response: " + payload);
        }

    }
}
