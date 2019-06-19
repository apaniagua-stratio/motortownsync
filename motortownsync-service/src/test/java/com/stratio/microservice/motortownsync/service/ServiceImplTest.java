package com.stratio.microservice.motortownsync.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.assertEquals;

public class ServiceImplTest {

    @Mock
    private ServiceImpl service;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testService() {
        int i = service.getProductos().size();
        assertEquals(i,0);
    }

}
