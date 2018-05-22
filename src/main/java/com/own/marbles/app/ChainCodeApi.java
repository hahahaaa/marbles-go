package com.own.marbles.app;

import com.own.marbles.config.NetWorkConfigLoader;
import com.own.marbles.config.model.SampleOrg;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;

@RestController
@RequestMapping(value = "chaincode")
public class ChainCodeApi {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Resource
    NetWorkConfigLoader configLoader;

    @RequestMapping("query")
    public void query() throws Exception {
        SampleOrg sampleOrg = configLoader.getSampleOrg("Org1");

        HFClient client = sampleOrg.getClient();
        client.setUserContext(sampleOrg.getPeerAdmin());


        // 构建proposal
        QueryByChaincodeRequest req = client.newQueryProposalRequest();
        // 指定要调用的chaincode
        ChaincodeID cid = ChaincodeID.newBuilder().setName("fabcar").build();
        req.setChaincodeID(cid);
        req.setFcn("queryAllCars");
        req.setArgs("");
        System.out.println("Querying for " + "");
        Collection<ProposalResponse> resps = configLoader.getChannel().queryByChaincode(req);
        for (ProposalResponse resp : resps) {
            String payload = new String(resp.getChaincodeActionResponsePayload());
            log.info("response: " + payload);
        }

    }
}

