package com.stratio.microservice.motortownsync.repository;


import com.stratio.microservice.motortownsync.entity.CsvRow;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class CsvRowRepository extends SimpleJpaRepository<CsvRow, Long> {
    private EntityManager entityManager;
    public CsvRowRepository(EntityManager entityManager) {
        super(CsvRow.class, entityManager);
        this.entityManager=entityManager;
    }

    @Transactional
    public List<CsvRow> save(List<CsvRow> rows) {
        rows.forEach(row -> entityManager.persist(row));
        //entityManager.flush();
        return rows;
    }

    @Transactional
    public long batchSave(List<CsvRow> rows) {
        int entityCount = 50;
        int batchSize = 25;
        long result=0;



        try {
            //entityTransaction.begin();

            for (int i = 0; i < rows.size(); i++) {

                entityManager.persist(rows.get(i));
                result++;
                System.out.println("persited: " + result);
            }

            return result;
            //entityTransaction.commit();
        } catch (RuntimeException e) {
            /*
            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
            */

            throw e;
        } finally {
            entityManager.close();
        }

    }

}