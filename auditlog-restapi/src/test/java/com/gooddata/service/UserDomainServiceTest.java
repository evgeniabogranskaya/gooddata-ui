/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.gooddata.c4.domain.C4Domain;
import com.gooddata.c4.domain.C4DomainNotFoundException;
import com.gooddata.c4.domain.DomainService;
import com.gooddata.c4.user.C4User;
import com.gooddata.c4.user.C4UserNotFoundException;
import com.gooddata.c4.user.UserService;
import com.gooddata.exception.DomainNotFoundException;
import com.gooddata.exception.UserNotDomainAdminException;
import com.gooddata.exception.UserNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserDomainServiceTest {

    private static final String USER_ID = "user";
    private static final String NOT_ADMIN_USER_ID = "not admin";
    private static final String NON_EXISTENT_USER_ID = "non existent user";
    private static final String DOMAIN = "domain";
    private static final String NON_EXISTENT_DOMAIN = "non existent domain";
    private static final String DOMAIN_URI = "/gdc/c4/domain/" + DOMAIN;
    private static final String USER_URI = "/gdc/c4/user/" + USER_ID;

    @Mock
    private UserService userService;

    @Mock
    private DomainService domainService;

    private UserDomainService userDomainService;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        C4User c4User = mock(C4User.class);
        doReturn(c4User).when(userService).getUser(USER_ID);
        doReturn(DOMAIN_URI).when(c4User).getDomainUri();

        C4Domain c4Domain = mock(C4Domain.class);
        doReturn(c4Domain).when(domainService).getDomain(DOMAIN);
        doReturn(USER_URI).when(c4Domain).getOwner();

        doThrow(C4UserNotFoundException.class).when(userService).getUser(NON_EXISTENT_USER_ID);
        doThrow(C4DomainNotFoundException.class).when(domainService).getDomain(NON_EXISTENT_DOMAIN);

        userDomainService = new UserDomainService(userService, domainService);
    }

    @Test
    public void findDomainForUser() {
        String domainForUser = userDomainService.findDomainForUser(USER_ID);

        assertThat(domainForUser, is(DOMAIN));
    }

    @Test
    public void testAuthorizeAdmin() {
        userDomainService.authorizeAdmin(USER_ID, DOMAIN);

        verify(domainService).getDomain(DOMAIN);
    }

    @Test(expected = UserNotDomainAdminException.class)
    public void testAuthorizeAdminDenied() {
        userDomainService.authorizeAdmin(NOT_ADMIN_USER_ID, DOMAIN);
    }

    @Test(expected = UserNotFoundException.class)
    public void testFindDomainForNonExistentUser() {
        userDomainService.findDomainForUser(NON_EXISTENT_USER_ID);
    }

    @Test(expected = DomainNotFoundException.class)
    public void testAuthorizeAdminForNonExistentDomain() {
        userDomainService.authorizeAdmin(USER_ID, NON_EXISTENT_DOMAIN);
    }
}
