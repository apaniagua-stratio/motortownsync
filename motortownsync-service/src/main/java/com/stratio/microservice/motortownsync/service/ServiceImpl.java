package com.stratio.microservice.motortownsync.service;

import com.stratio.microservice.motortownsync.entity.Producto;
import com.stratio.microservice.motortownsync.repository.PostgresRepository;
import com.stratio.microservice.motortownsync.repository.ProductosRepository;
import com.stratio.microservice.motortownsync.service.model.ServiceInput;
import com.stratio.microservice.motortownsync.service.model.ServiceOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Predicates.not;

@Slf4j
@Service
public class ServiceImpl implements com.stratio.microservice.motortownsync.service.Service{

  @Autowired
  private PostgresRepository repo;

  @Autowired
  private ProductosRepository prodrepo;

  @Value("${sftphost}")
  private String sftphost;

  @Value("${sftpuser}")
  private String sftpuser;

  @Value("${sftpkey}")
  private String sftpkey;

  @Value("${sftpoutfolder}")
  private String sftpoutfolder;

  @Value("${sftpoutputformat}")
  private String sftpoutputformat;


  @Override
  public ServiceOutput doSomething(ServiceInput input) {

    ServiceOutput out = new ServiceOutput("{\"exampleOutputField\":\"duno man\"}");


    return out;

  }

  @Override
  public ServiceOutput writeProductsToSftp(ServiceInput input) {


    //SftpWriter writer = new SftpWriter();
    //boolean resul = writer.writeCsvFileToSftp(sftpuser,sftphost,sftpkey,csvlines,remotefile);


    List<String> rows = repo.getProductoCsv();

    SftpWriter writer = new SftpWriter();

    Date date = new Date();
    Timestamp ts=new Timestamp(date.getTime());
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String fecha= formatter.format(ts);


    String filename = sftpoutfolder + "magento_csv_products_" + fecha + ".csv";

    boolean resul = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, rows, filename,"csv",addProductsHeader());

    log.info("AURGI write file: " + filename + " to stfp file: " + resul);

