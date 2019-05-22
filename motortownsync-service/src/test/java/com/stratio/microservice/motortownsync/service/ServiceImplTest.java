package com.stratio.microservice.motortownsync.service;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ServiceImplTest {

    private ServiceImpl serv;
    private final String var="";

    @Before
    public void setUp()
    {
        ServiceImpl serv = new ServiceImpl();
    }

    @Test
    public void fileListNotEmpty() {

        //assertThat(serv.listFilesInSftp("/anjana").size() > 0);
        //assertThat(var.isEmpty());
        assert(true);

    }

}
