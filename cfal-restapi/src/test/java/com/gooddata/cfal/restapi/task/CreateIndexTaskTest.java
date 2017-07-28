/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.task;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.gooddata.cfal.restapi.task.CreateIndexTask.LOGIN_INDEX_TIMER_NAME;
import static com.gooddata.cfal.restapi.task.CreateIndexTask.TTL_INDEX_TIMER_NAME;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

public class CreateIndexTaskTest {

    @Mock
    private Timer ttlTimer;
    @Mock
    private Timer loginTimer;
    @Mock
    private AuditLogEventRepository repository;
    @Mock
    private MetricRegistry metricRegistry;

    private CreateIndexTask instance;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        doReturn(ttlTimer).when(metricRegistry).timer(contains(TTL_INDEX_TIMER_NAME));
        doReturn(loginTimer).when(metricRegistry).timer(contains(LOGIN_INDEX_TIMER_NAME));

        this.instance = new CreateIndexTask(repository, metricRegistry);
    }

    @Test(expected = NullPointerException.class)
    public void constuctorWithNull() throws Exception {
        new CreateIndexTask(null, metricRegistry);
    }

    @Test(expected = NullPointerException.class)
    public void constructorWithNullRegistry() throws Exception {
        new CreateIndexTask(repository, null);
    }

    @Test
    public void createTtlIndexesGotCalled() throws Exception {
        instance.createTtlIndexes();

        verify(repository).createTtlIndexes();
        verify(ttlTimer).update(anyLong(), eq(TimeUnit.MILLISECONDS));
        verify(loginTimer, never()).update(anyLong(), any());
    }

    @Test
    public void createTtlIndexesThrowsException() throws Exception {
        doThrow(new RuntimeException("")).when(repository).createTtlIndexes();
        instance.createTtlIndexes();

        verify(repository).createTtlIndexes();
    }

    @Test
    public void createUserLoginIndexesGotCalled() throws Exception {
        instance.createUserLoginIndexes();

        verify(repository).createUserLoginIndexes();
        verify(loginTimer).update(anyLong(), eq(TimeUnit.MILLISECONDS));
        verify(ttlTimer, never()).update(anyLong(), any());
    }

    @Test
    public void createUserLoginIndexesThrowsException() throws Exception {
        doThrow(new RuntimeException("")).when(repository).createUserLoginIndexes();
        instance.createUserLoginIndexes();

        verify(repository).createUserLoginIndexes();
    }
}
