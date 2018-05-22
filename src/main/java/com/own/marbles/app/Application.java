package com.own.marbles.app;

import com.own.marbles.config.NetWorkConfigLoader;
import com.own.marbles.config.model.SampleOrg;
import com.own.marbles.config.model.SampleStore;
import com.own.marbles.config.model.SampleUser;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.NetworkConfig.UserInfo;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "app")
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Resource
    NetWorkConfigLoader configLoader;


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

//    @GetMapping(value = "channel")
//    public Object channel() throws Exception {
//
//    }

}
