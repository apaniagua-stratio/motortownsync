package com.stratio.microservice.motortownsync.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class Festivo {

    @Id
    private String fecha;
    private String descripcion;

}