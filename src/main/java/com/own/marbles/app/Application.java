package com.own.marbles.app;

import com.own.marbles.config.NetWorkConfigLoader;
import com.own.marbles.config.model.SampleOrg;
import com.own.marbles.config.model.SampleStore;
import com.own.marbles.config.model.SampleUser;
import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "app")
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Resource
    NetWorkConfigLoader configLoader;
    @Resource
    NetworkConfig networkConfig;


    @GetMapping(value = "regist")
    public Object regist() throws Exception {
        String orgName = "Org1";
        SampleOrg sampleOrg = configLoader.getSampleOrg(orgName);
        HFCAClient hfcaClient = sampleOrg.getCAClient();
        SampleUser orgAdmin = sampleOrg.getAdmin();
        SampleStore sampleStore = sampleOrg.getSampleStore();

        SampleUser user = sampleStore.getMember("User4", orgName);

        if (!user.isRegistered()) {
            RegistrationRequest rr = new RegistrationRequest(user.getName(),
                    "org1.department1");
            user.setEnrollmentSecret(hfcaClient.register(rr, orgAdmin));
            return "success";
        }

        return "isRegistered";
    }

    @GetMapping(value = "enroll")
    public Object enroll() throws Exception {
        String orgName = "Org1";
        SampleOrg sampleOrg = configLoader.getSampleOrg(orgName);
        HFCAClient hfcaClient = sampleOrg.getCAClient();
        SampleStore sampleStore = sampleOrg.getSampleStore();

        SampleUser user = sampleStore.getMember("User4", orgName);
        if (!user.isEnrolled()) {
            user.setEnrollment(hfcaClient.enroll(user.getName(), user.getEnrollmentSecret()));
            return "sucess";
        }
        return "isEnrolled";
    }


    //从配置文件中读取channel
    @GetMapping(value = "channel")
    public Object loadChannelFromConfig() throws Exception {

        String orgName = "Org1";
        SampleOrg sampleOrg = configLoader.getSampleOrg(orgName);
        log.info(sampleOrg.getPeerNames() + "");

        HFClient client = sampleOrg.getClient();
        client.setUserContext(sampleOrg.getPeerAdmin());

        Channel channel = client.loadChannelFromConfig("mychannel", networkConfig);
        Peer peer = channel.getPeers().iterator().next();
        log.info(peer.getName());

        return peer.getName();
    }

    //手动创建channel 并且peer加入channel
//    @GetMapping(value = "channel/create")
//    public Object createChannel() throws Exception {
//        String orgName = "Org1";
//        SampleOrg sampleOrg = configLoader.getSampleOrg(orgName);
//        log.info(sampleOrg.getPeerNames() + "");
//
//        HFClient client = sampleOrg.getClient();
//        client.setUserContext(sampleOrg.getPeerAdmin());
//
//        Channel testChannel = client.newChannel("testChannel3");
//
//        String orderName = sampleOrg.getOrdererNames().iterator().next();
//        Properties ordererProperties = sampleOrg.getOrdererProperties((String) sampleOrg.getOrdererNames().toArray()[0]);
//        ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime",
//                new Object[]{5L, TimeUnit.MINUTES});
//        ordererProperties.put(
//                "grpc.NettyChannelBuilderOption.keepAliveTimeout",
//                new Object[]{8L, TimeUnit.SECONDS});
//        Orderer orderer = client.newOrderer(orderName,
//                sampleOrg.getOrdererLocation(orderName), ordererProperties);
//        testChannel.addOrderer(orderer);
//
//
//        //peer join channel
//        for (String peerName : sampleOrg.getPeerNames()) {
//            Properties peerProperties = sampleOrg.getPeerProperties(peerName);
//            peerProperties.put(
//                    "grpc.NettyChannelBuilderOption.maxInboundMessageSize",
//                    9000000);
//            Peer peer = client.newPeer(peerName,
//                    sampleOrg.getPeerLocation(peerName), peerProperties);
//            testChannel.joinPeer(peer);
//        }
//
//        return testChannel.getName();
//    }

}
