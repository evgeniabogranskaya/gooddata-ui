package com.gooddata.cfal.restapi.selftest;

import com.gooddata.c4.C4Client;
import com.gooddata.c4.about.AboutService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Health;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class C4ConnectionCheckTest {

    @Mock
    private C4Client c4Client;
    @Mock
    private AboutService aboutService;
    private C4ConnectionCheck instance;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        doReturn(aboutService).when(c4Client).getAboutService();

        this.instance = new C4ConnectionCheck(c4Client);
    }

    @Test(expected = NullPointerException.class)
    public void testNullConstructor() throws Exception {
        new C4ConnectionCheck(null);
    }

    @Test
    public void testC4ClientIsUsed() throws Exception {
        final Health.Builder builder = new Health.Builder();
        instance.doHealthCheck(builder);

        verify(aboutService).getAbout();
    }
}