    return new ServiceOutput("file " + filename +  " written: " + resul );



  }

  @Override
  public String writeProductsToSftp() {

    List<String> rows = repo.getProductoCsv();
    log.info("AURGI: POSTGRES read " + rows.size() + " rows.");

    SftpWriter writer = new SftpWriter();

    Date date = new Date();
    Timestamp ts = new Timestamp(date.getTime());
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String fecha = formatter.format(ts);

    String lastFile = writer.getLastFilenameFromSftp(sftpuser, sftphost, sftpkey, sftpoutfolder);
    String filename = sftpoutfolder + "magento_csv_products_" + fecha ;



    //TODO: write total file
    boolean resul = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, rows, filename,sftpoutputformat,addProductsHeader());
    log.info("AURGI write file: " + filename + " " + sftpoutputformat + " to stfp file: " + resul);

    /*
    List<String> rows = repo.getProductoCsv();

    SftpWriter writer = new SftpWriter();

    Date date = new Date();
    Timestamp ts=new Timestamp(date.getTime());
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String fecha= formatter.format(ts);


    String filename = sftpoutfolder + "magento_csv_products_" + fecha + ".csv";

    boolean resul = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, rows, filename,"csv",addProductsHeader());

    log.info("AURGI write file: " + filename + " to stfp file: " + resul);


     */
    return "AURGI SFTP File " + filename +  " written: " + resul ;

  }

  @Override
  public String writeProductsDiffToSftp() {

    List<String> rows = repo.getProductoCsv();
    log.info("AURGI: POSTGRES read " + rows.size() + " rows.");

    SftpWriter writer = new SftpWriter();

    Date date = new Date();
    Timestamp ts=new Timestamp(date.getTime());
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String fecha= formatter.format(ts);

    String lastFile = writer.getLastFilenameFromSftp(sftpuser, sftphost, sftpkey,sftpoutfolder);
    String filename = sftpoutfolder + "magento_csv_products_" + fecha + ".csv";
    String filenameDiff = sftpoutfolder + "magento_csv_products_" + fecha + "_DIFF.csv";

    boolean resul = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, rows, filename,"csv",addProductsHeader());
    log.info("AURGI write file: " + filename + " to stfp file: " + resul);

    //TODO: use replace all if more dependencies are needed
    boolean resuldiff=false;

    if (!lastFile.isEmpty()) {

      List<String> lastrows = writer.readCsvFileFromSftp(sftpuser, sftphost, sftpkey,sftpoutfolder + lastFile);

      log.info("AURGI: SFTP read rows " + lastrows.size() + " from  previoues file" );

      List<String> diffrows = rows.stream()
            .filter(not(new HashSet<>(lastrows)::contains))
            .collect(Collectors.toList());

      resuldiff= writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, diffrows, filenameDiff,"csv",addProductsHeader());
      log.info("AURGI write file: " + filenameDiff + " to stfp file: " + resuldiff);
    }


    return "AURGI SFTP File " + filenameDiff + " written: " + resuldiff ;

  }


  private String addProductsHeader() {
    return "\"sku\",\"store_view_code\",\"attribute_set_code\",\"product_type\",\"categories\",\"product_websites\",\"name\",\"description\",\"short_description\",\"weight\",\"product_online\",\"tax_class_name\",\"visibility\",\"price\",\"special_price\",\"special_price_from_date\",\"special_price_to_date\",\"url_key\",\"meta_title\",\"meta_keywords\",\"meta_description\",\"base_image\",\"base_image_label\",\"small_image\",\"small_image_label\",\"thumbnail_image\",\"thumbnail_image_label\",\"swatch_image\",\"swatch_image_label\",\"created_at\",\"updated_at\",\"new_from_date\",\"new_to_date\",\"display_product_options_in\",\"map_price\",\"msrp_price\",\"map_enabled\",\"gift_message_available\",\"custom_design\",\"custom_design_from\",\"custom_design_to\",\"custom_layout_update\",\"page_layout\",\"product_options_container\",\"msrp_display_actual_price_type\",\"country_of_manufacture\",\"additional_attributes\",\"qty\",\"out_of_stock_qty\",\"use_config_min_qty\",\"is_qty_decimal\",\"allow_backorders\",\"use_config_backorders\",\"min_cart_qty\",\"use_config_min_sale_qty\",\"max_cart_qty\",\"use_config_max_sale_qty\",\"is_in_stock\",\"notify_on_stock_below\",\"use_config_notify_stock_qty\",\"manage_stock\",\"use_config_manage_stock\",\"use_config_qty_increments\",\"qty_increments\",\"use_config_enable_qty_inc\",\"enable_qty_increments\",\"is_decimal_divided\",\"website_id\",\"related_skus\",\"related_position\",\"crosssell_skus\",\"crosssell_position\",\"upsell_skus\",\"upsell_position\",\"additional_images\",\"additional_image_labels\",\"hide_from_product_page\",\"custom_options\",\"bundle_price_type\",\"bundle_sku_type\",\"bundle_price_view\",\"bundle_weight_type\",\"bundle_values\",\"bundle_shipment_type\",\"configurable_variations\",\"configurable_variation_labels\",\"associated_skus\"";
  }

  public int numberOfProductos() {
    return repo.getProductos().size();
  }

  public List<Producto> getProductos() {
    return repo.getProductos();
  }

  public List<String> getProductosCsv() {
    return repo.getProductoCsv();
  }



