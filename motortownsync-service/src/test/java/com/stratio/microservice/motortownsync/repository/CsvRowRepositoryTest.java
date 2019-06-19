package com.stratio.microservice.motortownsync.repository;

import com.stratio.microservice.motortownsync.entity.CsvRow;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class CsvRowRepositoryTest {


    @Mock
    private CsvRowRepository repository;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testRepo() {

        List<CsvRow> rows = new ArrayList<>();

        //int i = repository.save(rows).size();

        verify(repository.save(rows));

        assertEquals(i,0);
    }


}
