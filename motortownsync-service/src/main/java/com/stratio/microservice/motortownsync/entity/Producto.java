package com.stratio.microservice.motortownsync.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "productos_calc")
public class Producto {

    @Id
    public String ean;
    public String cod_interno;
    public String matricula;
    public String pvp;
    public String titulo_neumatico_matricula;
    public String codigo_neumatico_matricula;
    public String link_img_s;
    public String link_img_m;
    public String link_img_l;
    public String atributo_01;
    public String atributo_02;
    public String atributo_03;
    public String atributo_04;
    public String atributo_05;
    public String atributo_06;
    public String atributo_07;
    public String atributo_08;
    public String atributo_09;
    public String atributo_10;
    public String atributo_11;
    public String atributo_12;
    public String atributo_13;
    public String atributo_14;
    public String atributo_15;
    public String atributo_16;


    protected Producto() {}

    public Producto(String ean) {
        this.ean = ean;
    }

    @Override
    public String toString() {
        return String.format(
                "Producto[ean=%s, descripcion='%s', precio='%s']",
                ean, titulo_neumatico_matricula, pvp);
    }


}
