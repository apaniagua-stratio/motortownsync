package com.stratio.microservice.motortownsync.service;

import com.stratio.microservice.motortownsync.repository.PostgresRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Predicates.not;

@Slf4j
@Component
public class ScheduledTask {

    @Autowired
    private PostgresRepository repo;

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

    @Value("${sftpsplitfiles}")
    private String sftpsplitfiles;

    @Value("${sftpdifffiles}")
    private String sftpdifffiles;

    @Scheduled(fixedRateString = "${schedulerRate}")
    public void task()
    {

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


        //TODO: split total  in files on 1000 records

        if (sftpsplitfiles.equalsIgnoreCase("true")) {

            int cont=0;
            List<String> contRows=new ArrayList<String>();
            while (cont < rows.size()) {

                contRows.add(rows.get(cont));
                cont++;

                if (cont % 1000 == 0) {
                    String filenameCont = sftpoutfolder + "magento_csv_products_" + fecha + "_" + cont + ".csv";
                    System.out.println("cont size:" + contRows.size() + "contfile: " + filenameCont);
                    boolean resulcont = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, contRows, filenameCont,sftpoutputformat,addProductsHeader());
                    log.info("AURGI write file: " + filenameCont + " to stfp file: " + resulcont);

                    contRows=new ArrayList<String>();
                }
            }
            //write rest of rows
            String filenameCont = sftpoutfolder + "magento_csv_products_" + fecha + "_END.csv";
            boolean resulcont = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, contRows, filenameCont,sftpoutputformat,addProductsHeader());
            log.info("AURGI write file: " + filenameCont + " to stfp file: " + resulcont);

        }


        //TODO: write just the differences
        String filenameDiff = sftpoutfolder + "magento_csv_products_" + fecha + "_DIFF";
        if (sftpdifffiles.equalsIgnoreCase("true")) {

            boolean resuldiff = false;

            if (!lastFile.isEmpty()) {

                List<String> lastrows = writer.readCsvFileFromSftp(sftpuser, sftphost, sftpkey, sftpoutfolder + lastFile);

                log.info("AURGI: SFTP read rows " + lastrows.size() + " from  previoues file");

                List<String> diffrows = rows.stream()
                        .filter(not(new HashSet<>(lastrows)::contains))
                        .collect(Collectors.toList());

                resuldiff = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, diffrows, filenameDiff,sftpoutputformat,addProductsHeader());
                log.info("AURGI write file: " + filenameDiff + " to stfp file: " + resuldiff);

            }

        }


        //csv stock file
        List<String> stockrows = repo.getStockCsv();
        log.info("AURGI: POSTGRES stock read " + stockrows.size() + " rows.");
        String stockfilename = sftpoutfolder + "magento_csv_stock_" + fecha ;
        resul = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, stockrows, stockfilename,"csv",addStockHeader());
        log.info("AURGI write file: " + filename + " zipper to stfp file: " + resul);


        log.info("AURGI scheduled job end");
    }

    private String addProductsHeader() {
        return "\"sku\",\"store_view_code\",\"attribute_set_code\",\"product_type\",\"categories\",\"product_websites\",\"name\",\"description\",\"short_description\",\"weight\",\"product_online\",\"tax_class_name\",\"visibility\",\"price\",\"special_price\",\"special_price_from_date\",\"special_price_to_date\",\"url_key\",\"meta_title\",\"meta_keywords\",\"meta_description\",\"base_image\",\"base_image_label\",\"small_image\",\"small_image_label\",\"thumbnail_image\",\"thumbnail_image_label\",\"swatch_image\",\"swatch_image_label\",\"created_at\",\"updated_at\",\"new_from_date\",\"new_to_date\",\"display_product_options_in\",\"map_price\",\"msrp_price\",\"map_enabled\",\"gift_message_available\",\"custom_design\",\"custom_design_from\",\"custom_design_to\",\"custom_layout_update\",\"page_layout\",\"product_options_container\",\"msrp_display_actual_price_type\",\"country_of_manufacture\",\"additional_attributes\",\"qty\",\"out_of_stock_qty\",\"use_config_min_qty\",\"is_qty_decimal\",\"allow_backorders\",\"use_config_backorders\",\"min_cart_qty\",\"use_config_min_sale_qty\",\"max_cart_qty\",\"use_config_max_sale_qty\",\"is_in_stock\",\"notify_on_stock_below\",\"use_config_notify_stock_qty\",\"manage_stock\",\"use_config_manage_stock\",\"use_config_qty_increments\",\"qty_increments\",\"use_config_enable_qty_inc\",\"enable_qty_increments\",\"is_decimal_divided\",\"website_id\",\"related_skus\",\"related_position\",\"crosssell_skus\",\"crosssell_position\",\"upsell_skus\",\"upsell_position\",\"additional_images\",\"additional_image_labels\",\"hide_from_product_page\",\"custom_options\",\"bundle_price_type\",\"bundle_sku_type\",\"bundle_price_view\",\"bundle_weight_type\",\"bundle_values\",\"bundle_shipment_type\",\"configurable_variations\",\"configurable_variation_labels\",\"associated_skus\"";
    }

    private String addStockHeader() {
        return "\"source_code\",\"sku\",\"status\",\"availability\",\"availability_order\",\"availability_date\"";
    }

}