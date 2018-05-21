/*
 *  Copyright 2016, 2017 IBM, DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.own.marbles.config;

import java.io.File;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Config allows for a global config of the toolkit. Central location for all
 * toolkit configuration defaults. Has a local config file that can override any
 * property defaults. Config file can be relocated via a system property
 * "org.hyperledger.fabric.sdk.configuration". Any property can be overridden
 * with environment variable and then overridden
 * with a java system property. Property hierarchy goes System property
 * overrides environment variable which overrides config file for default values specified here.
 */

/**
 * Test Configuration
 */

public class TestConfig {


    public Properties getEndPointProperties(final String type, final String name) {
        Properties ret = new Properties();

        final String domainName = getDomainName(name);

        File cert = Paths.get(getTestChannelPath(), "crypto-config/ordererOrganizations".replace("orderer", type), domainName, type + "s",
                name, "tls/server.crt").toFile();
        if (!cert.exists()) {
            throw new RuntimeException(String.format("Missing cert file for: %s. Could not find at location: %s", name,
                    cert.getAbsolutePath()));
        }

        if (!isRunningAgainstFabric10()) {
            File clientCert;
            File clientKey;
            if ("orderer".equals(type)) {
                clientCert = Paths.get(getTestChannelPath(), "crypto-config/ordererOrganizations/example.com/users/Admin@example.com/tls/client.crt").toFile();

                clientKey = Paths.get(getTestChannelPath(), "crypto-config/ordererOrganizations/example.com/users/Admin@example.com/tls/client.key").toFile();
            } else {
                clientCert = Paths.get(getTestChannelPath(), "crypto-config/peerOrganizations/", domainName, "users/User1@" + domainName, "tls/client.crt").toFile();
                clientKey = Paths.get(getTestChannelPath(), "crypto-config/peerOrganizations/", domainName, "users/User1@" + domainName, "tls/client.key").toFile();
            }

            if (!clientCert.exists()) {
                throw new RuntimeException(String.format("Missing  client cert file for: %s. Could not find at location: %s", name,
                        clientCert.getAbsolutePath()));
            }

            if (!clientKey.exists()) {
                throw new RuntimeException(String.format("Missing  client key file for: %s. Could not find at location: %s", name,
                        clientKey.getAbsolutePath()));
            }
            ret.setProperty("clientCertFile", clientCert.getAbsolutePath());
            ret.setProperty("clientKeyFile", clientKey.getAbsolutePath());
        }

        ret.setProperty("pemFile", cert.getAbsolutePath());

        ret.setProperty("hostnameOverride", name);
        ret.setProperty("sslProvider", "openSSL");
        ret.setProperty("negotiationType", "TLS");

        return ret;
    }


    public String getTestChannelPath() {

        return "src/main/fixture/e2e-2Orgs/";

    }

    public boolean isRunningAgainstFabric10() {

        return "IntegrationSuiteV1.java".equals(System.getProperty("org.hyperledger.fabric.sdktest.ITSuite"));

    }

    private String getDomainName(final String name) {
        int dot = name.indexOf(".");
        if (-1 == dot) {
            return null;
        } else {
            return name.substring(dot + 1);
        }

    }

}