/*
  @Async
public CompletableFuture<String> async_getFromMagentoAPI(String id)
{
  RestTemplate restTemplate = new RestTemplate();
  String fooResourceUrl
          = "https://stratio-pre.motortown.es/rest/V1/products/" + id;

  HttpHeaders headers = new HttpHeaders();
  headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
  headers.setContentType(MediaType.APPLICATION_JSON);
  headers.set("Authorization", "Bearer s3tmbhjmcnh5t209yqa97unzalg4akpu");

  HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

  ResponseEntity<String> respEntity = restTemplate.exchange(fooResourceUrl, HttpMethod.GET, entity, String.class);

  String resp = respEntity.getBody();

  return CompletableFuture.completedFuture(resp);
}


  public String getFromMagentoAPI(String id)
  {
    RestTemplate restTemplate = new RestTemplate();
    String fooResourceUrl
            = "https://stratio-pre.motortown.es/rest/V1/products/" + id;

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer s3tmbhjmcnh5t209yqa97unzalg4akpu");

    HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

    ResponseEntity<String> respEntity = restTemplate.exchange(fooResourceUrl, HttpMethod.GET, entity, String.class);

    String resp = respEntity.getBody();



    return resp;
  }

  @Async
  public CompletableFuture<String> async_postToMagentoAPI(Producto prod)
  {
    RestTemplate restTemplate = new RestTemplate();
    String fooResourceUrl
            = "https://stratio-pre.motortown.es/rest/V1/products/";

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer s3tmbhjmcnh5t209yqa97unzalg4akpu");

    String body=this.getJSONProducto(prod);

    HttpEntity<String> entity = new HttpEntity<String>(body, headers);

    ResponseEntity<String> respEntity = restTemplate.exchange(fooResourceUrl, HttpMethod.POST, entity, String.class);

    String resp = respEntity.getBody();

   // log.info("AURGI POST RESULTADO: " + resp);

    return CompletableFuture.completedFuture(resp);
  }

  public String postToMagentoAPI(Producto prod)
  {
    RestTemplate restTemplate = new RestTemplate();
    String fooResourceUrl
            = "https://stratio-pre.motortown.es/rest/V1/products/";

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer s3tmbhjmcnh5t209yqa97unzalg4akpu");

    String body=this.getJSONProducto(prod);

    HttpEntity<String> entity = new HttpEntity<String>(body, headers);

    ResponseEntity<String> respEntity = restTemplate.exchange(fooResourceUrl, HttpMethod.POST, entity, String.class);

    String resp = respEntity.getBody();

   // log.info("AURGI POST RESULTADO: " + resp);

    return resp;
  }

  public String getJSONProducto()
  {

    String s = new StringBuilder()
            .append("{ \"product\":")
            .append("{")
            .append("\"sku\": \"<SKU>\",")
            .append("\"name\": \"<DESCRIPCION>\",")
            .append("\"attribute_set_id\": 4,")
            .append("\"price\": <PRECIO>,")
            .append("\"status\": 1,")
            .append("\"visibility\": 4,")
            .append("\"type_id\": \"virtual\",")
            .append("\"created_at\": \"2019-04-09 18:23:23\",")
            .append("\"updated_at\": \"2019-04-09 18:23:23\",")
            .append("\"weight\": 0,")
            .append("\"options\": [],")
            .append("\"tierPrices\": [],")
            .append("\"customAttributes\": [")
            .append("{ \"attribute_code\": \"description\", \"value\": \"<DESCRIPCION>\" },")
            .append("{ \"attribute_code\": \"image\", \"value\": \"<IMAGEN>\" },")
            .append("{ \"attribute_code\": \"url_key\", \"value\": \"<URLKEY>\" },")
            .append("{ \"attribute_code\": \"gift_message_available\", \"value\": \"2\" },")
            .append("{ \"attribute_code\": \"tyre_group\", \"value\": \"37\" },")
            .append("{ \"attribute_code\": \"short_description\", \"value\": \"<DESCRIPCIONCORTA>\" },")
            .append("{ \"attribute_code\": \"small_image\", \"value\": \"<IMAGENSMALL>\" },")
            .append("{ \"attribute_code\": \"meta_title\", \"value\": \"<METATITULO>\" },")
            .append("{ \"attribute_code\": \"options_container\", \"value\": \"container2\" },")
            .append("{ \"attribute_code\": \"vehicle_type\", \"value\": \"74\" },")
            .append("{ \"attribute_code\": \"thumbnail\", \"value\": \"<IMAGENSMALL>\" },")
            .append("{ \"attribute_code\": \"meta_keyword\", \"value\": \"<METAKEYWORD>\" },")
            .append("{ \"attribute_code\": \"speed_index\", \"value\": \"83\" },")
            .append("{ \"attribute_code\": \"swatch_image\", \"value\": \"<IMAGENSWATCH>\" },")
            .append("{ \"attribute_code\": \"meta_description\", \"value\": \"<METADESCRIPCION> \" },")

            .append("{ \"attribute_code\": \"width\", \"value\": \"<ANCHO>\" },")
            .append("{ \"attribute_code\": \"height\", \"value\": \"<ALTO>\" },")
            .append("{ \"attribute_code\": \"diameter\", \"value\": \"<DIAMETRO>\" },")
            //..
            .append("{ \"attribute_code\": \"tyre_model\", \"value\": \"<MODELO>\" },")
            //...
            .append("{ \"attribute_code\": \"matricula\", \"value\": \"<MATRICULA>\" },")
            //...
            .append("{ \"attribute_code\": \"homologation\", \"value\": \"<HOMOLOGACION>\" },")
            .append("{ \"attribute_code\": \"required_options\", \"value\": \"1\" },")
            .append("{ \"attribute_code\": \"tyre_store\", \"value\": \"41\" },")
            .append("{ \"attribute_code\": \"has_options\", \"value\": \"1\" },")
            //...
            .append("{ \"attribute_code\": \"ean\", \"value\": \"<EAN>\" },")
            //..
            .append("{ \"attribute_code\": \"color\", \"value\": \"9\" }")
            .append("]")
            .append("},")
            .append("\"saveOptions\": true")
            .append("}")
            .toString();

    return s;
    //jsonObject .put("names", names);

  }


  public String getJSONProducto(Producto prod)
  {

    String s = new StringBuilder()
            .append("{ \"product\":")
            .append("{")
            .append("\"sku\": \"<SKU>\",")
            .append("\"name\": \"<DESCRIPCION>\",")
            .append("\"attribute_set_id\": 4,")
            .append("\"price\": <PRECIO>,")
            .append("\"status\": 1,")
            .append("\"visibility\": 4,")
            .append("\"type_id\": \"virtual\",")
            .append("\"created_at\": \"2019-04-09 18:23:23\",")
            .append("\"updated_at\": \"2019-04-09 18:23:23\",")
            .append("\"weight\": 0,")
            .append("\"options\": [],")
            .append("\"tierPrices\": [],")
            .append("\"customAttributes\": [")
            .append("{ \"attribute_code\": \"description\", \"value\": \"<DESCRIPCION>\" },")
            .append("{ \"attribute_code\": \"image\", \"value\": \"<IMAGEN>\" },")
            .append("{ \"attribute_code\": \"url_key\", \"value\": \"<URLKEY>\" },")
            .append("{ \"attribute_code\": \"gift_message_available\", \"value\": \"2\" },")
            .append("{ \"attribute_code\": \"tyre_group\", \"value\": \"37\" },")
            .append("{ \"attribute_code\": \"short_description\", \"value\": \"<DESCRIPCIONCORTA>\" },")
            .append("{ \"attribute_code\": \"small_image\", \"value\": \"<IMAGENSMALL>\" },")
            .append("{ \"attribute_code\": \"meta_title\", \"value\": \"<METATITULO>\" },")
            .append("{ \"attribute_code\": \"options_container\", \"value\": \"container2\" },")
            .append("{ \"attribute_code\": \"vehicle_type\", \"value\": \"74\" },")
            .append("{ \"attribute_code\": \"thumbnail\", \"value\": \"<IMAGENSMALL>\" },")
            .append("{ \"attribute_code\": \"meta_keyword\", \"value\": \"<METAKEYWORD>\" },")
            .append("{ \"attribute_code\": \"speed_index\", \"value\": \"83\" },")
            .append("{ \"attribute_code\": \"swatch_image\", \"value\": \"<IMAGENSWATCH>\" },")
            .append("{ \"attribute_code\": \"meta_description\", \"value\": \"<METADESCRIPCION> \" },")
            .append("{ \"attribute_code\": \"width\", \"value\": \"<ANCHO>\" },")
            .append("{ \"attribute_code\": \"height\", \"value\": \"<ALTO>\" },")
            .append("{ \"attribute_code\": \"diameter\", \"value\": \"<DIAMETRO>\" },")
            .append("{ \"attribute_code\": \"tyre_model\", \"value\": \"<MODELO>\" },")
            .append("{ \"attribute_code\": \"matricula\", \"value\": \"<MATRICULA>\" },")
            .append("{ \"attribute_code\": \"homologation\", \"value\": \"<HOMOLOGACION>\" },")
            .append("{ \"attribute_code\": \"required_options\", \"value\": \"1\" },")
            .append("{ \"attribute_code\": \"tyre_store\", \"value\": \"41\" },")
            .append("{ \"attribute_code\": \"has_options\", \"value\": \"1\" },")
            .append("{ \"attribute_code\": \"ean\", \"value\": \"<EAN>\" },")
            .append("{ \"attribute_code\": \"color\", \"value\": \"9\" }")
            .append("]")
            .append("},")
            .append("\"saveOptions\": true")
            .append("}")
            .toString();




    s=s.replaceAll("<SKU>",prod.ean);
    s=s.replaceAll("<DESCRIPCION>",prod.titulo_neumatico_matricula);
    s=s.replaceAll("<PRECIO>",prod.pvp);
    s=s.replaceAll("<IMAGEN>",prod.link_img_l);
    s=s.replaceAll("<URLKEY>",prod.ean);

    s=s.replaceAll("<DESCRIPCIONCORTA>",prod.codigo_neumatico_matricula);
    s=s.replaceAll("<IMAGENSMALL>",prod.link_img_s);
    s=s.replaceAll("<METATITULO>",prod.codigo_neumatico_matricula);
    s=s.replaceAll("<METAKEYWORD>",prod.codigo_neumatico_matricula);
    s=s.replaceAll("<IMAGENSWATCH>",prod.link_img_m);
    s=s.replaceAll("<METADESCRIPCION>",prod.titulo_neumatico_matricula);

    s=s.replaceAll("<ANCHO>",prod.atributo_07);
    s=s.replaceAll("<ALTO>",prod.atributo_08);
    s=s.replaceAll("<DIAMETRO>",prod.atributo_06);
    s=s.replaceAll("<MODELO>",prod.atributo_04);
    s=s.replaceAll("<HOMOLOGACION>",prod.atributo_16);
    s=s.replaceAll("<EAN>",prod.ean);
    s=s.replaceAll("<MATRICULA>",prod.matricula);

    return s;



  }
*/


}
