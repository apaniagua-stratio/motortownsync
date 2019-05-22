package com.stratio.microservice.motortownsync.repository;

import com.stratio.microservice.motortownsync.entity.Festivo;
import org.springframework.data.repository.Repository;

import com.stratio.microservice.motortownsync.entity.Producto;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostgresRepository extends Repository<Producto,Long> {


            //"SELECT cod_interno,ean,matricula,pvp,titulo_neumatico_matricula,codigo_neumatico_matricula,link_img_s,link_img_m,link_img_l FROM motortown.productos_calc limit 1;" ;

    @Query(value = "SELECT distinct cod_interno,ean,matricula,pvp,titulo_neumatico_matricula,codigo_neumatico_matricula,link_img_s,link_img_m,link_img_l,atributo_01,atributo_02,atributo_03,atributo_04,atributo_05,atributo_06,atributo_07,atributo_08,atributo_09,atributo_10,atributo_11,atributo_12,atributo_13,atributo_14,atributo_15,atributo_16 FROM motortown.productos_calc limit 3;",
            nativeQuery=true
    )
    public List<Producto> getProductos();


    @Query(value = "SELECT cod_interno,ean,matricula,pvp FROM motortown.productos WHERE ean = ?1 limit 1 ;",
            nativeQuery=true
    )
    public Producto getProducto(String ean);

    @Query(value = "SELECT csv FROM motortown.magentocsvproducts;",
            nativeQuery=true
    )
    public List<String> getProductoCsv();

    @Query(value = "SELECT csv FROM motortown.magentocsvstock;",
            nativeQuery=true
    )
    public List<String> getStockCsv();

}
