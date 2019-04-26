package com.stratio.microservice.motortownsync.repository;

import com.stratio.microservice.motortownsync.entity.Producto;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProductosRepository  extends CrudRepository<Producto, String> {

    List<Producto> findByean(String ean);

}