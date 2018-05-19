package com.own.marbles.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.own.marbles.config.model.SampleOrg;
import com.own.marbles.config.model.SampleStore;
import com.own.marbles.config.model.SampleUser;
import org.apache.commons.io.IOUtils;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Component
public class NetworkConfig {
    private static final Logger log = LoggerFactory.getLogger(NetworkConfig.class);
    public final String TEST_ADMIN_NAME = "admin";
    public final String TEST_ADMIN_PW = "adminpw";

    public int GOSSIPWAITTIME = 5000;
    public int INVOKEWAITTIME = 100000;
    public int DEPLOYWAITTIME = 120000;
    public int PROPOSALWAITTIME = 120000;

    private String configPath = "classpath:config/marbles_local.json";

    //marbles_local.json
    private JSONObject config;

    //网络配置json network_config.json
    private JSONObject networkConfig;

    private final HashMap<String, SampleOrg> sampleOrgs = new HashMap<>();


    @PostConstruct
    private void initialConfig() throws Exception {
        config = JSON.parseObject(IOUtils.toString(new FileInputStream(ResourceUtils.getFile(configPath)), "utf8"));
        String cpPath = "classpath:config/" + config.getString("network_config");
        networkConfig = JSON.parseObject(IOUtils.toString(new FileInputStream(ResourceUtils.getFile(cpPath)), "utf8"));

        networkConfigHandler();
    }

    private void networkConfigHandler() throws Exception {
        String ordererUrl = "", ordererTlsCa = "", ordererDomainName = "";

        Iterator itr = networkConfig.keySet().iterator();
        while (itr.hasNext()) {
            String orgKey = (String) itr.next();
            if (orgKey.indexOf("orderer") == 0) {
                JSONObject ordererObject = (JSONObject) networkConfig
                        .get(orgKey);
                ordererUrl = (String) ordererObject.get("url");
                ordererTlsCa = (String) ordererObject.get("tls_cacerts");
                ordererDomainName = (String) ordererObject
                        .get("server-hostname");
            }

            if (orgKey.indexOf("org") == 0) {
                JSONObject orgObject = (JSONObject) networkConfig.get(orgKey);
                JSONObject adminObject = (JSONObject) orgObject
                        .get("admin");
                String adminKeyPath = (String) adminObject.get("key");
                String adminCertPath = (String) adminObject.get("cert");

                String orgName = (String) orgObject.get("name");
                String orgMSP = (String) orgObject.get("mspid");
                String caUrl = (String) orgObject.get("ca");
                SampleOrg sampleOrg = new SampleOrg(orgName, orgMSP);
                sampleOrg.addOrdererLocation(ordererDomainName, ordererUrl);

                if (orgObject.containsKey("peers")) {

                    JSONObject peerObject = (JSONObject) orgObject
                            .get("peers");
                    // Iterate over the peer object and get all the peer
                    // defined for each org
                    Iterator peerObjIterator = peerObject.keySet()
                            .iterator();

                    while (peerObjIterator.hasNext()) {
                        String peerKey = (String) peerObjIterator.next();
                        JSONObject peerInfoObject = (JSONObject) peerObject
                                .get(peerKey);
                        String peerUrl = (String) peerInfoObject
                                .get("requests");
                        String peerEventsUrl = (String) peerInfoObject
                                .get("events");
                        String peerDomainName = (String) peerInfoObject
                                .get("server-hostname");
                        String peerTlsCaCerts = (String) peerInfoObject
                                .get("tls_cacerts");

                        sampleOrg.addPeerLocation(peerDomainName, peerUrl);
                        sampleOrg.addEventHubLocation(peerDomainName,
                                peerEventsUrl);
                        sampleOrg.setDomainName(peerDomainName.substring(
                                peerDomainName.indexOf(".") + 1,
                                peerDomainName.length()));
                        sampleOrg.addOrdererLocation(ordererDomainName,
                                ordererUrl);

                        Properties caProperties = setProperties(
                                peerTlsCaCerts, null);
                        sampleOrg.setCAProperties(caProperties);
                        sampleOrg.setCALocation(caUrl);

                        Properties ordererProperties = setProperties(
                                ordererTlsCa, ordererDomainName);
                        sampleOrg.setOrdererProperties(ordererProperties);

                        Properties peerProperties = setProperties(
                                peerTlsCaCerts, peerDomainName);
                        sampleOrg.setPeerProperties(peerDomainName,
                                peerProperties);
                        // add Peer in Set
                        sampleOrgs.put(orgName, sampleOrg);
                    }
                    sampleOrg.setCAClient(HFCAClient.createNewInstance(
                            sampleOrg.getCALocation(),
                            sampleOrg.getCAProperties()));

                    setClient(sampleOrgs.get(orgName));
                    setSampleStore(sampleOrgs.get(orgName));
                    // setOrgAdmin
                    setOrgAdmin(sampleOrgs.get(orgName), adminCertPath,
                            adminKeyPath);
                    setAdminUser(sampleOrgs.get(orgName));
                    setOrgPeers(sampleOrgs.get(orgName));

                }
            }
        }
    }

