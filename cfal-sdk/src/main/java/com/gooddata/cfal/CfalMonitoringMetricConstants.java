/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import static com.codahale.metrics.MetricRegistry.name;

public final class CfalMonitoringMetricConstants {

    public static final String QUEUE_SIZE = name("cfal", "queue", "size");
    public static final String QUEUE_REJECTED_COUNT = name("cfal", "queue", "rejected", "count");
    public static final String WRITE_ERROR_COUNT = name("cfal", "write", "error", "count");
    public static final String ROTATE_ERROR_COUNT = name("cfal", "rotate", "error", "count");
    public static final String LOG_CALL_COUNT = name("cfal", "log", "call", "count");
}
