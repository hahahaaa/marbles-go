package com.own.marbles.config;

import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.HFCAInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Properties;

@Component
public class NetWorkConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(NetWorkConfigLoader.class);

    private NetworkConfig networkConfig;

    private static final TestConfig testConfig = new TestConfig();

    @PostConstruct
    public void init() throws Exception {

        networkConfig = NetworkConfig.fromJsonFile(new File("src/main/fixture/network_configs/network-config.json"));
        networkConfig.getOrdererNames().forEach(ordererName -> {
            try {
                Properties ordererProperties = networkConfig.getOrdererProperties(ordererName);
                Properties testProp = testConfig.getEndPointProperties("orderer", ordererName);
                ordererProperties.setProperty("clientCertFile", testProp.getProperty("clientCertFile"));
                ordererProperties.setProperty("clientKeyFile", testProp.getProperty("clientKeyFile"));
                networkConfig.setOrdererProperties(ordererName, ordererProperties);

            } catch (InvalidArgumentException e) {
                throw new RuntimeException(e);
            }
        });

        networkConfig.getPeerNames().forEach(peerName -> {
            try {
                Properties peerProperties = networkConfig.getPeerProperties(peerName);
                Properties testProp = testConfig.getEndPointProperties("peer", peerName);
                peerProperties.setProperty("clientCertFile", testProp.getProperty("clientCertFile"));
                peerProperties.setProperty("clientKeyFile", testProp.getProperty("clientKeyFile"));
                networkConfig.setPeerProperties(peerName, peerProperties);

            } catch (InvalidArgumentException e) {
                throw new RuntimeException(e);
            }
        });

        networkConfig.getEventHubNames().forEach(eventhubName -> {
            try {
                Properties eventHubsProperties = networkConfig.getEventHubsProperties(eventhubName);
                Properties testProp = testConfig.getEndPointProperties("peer", eventhubName);
                eventHubsProperties.setProperty("clientCertFile", testProp.getProperty("clientCertFile"));
                eventHubsProperties.setProperty("clientKeyFile", testProp.getProperty("clientKeyFile"));
                networkConfig.setEventHubProperties(eventhubName, eventHubsProperties);

            } catch (InvalidArgumentException e) {
                throw new RuntimeException(e);
            }
        });

        //Check if we get access to defined CAs!
        NetworkConfig.OrgInfo org = networkConfig.getOrganizationInfo("Org1");
        NetworkConfig.CAInfo caInfo = org.getCertificateAuthorities().get(0);

        HFCAClient hfcaClient = HFCAClient.createNewInstance(caInfo);
        log.info(hfcaClient.getCAName());
        log.info(caInfo.getCAName());

        HFCAInfo info = hfcaClient.info(); //makes actual REST call.
        log.info(caInfo.getCAName());
        log.info(info.getCAName());

//        Collection<NetworkConfig.UserInfo> registrars = caInfo.getRegistrars();
//        assertTrue(!registrars.isEmpty());
//        NetworkConfig.UserInfo registrar = registrars.iterator().next();
//        registrar.setEnrollment(hfcaClient.enroll(registrar.getName(), registrar.getEnrollSecret()));
//        MockUser mockuser = getMockUser(org.getName() + "_mock_" + System.nanoTime(), registrar.getMspId());
//        RegistrationRequest rr = new RegistrationRequest(mockuser.getName(), "org1.department1");
//        mockuser.setEnrollmentSecret(hfcaClient.register(rr, registrar));
//        mockuser.setEnrollment(hfcaClient.enroll(mockuser.getName(), mockuser.getEnrollmentSecret()));
//        orgRegisteredUsers.put(org.getName(), mockuser);
//
//        org = networkConfig.getOrganizationInfo("Org2");
//        caInfo = org.getCertificateAuthorities().get(0);
//
//        hfcaClient = HFCAClient.createNewInstance(caInfo);
//        assertEquals(hfcaClient.getCAName(), caInfo.getCAName());
//        info = hfcaClient.info(); //makes actual REST call.
//        assertEquals(info.getCAName(), "");
//
//        registrars = caInfo.getRegistrars();
//        assertTrue(!registrars.isEmpty());
//        registrar = registrars.iterator().next();
//        registrar.setEnrollment(hfcaClient.enroll(registrar.getName(), registrar.getEnrollSecret()));
//        mockuser = getMockUser(org.getName() + "_mock_" + System.nanoTime(), registrar.getMspId());
//        rr = new RegistrationRequest(mockuser.getName(), "org1.department1");
//        mockuser.setEnrollmentSecret(hfcaClient.register(rr, registrar));
//        mockuser.setEnrollment(hfcaClient.enroll(mockuser.getName(), mockuser.getEnrollmentSecret()));
//        orgRegisteredUsers.put(org.getName(), mockuser);
//
//        deployChaincodeIfRequired();
    }


}
