package com.stratio.microservice.motortownsync.repository;

public class AurgiQuery {

    public AurgiQuery(){};

    public String getQueryProductos(int limit) {

        String s = new StringBuilder()
                .append("SELECT cod_interno,ean,matricula,pvp,titulo_neumatico_matricula,codigo_neumatico_matricula ")
                .append(",link_img_s,link_img_m,link_img_l ")
                .append(",atributo_01,atributo_02,atributo_03,atributo_04,atributo_05,atributo_06,atributo_07,atributo_08, ")
                .append(",atributo_09,atributo_10,atributo_11,atributo_12,atributo_13,atributo_14,atributo_15,atributo_16 ")
                .append("FROM motortown.productos_calc limit " + limit)
                .append(";")
                .toString();

        return s;
    }

}
