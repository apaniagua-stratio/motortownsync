package com.stratio.microservice.motortownsync.controller;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;

public class ControllerTest {

    private final static String URL_RESOURCE = "/productscsvfile";

    @InjectMocks
    private Controller controller;


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testController() {
        String result=controller.home();
        assertEquals(result,"motortown_sync");
    }



}
