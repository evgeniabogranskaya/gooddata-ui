/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.gooddata.cfal.ConcurrentAuditLogService.DEFAULT_BACKLOG_SIZE;

/**
 * The application writes {@code count} events into the log {@code file} at the rate of {@code rate} per {@code unit}
 * and reports the total time for writing and number of rejected events.<p>
 * Example: Write 1.000 events at rate 100 events per second:
 * <pre>java -cp '*' com.gooddata.cfal.ConcurrentAuditLogServicePerformance -Dcount=1000 -Drate=100 -Dunit=SECONDS</pre>
 */
public class ConcurrentAuditLogServicePerformance {

    public static void main(String... args) throws Exception {
        final int count = Integer.getInteger("count", 10_000);
        final int rate = Integer.getInteger("rate", 1_000);
        final TimeUnit unit = Optional
                .ofNullable(System.getProperty("unit"))
                .map(TimeUnit::valueOf)
                .orElse(TimeUnit.SECONDS);
        final File file = new File(System.getProperty("file", "/tmp/cfal.log"));
        run(count, rate, unit, file);
    }

    private static void run(final int count, final int rate, final TimeUnit unit, final File file) throws Exception {
        final CountingRejectionHandler reject = new CountingRejectionHandler();
        final AuditLogEventFileWriter writer = new AuditLogEventFileWriter(file);
        final ConcurrentAuditLogService log = new ConcurrentAuditLogService("test", writer, DEFAULT_BACKLOG_SIZE, reject);

        final long sleep = unit.toMillis(1) / rate;

        final StopWatch sw = new StopWatch();
        sw.start();

        System.out.printf("START count=%d rate=%d/%s sleep=%d ms file=%s %n", count, rate, unit, sleep, file);

        for (int i = 0; i < count; i++) {
            log.logEvent(new AuditLogEvent("foo", "bear@gooddata.com", "127.0.0.1", "default"));
            if (i % sleep == 0) {
                System.out.printf("queue=%d%n", log.getQueueSize());
            }
            Thread.sleep(sleep);
        }
        log.destroy();

        System.out.printf("FINISHED count=%d reject=%d duration=%s rate=%d/%s sleep=%d ms file=%s size=%d %n",
                count, reject.getCounter(), sw, rate, unit, sleep, file, file.length() / 1024 / 1024);
    }

    private static class CountingRejectionHandler implements RejectionHandler {

        private long counter;

        @Override
        public void handle(final AuditLogEvent event) {
            if (++counter % 100 == 0) {
                System.out.println("rejected " + counter);
            }
        }

        long getCounter() {
            return counter;
        }
    }
}