    private Properties setProperties(String tlsCertPath, String hostNameOverRide) {

        File cf = new File(tlsCertPath);
        if (!cf.exists() || !cf.isFile()) {
            throw new RuntimeException("TEST is missing cert file "
                    + cf.getAbsolutePath());
        }
        Properties properties = new Properties();
        properties.setProperty("pemFile", cf.getAbsolutePath());

        // only for CA there is no option for hostNameOverride.
        if (hostNameOverRide == null) {
            properties.setProperty("allowAllHostNames", "true");//
        } else {
            properties.setProperty("hostnameOverride", hostNameOverRide);
        }
        properties.setProperty("sslProvider", "openSSL");
        properties.setProperty("negotiationType", "TLS");
        return properties;
    }

    public int getTransactionWaitTime() {
        return INVOKEWAITTIME;
    }

    public int getDeployWaitTime() {
        return DEPLOYWAITTIME;
    }

    public int getGossipWaitTime() {
        return GOSSIPWAITTIME;
    }

    public long getProposalWaitTime() {
        return PROPOSALWAITTIME;
    }

    public SampleOrg getSampleOrg(String name) {
        return sampleOrgs.get(name);
    }

    public SampleOrg setSampleOrg(String name, SampleOrg org) {
        return sampleOrgs.put(name, org);

    }

