package com.own.marbles.config.model;

import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.util.*;

/*
 *  Copyright 2016, 2017 DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * Sample Organization Representation
 * <p>
 * Keeps track which resources are defined for the Organization it represents.
 */
public class SampleOrg {
    final String name;
    final String mspid;
    HFCAClient caClient;
    HFClient client;

    Map<String, User> userMap = new HashMap<>();
    Map<String, String> peerLocations = new HashMap<>();
    Map<String, String> ordererLocations = new HashMap<>();
    Map<String, String> eventHubLocations = new HashMap<>();
    private SampleUser admin;
    private String caLocation;
    private Properties caProperties = null;

    private SampleUser peerAdmin;


    private String domainName;

    public String getCAName() {
        return caName;
    }

    private String caName;

    SampleStore sampleStore;

    private Map<String, Properties> ordererProperties = new HashMap<>();
    private  Map<String, Properties> peerProperties = new HashMap<>();
    private  Map<String, Properties> eventHubsProperties = new HashMap<>();


    public SampleOrg(String name, String mspid) {
        this.name = name;
        this.mspid = mspid;
    }


    public SampleUser getAdmin() {
        return admin;
    }

    public void setAdmin(SampleUser admin) {
        this.admin = admin;
    }

    public String getMSPID() {
        return mspid;
    }

    public String getCALocation() {
        return this.caLocation;
    }

    public void setCALocation(String caLocation) {
        this.caLocation = caLocation;
    }

    public void addPeerLocation(String name, String location) {

        peerLocations.put(name, location);
    }

    public void addOrdererLocation(String name, String location) {

        ordererLocations.put(name, location);
    }

    public void addEventHubLocation(String name, String location) {

        eventHubLocations.put(name, location);
    }

    public String getPeerLocation(String name) {
        return peerLocations.get(name);

    }

    public String getOrdererLocation(String name) {
        return ordererLocations.get(name);

    }

    public String getEventHubLocation(String name) {
        return eventHubLocations.get(name);

    }

    public Set<String> getPeerNames() {

        return Collections.unmodifiableSet(peerLocations.keySet());
    }


    public Set<String> getOrdererNames() {

        return Collections.unmodifiableSet(ordererLocations.keySet());
    }

    public Set<String> getEventHubNames() {

        return Collections.unmodifiableSet(eventHubLocations.keySet());
    }

    public HFCAClient getCAClient() {

        return caClient;
    }

    public void setPeerProperties(String peerName, Properties peerProperties) {
        this.peerProperties.put(peerName, peerProperties);
    }

    public Properties getPeerProperties(String peerName) {
        return peerProperties.get(peerName);
    }

    public void setCAClient(HFCAClient caClient) {

        this.caClient = caClient;
    }

    public String getName() {
        return name;
    }

    public void addUser(SampleUser user) {
        userMap.put(user.getName(), user);
    }

    public User getUser(String name) {
        return userMap.get(name);
    }

    public void setOrdererProperties(String ordererName, Properties orderProperties) {
        this.ordererProperties.put(ordererName, orderProperties);
    }

    public Properties getOrdererProperties(String ordererName) {
        return ordererProperties.get(ordererName);
    }
    public void setEventHubsProperties(String eventHub, Properties orderProperties) {
        this.ordererProperties.put(eventHub, orderProperties);
    }

    public Properties getEventHubsProperties(String eventHub) {
        return ordererProperties.get(eventHub);
    }


    public Collection<String> getEventHubLocations() {
        return Collections.unmodifiableCollection(eventHubLocations.values());
    }


    public void setCAProperties(Properties caProperties) {
        this.caProperties = caProperties;
    }

    public Properties getCAProperties() {
        return caProperties;
    }


    public SampleUser getPeerAdmin() {
        return peerAdmin;
    }

    public void setPeerAdmin(SampleUser peerAdmin) {
        this.peerAdmin = peerAdmin;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setCAName(String caName) {
        this.caName = caName;
    }

    public HFClient getClient() {
        return client;
    }

    public void setClient(HFClient client) {
        this.client = client;
    }

    public SampleStore getSampleStore() {
        return sampleStore;
    }

    public void setSampleStore(SampleStore sampleStore) {
        this.sampleStore = sampleStore;
    }

}

