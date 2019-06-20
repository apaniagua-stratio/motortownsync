package com.stratio.microservice.motortownsync.service;

import com.stratio.microservice.motortownsync.entity.CsvRow;
import com.stratio.microservice.motortownsync.entity.Producto;
import com.stratio.microservice.motortownsync.repository.CsvRowRepository;
import com.stratio.microservice.motortownsync.repository.PostgresRepository;
import com.stratio.microservice.motortownsync.service.model.ServiceInput;
import com.stratio.microservice.motortownsync.service.model.ServiceOutput;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.File;
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
  private CsvRowRepository csvrowrepo;

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
  @Value("${spartawfpath}")
  private String spartawfpath;
  @Value("${spartawfname}")
  private String spartawfname;
  @Value("${spartawfversion}")
  private int spartawfversion;
  @Value("${spartaretries}")
  private int spartaretries;
  @Value("${ecommerce}")
  private String ECOMMERCE;


  @Override
  public ServiceOutput writeProductsToSftp(ServiceInput input) {


    List<String> rows = repo.getProductoCsv();

    SftpWriter writer = new SftpWriter();

    Date date = new Date();
    Timestamp ts=new Timestamp(date.getTime());
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String fecha= formatter.format(ts);


    String filename = sftpoutfolder + "magento_csv_products_" + fecha + ".csv";

    boolean resul = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, rows, filename,"csv",addProductsHeader(),ECOMMERCE);

    log.info(ECOMMERCE + ": write file: " + filename + " to stfp file: " + resul);

    return new ServiceOutput("file " + filename +  " written: " + resul );


  }

  @Override
  public String writeStockToSftp() {

    List<String> rows = repo.getStockCsv();
    String originalFile = repo.getProductOriginalFile();
    log.info(ECOMMERCE + ": POSTGRES STOCK read " + rows.size() + " rows.");

    SftpWriter writer = new SftpWriter();

    originalFile = StringUtils.substringAfterLast(originalFile, "/");
    originalFile = StringUtils.substringBefore(originalFile, ".");

    File myFile = new File(originalFile);
    String filename = sftpoutfolder + "magento_csv_stock_" + originalFile ;


    //TODO: write total file
    boolean resul = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, rows, filename,"csv",addStockHeader(),ECOMMERCE);
    log.info(ECOMMERCE + ": write file: " + filename + " " + sftpoutputformat + " to stfp file: " + resul);

    return filename +  " : " + resul ;

  }

  @Override
  public String writeProductsToSftp() {

    List<String> rows = repo.getProductoCsv();
    String originalFile = repo.getProductOriginalFile();
    log.info(ECOMMERCE + ": POSTGRES read " + rows.size() + " rows.");

    SftpWriter writer = new SftpWriter();

    originalFile = StringUtils.substringAfterLast(originalFile, "/");
    originalFile = StringUtils.substringBefore(originalFile, ".");

    File myFile = new File(originalFile);
    String filename = sftpoutfolder + "magento_csv_products_" + originalFile ;


    //TODO: write total file
    boolean resul = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, rows, filename,sftpoutputformat,addProductsHeader(),ECOMMERCE);
    log.info(ECOMMERCE + ": SFTP write file: " + filename + " " + sftpoutputformat + " to stfp file: " + resul);

    return filename +  " : " + resul ;

  }

  @Override
  public ServiceOutput reprocess(ServiceInput input) {

    SftpWriter writer = new SftpWriter();

    List<CsvRow> rows;
    rows = writer.rowsFromSftpZip(sftpuser,sftphost,sftpkey,input.getSftpFile(),ECOMMERCE);

    log.info(ECOMMERCE + ": POSTGRES:  start writing to PG this number of entities" + rows.size());
    csvrowrepo.deleteAllInBatch();
    csvrowrepo.flush();
    csvrowrepo.save(rows);
    log.info(ECOMMERCE + ": POSTGRES:  " + rows.size() +  " csv rows written in PG table. ");

    int currentTry=1;

    String wfResult="";
    while (currentTry <= spartaretries && ! wfResult.equalsIgnoreCase("Finished")) {

      log.info(ECOMMERCE + ": SPARTA: running " + spartawfname + " v" + spartawfversion + " execution number " + currentTry);
      wfResult = runWorkflow(spartawfpath,spartawfname,spartawfversion);
      log.info(ECOMMERCE + ": SPARTA: " + spartawfname + " v" + spartawfversion + " execution number " + currentTry +  " finished with state " + wfResult);
      currentTry++;
    }

    String result = "";
    result += " Products file: " +  writeProductsToSftp();
    result += " Stock file: " +   writeStockToSftp();

    log.info(ECOMMERCE + ": reprocess end with result: " + result);
    return new ServiceOutput(result);

  }

  private String runWorkflow(String wf_path, String wf_name,int wf_version) {

    String sTicket=StratioHttpClient.getDCOSTicket();
    log.info(ECOMMERCE+ ": DCOS ticket: " + sTicket);

    String resul="";
    try {

      Thread.sleep(3000);
      resul=StratioHttpClient.runSpartaWF(sTicket,wf_path,wf_name,wf_version);

    } catch (InterruptedException e) {
      e.printStackTrace();
    }


    return resul;
  }

  @Override
  public String writeProductsDiffToSftp() {

    List<String> rows = repo.getProductoCsv();
    log.info("MOTORTOWN: POSTGRES read " + rows.size() + " rows.");

    SftpWriter writer = new SftpWriter();

    Date date = new Date();
    Timestamp ts=new Timestamp(date.getTime());
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String fecha= formatter.format(ts);

    String lastFile = writer.getLastFilenameFromSftp(sftpuser, sftphost, sftpkey,sftpoutfolder);
    String filename = sftpoutfolder + "magento_csv_products_" + fecha + ".csv";
    String filenameDiff = sftpoutfolder + "magento_csv_products_" + fecha + "_DIFF.csv";

    boolean resul = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, rows, filename,"csv",addProductsHeader(),ECOMMERCE);
    log.info("AURGI write file: " + filename + " to stfp file: " + resul);

    //TODO: use replace all if more dependencies are needed
    boolean resuldiff=false;

    if (!lastFile.isEmpty()) {

      List<String> lastrows = writer.readCsvFileFromSftp(sftpuser, sftphost, sftpkey,sftpoutfolder + lastFile,ECOMMERCE);

      log.info("AURGI: SFTP read rows " + lastrows.size() + " from  previoues file" );

      List<String> diffrows = rows.stream()
            .filter(not(new HashSet<>(lastrows)::contains))
            .collect(Collectors.toList());

      resuldiff= writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, diffrows, filenameDiff,"csv",addProductsHeader(),ECOMMERCE);
      log.info("AURGI write file: " + filenameDiff + " to stfp file: " + resuldiff);
    }


    return "AURGI SFTP File " + filenameDiff + " written: " + resuldiff ;

  }


  private String addProductsHeader() {
    //return "\"sku\",\"store_view_code\",\"attribute_set_code\",\"product_type\",\"categories\",\"product_websites\",\"name\",\"description\",\"short_description\",\"weight\",\"product_online\",\"tax_class_name\",\"visibility\",\"price\",\"special_price\",\"special_price_from_date\",\"special_price_to_date\",\"url_key\",\"meta_title\",\"meta_keywords\",\"meta_description\",\"base_image\",\"base_image_label\",\"small_image\",\"small_image_label\",\"thumbnail_image\",\"thumbnail_image_label\",\"swatch_image\",\"swatch_image_label\",\"created_at\",\"updated_at\",\"new_from_date\",\"new_to_date\",\"display_product_options_in\",\"map_price\",\"msrp_price\",\"map_enabled\",\"gift_message_available\",\"custom_design\",\"custom_design_from\",\"custom_design_to\",\"custom_layout_update\",\"page_layout\",\"product_options_container\",\"msrp_display_actual_price_type\",\"country_of_manufacture\",\"additional_attributes\",\"qty\",\"out_of_stock_qty\",\"use_config_min_qty\",\"is_qty_decimal\",\"allow_backorders\",\"use_config_backorders\",\"min_cart_qty\",\"use_config_min_sale_qty\",\"max_cart_qty\",\"use_config_max_sale_qty\",\"is_in_stock\",\"notify_on_stock_below\",\"use_config_notify_stock_qty\",\"manage_stock\",\"use_config_manage_stock\",\"use_config_qty_increments\",\"qty_increments\",\"use_config_enable_qty_inc\",\"enable_qty_increments\",\"is_decimal_divided\",\"website_id\",\"related_skus\",\"related_position\",\"crosssell_skus\",\"crosssell_position\",\"upsell_skus\",\"upsell_position\",\"additional_images\",\"additional_image_labels\",\"hide_from_product_page\",\"custom_options\",\"bundle_price_type\",\"bundle_sku_type\",\"bundle_price_view\",\"bundle_weight_type\",\"bundle_values\",\"bundle_shipment_type\",\"configurable_variations\",\"configurable_variation_labels\",\"associated_skus\"";
    return "\"sku\",\"store_view_code\",\"attribute_set_code\",\"product_type\",\"categories\",\"product_websites\",\"name\",\"description\",\"short_description\",\"weight\",\"product_online\",\"tax_class_name\",\"visibility\",\"price\",\"special_price\",\"special_price_from_date\",\"special_price_to_date\",\"url_key\",\"meta_title\",\"meta_keywords\",\"meta_description\",\"base_image\",\"base_image_label\",\"small_image\",\"small_image_label\",\"thumbnail_image\",\"thumbnail_image_label\",\"swatch_image\",\"swatch_image_label\",\"created_at\",\"updated_at\",\"new_from_date\",\"new_to_date\",\"display_product_options_in\",\"map_price\",\"msrp_price\",\"map_enabled\",\"gift_message_available\",\"custom_design\",\"custom_design_from\",\"custom_design_to\",\"custom_layout_update\",\"page_layout\",\"product_options_container\",\"msrp_display_actual_price_type\",\"country_of_manufacture\",\"additional_attributes\",\"is_matricula\",\"runflat\",\"speed_index\",\"diameter\",\"height\",\"width\",\"activoexterno\",\"ean\",\"homologation\",\"load_index\",\"manufacturer\",\"matricula\",\"season\",\"tyre_model\",\"vehicle_type\",\"availability\",\"availability_order\",\"availability_matricula\",\"availability_order_matricula\",\"availability_date\",\"alphabetical_order\",\"ecotasa\",\"qty\",\"out_of_stock_qty\",\"use_config_min_qty\",\"is_qty_decimal\",\"allow_backorders\",\"use_config_backorders\",\"min_cart_qty\",\"use_config_min_sale_qty\",\"max_cart_qty\",\"use_config_max_sale_qty\",\"is_in_stock\",\"notify_on_stock_below\",\"use_config_notify_stock_qty\",\"manage_stock\",\"use_config_manage_stock\",\"use_config_qty_increments\",\"qty_increments\",\"use_config_enable_qty_inc\",\"enable_qty_increments\",\"is_decimal_divided\",\"website_id\",\"related_skus\",\"related_position\",\"crosssell_skus\",\"crosssell_position\",\"upsell_skus\",\"upsell_position\",\"additional_images\",\"additional_image_labels\",\"hide_from_product_page\",\"custom_options\",\"bundle_price_type\",\"bundle_sku_type\",\"bundle_price_view\",\"bundle_weight_type\",\"bundle_values\",\"bundle_shipment_type\",\"configurable_variations\",\"configurable_variation_labels\",\"associated_skus\"";

  }

  private String addStockHeader() {
    return "\"source_code\",\"sku\",\"status\",\"availability\",\"availability_order\",\"availability_date\"";
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

  public String getSome() {
    return "some";
  }

}
