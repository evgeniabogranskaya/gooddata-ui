/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.task;

import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class CreateIndexTaskTest {

    @Mock
    private AuditLogEventRepository repository;
    private CreateIndexTask instance;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.instance = new CreateIndexTask(repository);
    }

    @Test(expected = NullPointerException.class)
    public void constuctorWithNull() throws Exception {
        new CreateIndexTask(null);
    }

    @Test
    public void createTtlIndexesGotCalled() throws Exception {
        instance.createTtlIndexes();

        verify(repository).createTtlIndexes();
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
    }

    @Test
    public void createUserLoginIndexesThrowsException() throws Exception {
        doThrow(new RuntimeException("")).when(repository).createUserLoginIndexes();
        instance.createUserLoginIndexes();

        verify(repository).createUserLoginIndexes();
    }
}
