package com.own.marbles.config;

import com.own.marbles.config.model.SampleOrg;
import com.own.marbles.config.model.SampleStore;
import com.own.marbles.config.model.SampleUser;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.NetworkConfigurationException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

import static java.lang.String.format;

@Configuration
public class NetWorkConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(NetWorkConfigLoader.class);

    private final HashMap<String, SampleOrg> sampleOrgs = new HashMap<>();


    @Bean
    public NetworkConfig networkConfig() throws Exception {
        TestConfig testConfig = new TestConfig();
        NetworkConfig networkConfig = NetworkConfig.fromJsonFile(new File("src/main/fixture/network_configs/network-config.json"));
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
        for (NetworkConfig.OrgInfo orgInfo : networkConfig.getOrganizationInfos()) {
            SampleOrg sampleOrg = new SampleOrg(orgInfo.getName(), orgInfo.getMspId());

            HFClient client = HFClient.createNewInstance();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            sampleOrg.setClient(client);

            HFCAClient hfcaClient = HFCAClient.createNewInstance(orgInfo.getCertificateAuthorities().get(0));
            sampleOrg.setCAClient(hfcaClient);

            SampleStore sampleStore = setSampleStore(sampleOrg);

            SampleUser peerOrgAdmin = sampleStore.getMember(
                    orgInfo.getName() + "Admin",
                    orgInfo.getName(),
                    sampleOrg.getMSPID(),
                    findFileSk(Paths.get(pathPrefix, adminKeyPath).toFile()),
                    Paths.get(pathPrefix, adminCertPath,
                            format("/Admin@%s-cert.pem", sampleOrgDomainName))
                            .toFile());

            sampleOrg.setPeerAdmin(peerOrgAdmin);

            //TODO:  SampleUser admin 和 orgInfo.getPeerAdmin()的区别
            SampleUser admin = sampleStore.getMember("admin", "org1");
            if (!admin.isEnrolled()) {  //Preregistered admin only needs to be enrolled with Fabric caClient.
                admin.setEnrollment(hfcaClient.enroll(admin.getName(), "adminpw"));
                admin.setMspId(orgInfo.getMspId());
            }
            sampleOrg.setAdmin(admin);

            sampleOrgs.put(orgInfo.getName(), sampleOrg);

        }

        return networkConfig;
    }

    public SampleStore setSampleStore(SampleOrg sampleOrg) {
        // Persistence is not part of SDK. Sample file store is for
        // demonstration purposes only!
        // MUST be replaced with more robust application implementation
        // (Database, LDAP)

        File sampleStoreFile = new File(System.getProperty("java.io.tmpdir")
                + "/HFCSampletest" + sampleOrg.getName() + ".properties");
        if (sampleStoreFile.exists()) { // For testing start fresh
            //	sampleStoreFile.delete();
        }

        SampleStore sampleStore = new SampleStore(sampleStoreFile);
        sampleOrg.setSampleStore(sampleStore);

        return sampleStore;
    }

    private static File findFileSk(File directory) {

        File[] matches = directory.listFiles((dir, name) -> name
                .endsWith("_sk"));

        if (null == matches) {
            throw new RuntimeException(format(
                    "Matches returned null does %s directory exist?", directory
                            .getAbsoluteFile().getName()));
        }

        if (matches.length != 1) {
            throw new RuntimeException(format(
                    "Expected in %s only 1 sk file but found %d", directory
                            .getAbsoluteFile().getName(), matches.length));
        }

        return matches[0];

    }



}
