package com.own.marbles.app;

import com.own.marbles.config.NetWorkConfigLoader;
import com.own.marbles.config.model.SampleUser;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.NetworkConfig.UserInfo;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.InfoException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.util.Collection;

@RestController
@RequestMapping(value = "app")
public class Application {

    @Resource
    NetworkConfig networkConfig;


    @GetMapping(value = "regist")
    public Object regist() throws Exception {
        NetworkConfig.OrgInfo org = networkConfig.getOrganizationInfo("Org1");
        NetworkConfig.CAInfo caInfo = org.getCertificateAuthorities().get(0);

        HFCAClient hfcaClient = HFCAClient.createNewInstance(caInfo);

        UserInfo admin = org.getPeerAdmin();
        if (admin.getEnrollment() == null) {
            admin.setEnrollment(hfcaClient.enroll(admin.getName(), admin.getEnrollSecret()));
        }
        UserInfo user = new UserInfo(org.getMspId(), "User1", "123");
        RegistrationRequest rr = new RegistrationRequest(user.getName(), "org1.dev.1");
        hfcaClient.register(rr, admin);

//        SampleUser mockuser = new SampleUser(org.getName() + "_mock_" + System.nanoTime(), registrar.getMspId());
//        RegistrationRequest rr = new RegistrationRequest(mockuser.getName(), "org1.department1");
//        mockuser.setEnrollmentSecret(hfcaClient.register(rr, registrar));
//        mockuser.setEnrollment(hfcaClient.enroll(mockuser.getName(), mockuser.getEnrollmentSecret()));

        return hfcaClient.info();
    }


}
