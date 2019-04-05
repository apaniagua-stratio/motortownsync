package com.stratio.microservice.motortownsync.repository;

import com.stratio.microservice.motortownsync.entity.Festivo;
import org.springframework.data.repository.Repository;

import com.stratio.microservice.motortownsync.entity.Producto;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostgresRepository extends Repository<Producto,Long> {


    @Query(value = "SELECT cod_interno,ean,matricula FROM motortown.productos;",
            nativeQuery=true
    )
    public List<Producto> getProductos();

}
