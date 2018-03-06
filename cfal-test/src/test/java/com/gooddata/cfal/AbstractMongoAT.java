/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.gooddata.test.ssh.Authentication;
import com.gooddata.test.ssh.CommandResult;
import com.gooddata.test.ssh.SshClient;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.util.UriTemplate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.fail;

public abstract class AbstractMongoAT extends AbstractAT {

    protected static final String INVALID_COLLECTION = "cfalinvalid";

    private static final int MONGO_PORT = 27017;
    private static final int DEFAULT_EVENT_WAIT_SECONDS = 60;

    private MongoTemplate mongo;
    private MongoClient mongoClient;

    protected SshClient ssh;

    @BeforeClass(groups = SSH_GROUP)
    public void setUpSshAndMongo() throws Exception {
        final Authentication auth = props.getSshAuth();
        ssh = new SshClient(gd.getEndpoint().getHostname(), auth).open();

        final String mongoPass = obtainMongoPass();
        final int mongoPort = ssh.createLocalPortForwarder(MONGO_PORT);
        final String uri = new UriTemplate("mongodb://gdc_root:{pass}@localhost:{port}")
                .expand(mongoPass, mongoPort)
                .toString();
        this.mongoClient = new MongoClient(new MongoClientURI(uri));
        this.mongo = new MongoTemplate(mongoClient, "cfal");
    }

    private String obtainMongoPass() {
        final CommandResult result = ssh.execCmd("sudo cat /etc/gdc/etc/mongo");
        if (result.getExitCode() != 0) {
            throw new IllegalStateException("Unable to obtain mongo password: " + result.toString());
        }
        return result.getStdout();
    }

    @AfterClass(groups = SSH_GROUP)
    public void tearDownMongoAndSsh() throws Exception {
        if (mongoClient != null) {
            mongoClient.close();
        }
        if (ssh != null) {
            ssh.close();
        }
    }

    protected void assertQuery(final Query query, final String collectionName) throws Exception {
        final String testMethodName = getTestMethodName();
        int count = 0;
        while (!mongo.exists(query, collectionName)) {
            if (++count > POLL_LIMIT) {
                fail("Query in collection " + collectionName + " didn't return result: " + query);
            }
            logger.info("{}(): message in collection {} not found, waiting {} seconds",
                    testMethodName, collectionName, POLL_INTERVAL_SECONDS);
            TimeUnit.SECONDS.sleep(POLL_INTERVAL_SECONDS);
        }
        logger.info("{}(): message in collection {} found", testMethodName, collectionName);
    }

    protected void assertNotQuery(final Query query, final String collectionName) throws Exception {
        final String testMethodName = getTestMethodName();
        TimeUnit.SECONDS.sleep(DEFAULT_EVENT_WAIT_SECONDS);
        if (mongo.exists(query, collectionName)) {
            fail("Query in collection " + collectionName + " should not return any result: " + query);
        }
        logger.info("{}(): no message in collection {} found", testMethodName, collectionName);
    }
}
