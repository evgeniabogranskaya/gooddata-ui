/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.task;

import static com.gooddata.cfal.restapi.task.CreateIndexTask.LOGIN_INDEX_TIMER_NAME;
import static com.gooddata.cfal.restapi.task.CreateIndexTask.TTL_INDEX_TIMER_NAME;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CreateIndexTaskTest {

    @Mock
    private AuditLogEventRepository repository;
    @Mock
    private MetricRegistry metricRegistry;

    private CreateIndexTask instance;
    private Timer ttlTimer;
    private Timer loginTimer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.instance = new CreateIndexTask(repository, metricRegistry);

        final ArgumentCaptor<Timer> argument = ArgumentCaptor.forClass(Timer.class);

        verify(metricRegistry, times(1)).register(contains(TTL_INDEX_TIMER_NAME), argument.capture());
        ttlTimer = argument.getValue();
        verify(metricRegistry, times(1)).register(contains(LOGIN_INDEX_TIMER_NAME), argument.capture());
        loginTimer = argument.getValue();
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
        assertThat(ttlTimer.getCount(), is(1L));
        assertThat(loginTimer.getCount(), is(0L));
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

        assertThat(loginTimer.getCount(), is(1L));
        assertThat(ttlTimer.getCount(), is(0L));
    }

    @Test
    public void createUserLoginIndexesThrowsException() throws Exception {
        doThrow(new RuntimeException("")).when(repository).createUserLoginIndexes();
        instance.createUserLoginIndexes();

        verify(repository).createUserLoginIndexes();
    }
}