    public void setClient(SampleOrg sampleOrg) throws CryptoException,
            InvalidArgumentException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        sampleOrg.setClient(client);
    }

    public void setSampleStore(SampleOrg sampleOrg) {
        // Persistence is not part of SDK. Sample file store is for
        // demonstration purposes only!
        // MUST be replaced with more robust application implementation
        // (Database, LDAP)

        File sampleStoreFile = new File(System.getProperty("java.io.tmpdir")
                + "/HFCSampletest" + sampleOrg.name + ".properties");
        if (sampleStoreFile.exists()) { // For testing start fresh
            //	sampleStoreFile.delete();
        }

        SampleStore sampleStore = new SampleStore(sampleStoreFile);
        sampleOrg.setSampleStore(sampleStore);
    }

    public void setOrgAdmin(SampleOrg sampleOrg, String adminCertPath,
                            String adminKeyPath) throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidKeySpecException, IOException {

        SampleStore sampleStore = sampleOrg.getSampleStore();
        final String sampleOrgName = sampleOrg.getName();
        final String sampleOrgDomainName = sampleOrg.getDomainName();
        SampleUser peerOrgAdmin = sampleStore.getMember(
                sampleOrgName + "Admin",
                sampleOrgName,
                sampleOrg.getMSPID(),
                findFileSk(Paths.get(adminKeyPath)
                        .toFile()),
                Paths.get(adminCertPath,
                        format("/Admin@%s-cert.pem", sampleOrgDomainName))
                        .toFile());

        sampleOrg.setOrgAdmin(peerOrgAdmin); // A special user that can create
        // channels, join peers and
        // install chaincode

    }

    public SampleUser getOrgAdmin(String orgName) {
        return sampleOrgs.get(orgName).getOrgAdmin();
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

    public void setAdminUser(SampleOrg sampleOrg) throws EnrollmentException,
            org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException, IllegalAccessException, InvocationTargetException, InvalidArgumentException, InstantiationException, NoSuchMethodException, CryptoException, ClassNotFoundException {

        HFCAClient ca = sampleOrg.getCAClient();
        SampleStore sampleStore = sampleOrg.getSampleStore();
        final String orgName = sampleOrg.getName();
        final String mspid = sampleOrg.getMSPID();
        ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        SampleUser admin = sampleStore.getMember(TEST_ADMIN_NAME, orgName);

        if (!admin.isEnrolled()) { // Preregistered admin only needs to be
            // enrolled with Fabric caClient.

            System.out.println("Admin is not enrolled");
            admin.setEnrollment(ca.enroll(admin.getName(), TEST_ADMIN_PW));
            admin.setMspId(mspid);
        }

        sampleOrg.setAdminUser(admin); // The admin of this org --*/
    }

    public SampleUser getAdminUser(String orgName) {
        return sampleOrgs.get(orgName).getAdminUser();
    }

    public Collection<Peer> newPeers(String peers[], SampleOrg sampleOrg)
            throws InvalidArgumentException {
        Collection<Peer> targets = new Vector<>();
        HFClient client = sampleOrg.getClient();
        for (String peerName : peers) {
            Peer peer = client.newPeer(peerName,
                    sampleOrg.getPeerLocation(peerName),
                    sampleOrg.getPeerProperties(peerName));
            targets.add(peer);
        }

        return targets;
    }

    public Collection<Orderer> newOrderer(SampleOrg sampleOrg,
                                          Channel newChannel) throws InvalidArgumentException {
        Collection<Orderer> targets = new Vector<>();
        HFClient client = sampleOrg.getClient();
        for (String ordererName : sampleOrg.getOrdererNames()) {
            Orderer orderer = client.newOrderer(ordererName,
                    sampleOrg.getOrdererLocation(ordererName),
                    sampleOrg.getordererProperties());
            targets.add(orderer);
        }

        return targets;
    }

    public Channel getChannelInstance(Collection<Peer> peers,
                                      String channelName, HFClient client, SampleOrg sampleOrg)
            throws InvalidArgumentException, TransactionException {

        // add orderer, add peer, add eventhub and initialize the channel. for
        // all the cases do a check if they already present or not
        client.setUserContext(sampleOrg.getOrgAdmin());
        Channel newChannel = client.newChannel(channelName);

        // set the orderer and peers in the channel object
        String orderName = sampleOrg.getOrdererNames().iterator().next();
        Properties ordererProperties = sampleOrg.getordererProperties();
        ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime",
                new Object[]{5L, TimeUnit.MINUTES});
        ordererProperties.put(
                "grpc.NettyChannelBuilderOption.keepAliveTimeout",
                new Object[]{8L, TimeUnit.SECONDS});
        Orderer orderer = client.newOrderer(orderName,
                sampleOrg.getOrdererLocation(orderName), ordererProperties);
        if (newChannel.getOrderers().size() == 0) {
            System.out.println("orderer org is not added");
            newChannel.addOrderer(orderer);
        }

        System.out.println("peer length" + peers.size());
        if (newChannel.getPeers().size() == 0) {

            for (Peer peer : peers) {
                newChannel.addPeer(peer);

            }
        }
        for (String event : sampleOrg.getEventHubNames()) {

            newChannel.addEventHub(client.newEventHub(event,
                    sampleOrg.getEventHubLocation(event)));
        }
        if (!newChannel.isInitialized()) {
            newChannel.initialize();
        }
        return newChannel;
    }

    public Collection<SampleOrg> getIntegrationTestsSampleOrgs() {
        return Collections.unmodifiableCollection(sampleOrgs.values());
    }

    public void setOrgPeers(SampleOrg sampleOrg)
            throws InvalidArgumentException {
        System.out.println("organization" + sampleOrg.getName());
        HFClient client = sampleOrg.getClient();
        client.setUserContext(sampleOrg.getOrgAdmin());
        for (String peerName : sampleOrg.getPeerNames()) {
            System.out.println("Peer name in add peer set" + peerName);
            Peer peer = client.newPeer(peerName,
                    sampleOrg.getPeerLocation(peerName),
                    sampleOrg.getPeerProperties(peerName));
            sampleOrg.peers.add(peer);
        }
    }


}