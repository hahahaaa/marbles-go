package com.own.marbles.config;

import com.own.marbles.config.model.SampleOrg;
import com.own.marbles.config.model.SampleStore;
import com.own.marbles.config.model.SampleUser;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

@Configuration
public class NetWorkConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(NetWorkConfigLoader.class);

    private final HashMap<String, SampleOrg> sampleOrgs = new HashMap<>();

    public HashMap<String, SampleOrg> getSampleOrgs() {
        return sampleOrgs;
    }

    public SampleOrg getSampleOrg(String orgName) {
        return sampleOrgs.get(orgName);
    }

    private Channel channel;

    public Channel getChannel() {
        return channel;
    }

    @Bean
    public NetworkConfig networkConfig() throws Exception {
        NetworkConfig networkConfig = NetworkConfig.fromJsonFile(new File("src/main/fixture/network_configs/network-config.json"));

        TestConfig testConfig = new TestConfig();


        for (NetworkConfig.OrgInfo orgInfo : networkConfig.getOrganizationInfos()) {

            SampleOrg sampleOrg = new SampleOrg(orgInfo.getName(), orgInfo.getMspId());


            List<NetworkConfig.CAInfo> caInfos = orgInfo.getCertificateAuthorities();
            if (caInfos.size() == 0) {
                continue;
            }
            HFCAClient hfcaClient = HFCAClient.createNewInstance(caInfos.get(0));
            //set CAClient
            sampleOrg.setCAClient(hfcaClient);
            //set sampleStore
            SampleStore sampleStore = setSampleStore(sampleOrg);
            //set PeerAdmin
            SampleUser peerAdmin = new SampleUser(orgInfo.getPeerAdmin(), orgInfo, sampleStore);
            sampleOrg.setPeerAdmin(peerAdmin);

            NetworkConfig.UserInfo adminInfo = (NetworkConfig.UserInfo) caInfos.get(0).getRegistrars().toArray()[0];
            SampleUser admin = sampleStore.getMember(adminInfo.getName(), orgInfo.getName());
            if (!admin.isEnrolled()) {  //Preregistered admin only needs to be enrolled with Fabric caClient.
                admin.setEnrollment(hfcaClient.enroll(admin.getName(), adminInfo.getEnrollSecret()));
                admin.setMspId(orgInfo.getMspId());
                admin.setEnrollmentSecret(adminInfo.getEnrollSecret());
            }
            //setAdmin
            sampleOrg.setAdmin(admin);

            HFClient client = HFClient.createNewInstance();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(sampleOrg.getPeerAdmin());
            //set Client
            sampleOrg.setClient(client);

            channel = client.loadChannelFromConfig("mychannel", networkConfig);
            channel.initialize();

            //setOrderProperties
//            networkConfig.getOrdererNames().forEach(ordererName -> {
//                try {
//                    sampleOrg.addOrdererLocation(ordererName, "grpc://localhost:7050");
//                    Properties ordererProperties = networkConfig.getOrdererProperties(ordererName);
//                    Properties testProp = testConfig.getEndPointProperties("orderer", ordererName);
//                    ordererProperties.setProperty("clientCertFile", testProp.getProperty("clientCertFile"));
//                    ordererProperties.setProperty("clientKeyFile", testProp.getProperty("clientKeyFile"));
//                    sampleOrg.setOrdererProperties(ordererName, ordererProperties);
//                } catch (InvalidArgumentException e) {
//                    e.printStackTrace();
//                }
//            });
//
//            //setPeerNamesProperties
//            networkConfig.getPeerNames().forEach(peerName -> {
//                try {
//                    sampleOrg.addPeerLocation(peerName, "grpc://localhost:7051");
//                    Properties peerProperties = networkConfig.getPeerProperties(peerName);
//                    Properties testProp = testConfig.getEndPointProperties("peer", peerName);
//                    peerProperties.setProperty("clientCertFile", testProp.getProperty("clientCertFile"));
//                    peerProperties.setProperty("clientKeyFile", testProp.getProperty("clientKeyFile"));
//                    sampleOrg.setPeerProperties(peerName, peerProperties);
//
//                } catch (InvalidArgumentException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//
//            //setEventHubProperties
//            networkConfig.getEventHubNames().forEach(eventhubName -> {
//                try {
//                    Properties eventHubsProperties = networkConfig.getEventHubsProperties(eventhubName);
//                    Properties testProp = testConfig.getEndPointProperties("peer", eventhubName);
//                    eventHubsProperties.setProperty("clientCertFile", testProp.getProperty("clientCertFile"));
//                    eventHubsProperties.setProperty("clientKeyFile", testProp.getProperty("clientKeyFile"));
//                    sampleOrg.setEventHubsProperties(eventhubName, eventHubsProperties);
//
//                } catch (InvalidArgumentException e) {
//                    throw new RuntimeException(e);
//                }
//            });


            sampleOrgs.put(orgInfo.getName(), sampleOrg);

        }

        return networkConfig;
    }

    public SampleStore setSampleStore(SampleOrg sampleOrg) {
        // Persistence is not part of SDK. Sample file store is for
        // demonstration purposes only!
        // MUST be replaced with more robust application implementation
        // (Database, LDAP)

        File sampleStoreFile = new File("src/main/fixture/HFCSampletest" + sampleOrg.getName() + ".properties");
        if (sampleStoreFile.exists()) { // For testing start fresh
//            sampleStoreFile.delete();
        }

        SampleStore sampleStore = new SampleStore(sampleStoreFile);
        sampleOrg.setSampleStore(sampleStore);

        return sampleStore;
    }


}
