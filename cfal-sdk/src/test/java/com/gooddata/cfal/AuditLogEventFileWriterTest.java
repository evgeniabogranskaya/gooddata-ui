/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static com.gooddata.cfal.AuditLogEventFileWriter.createLogFileName;
import static com.gooddata.cfal.AuditLogEventType.STANDARD_LOGIN;
import static java.nio.file.Files.readAllLines;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class AuditLogEventFileWriterTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private AuditLogEvent event;

    @Before
    public void setUp() throws Exception {
        DateTimeUtils.setCurrentMillisFixed(new DateTime(2017, 3, 10, 9, 47, 3, 547, DateTimeZone.UTC).getMillis());
        event = new AuditLogEvent(STANDARD_LOGIN, "user@example.com", "1.2.3.4", "default");
        event.setComponent("foo");
    }

    @After
    public void tearDown() throws Exception {
        DateTimeUtils.currentTimeMillis();
    }

    @Test
    public void shouldCreateFileName() throws Exception {
        final File fileName = createLogFileName(tmp.getRoot(), "foo", "bar");
        assertThat(fileName, is(notNullValue()));
        assertThat(fileName.getAbsolutePath(), containsString("foo"));
        assertThat(fileName.getAbsolutePath(), containsString("bar"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnEmptyComponent() throws Exception {
        createLogFileName(tmp.getRoot(), "");
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailOnNullDir() throws Exception {
        createLogFileName(null, "foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnNonExistingDir() throws Exception {
        createLogFileName(new File(tmp.getRoot(), "nonexistent"), "foo");
    }

    @Test(expected = IOException.class)
    public void shouldFailOnLogFileInNonExistingDirectory() throws Exception {
        final File dir = new File(tmp.getRoot(), "nonexistent");
        new AuditLogEventFileWriter(new File(dir, "foo.log"));
    }

    @Test
    public void shouldAppend() throws Exception {
        final File file = tmp.newFile();

        try (final AuditLogEventFileWriter writer = new AuditLogEventFileWriter(file)) {
            writer.logEvent(event);
        }
        assertThat(readAllLines(file.toPath()), hasSize(1));

        try (final AuditLogEventFileWriter writer = new AuditLogEventFileWriter(file)) {
            writer.logEvent(event);
        }
        assertThat(readAllLines(file.toPath()), hasSize(2));
    }
}