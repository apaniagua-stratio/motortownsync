package com.stratio.microservice.motortownsync.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class Producto {

    @Id
    private String cod_interno;
    private String ean;
    private String matricula;



}
