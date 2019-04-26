package com.stratio.microservice.motortownsync.service;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

@Slf4j
public class SftpWriter {

    public List<String> readCsvFileFromSftp(String user,String host, String sftpkey, String remoteFile)
    {

        //2019_04_04_03_30.zip
        int port=22;

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);


            //change before deploy (no passphrase)
            //String privateKey = "/home/apaniagua/.ssh/id_rsa";

            if (sftpkey.equalsIgnoreCase("/home/apaniagua/.ssh/id_rsa")) {
                jsch.addIdentity(sftpkey,"MailSagApm17");
            }
            else {
                jsch.addIdentity(sftpkey);
            }

            session.setConfig("StrictHostKeyChecking", "no");
            log.info("AURGI: Establishing Connection..." + user + "@" + host + " with " + sftpkey);

            session.connect();
            log.info("AURGI: Connection established.");

            log.info("AURGI: Creating SFTP Channel.");
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            InputStream out= null;
            log.info("AURGI: SFTP Getting file " + remoteFile);
            out= sftpChannel.get(remoteFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(out));

            List<String> strings = new ArrayList<String>();

            String line;
            while ((line = br.readLine()) != null)
            {
                strings.add(line);
            }
            br.close();
            sftpChannel.disconnect();
            session.disconnect();
            log.info("AURGI: SFTP channell and session closed");

            return strings;
        }
        catch(JSchException | SftpException | IOException e)
        {
            System.out.println("AURGI: " + e);
        }

        return null;

    }

    public boolean writeCsvFileToSftp(String user, String host, String sftpkey,List<String> csvlines, String remoteFile)
    {

        int port=22;

        //String remoteFile="sample.txt";

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);


            //TODO change before deploy (no passphrase)
            //String privateKey = "/home/apaniagua/.ssh/id_rsa";

            if (sftpkey.equalsIgnoreCase("/home/apaniagua/.ssh/id_rsa")) {
                jsch.addIdentity(sftpkey,"MailSagApm17");
            }
            else {
                jsch.addIdentity(sftpkey);
            }

            session.setConfig("StrictHostKeyChecking", "no");
            log.info("AURGI: SFTP Establishing Connection..." + user + "@" + host + " with " + sftpkey);

            session.connect();
            log.info("AURGI: SFTP Connection established.");

            log.info("AURGI: SFTP Creating channel.");
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            log.info("AURGI: SFTP Channel created.");


            String content = addProductsHeader() + "\n";
            Iterator<String> lisIterator = csvlines.iterator();

            while (lisIterator.hasNext()) {

                content += lisIterator.next() + "\n";
            }

            InputStream stream = new ByteArrayInputStream(content.getBytes ());
            sftpChannel.put (stream, remoteFile);
            log.info("AURGI: SFTP File put.");

            sftpChannel.disconnect();
            session.disconnect();
            log.info("AURGI: SFTP Channel and session closed.");


            //also write differences from last csv written


            return true;
        }
        catch(JSchException | SftpException e)
        {
            log.error("AURGI: " + e);
        }

        return false;

    }

    private String addProductsHeader() {
        return "\"sku\",\"store_view_code\",\"attribute_set_code\",\"product_type\",\"categories\",\"product_websites\",\"name\",\"description\",\"short_description\",\"weight\",\"product_online\",\"tax_class_name\",\"visibility\",\"price\",\"special_price\",\"special_price_from_date\",\"special_price_to_date\",\"url_key\",\"meta_title\",\"meta_keywords\",\"meta_description\",\"base_image\",\"base_image_label\",\"small_image\",\"small_image_label\",\"thumbnail_image\",\"thumbnail_image_label\",\"swatch_image\",\"swatch_image_label\",\"created_at\",\"updated_at\",\"new_from_date\",\"new_to_date\",\"display_product_options_in\",\"map_price\",\"msrp_price\",\"map_enabled\",\"gift_message_available\",\"custom_design\",\"custom_design_from\",\"custom_design_to\",\"custom_layout_update\",\"page_layout\",\"product_options_container\",\"msrp_display_actual_price_type\",\"country_of_manufacture\",\"additional_attributes\",\"qty\",\"out_of_stock_qty\",\"use_config_min_qty\",\"is_qty_decimal\",\"allow_backorders\",\"use_config_backorders\",\"min_cart_qty\",\"use_config_min_sale_qty\",\"max_cart_qty\",\"use_config_max_sale_qty\",\"is_in_stock\",\"notify_on_stock_below\",\"use_config_notify_stock_qty\",\"manage_stock\",\"use_config_manage_stock\",\"use_config_qty_increments\",\"qty_increments\",\"use_config_enable_qty_inc\",\"enable_qty_increments\",\"is_decimal_divided\",\"website_id\",\"related_skus\",\"related_position\",\"crosssell_skus\",\"crosssell_position\",\"upsell_skus\",\"upsell_position\",\"additional_images\",\"additional_image_labels\",\"hide_from_product_page\",\"custom_options\",\"bundle_price_type\",\"bundle_sku_type\",\"bundle_price_view\",\"bundle_weight_type\",\"bundle_values\",\"bundle_shipment_type\",\"configurable_variations\",\"configurable_variation_labels\",\"associated_skus\"";
    }


    public String getLastFilenameFromSftp(String user, String host, String sftpkey, String remotePath)
    {
        int port=22;
        String oldestFile="";
        //String remoteFile="sample.txt";

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);


            //TODO: change before deploy (no passphrase)


            if (sftpkey.equalsIgnoreCase("/home/apaniagua/.ssh/id_rsa")) {
                jsch.addIdentity(sftpkey,"MailSagApm17");
            }
            else {
                jsch.addIdentity(sftpkey);
            }

            session.setConfig("StrictHostKeyChecking", "no");
            log.info("AURGI: SFTP Establishing Connection..." + user + "@" + host + " with " + sftpkey);

            session.connect();
            log.info("AURGI: SFTP Connection established.");

            log.info("AURGI: SFTP Creating channel.");
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            Vector<ChannelSftp.LsEntry> vector = (Vector<ChannelSftp.LsEntry>) sftpChannel.ls(remotePath + "magento_csv_products_????-??-??T??:??:??.csv");
            if (vector.size() > 0) {


                ChannelSftp.LsEntry list = vector.get(0);
                oldestFile =list.getFilename();
                SftpATTRS attrs=list.getAttrs();
                int currentOldestTime =attrs.getMTime();
                String nextName=null;
                ChannelSftp.LsEntry lsEntry=null;
                int nextTime;
                for (Object sftpFile : vector) {
                    lsEntry = (ChannelSftp.LsEntry) sftpFile;
                    nextName = lsEntry.getFilename();
                    attrs = lsEntry.getAttrs();
                    nextTime = attrs.getMTime();
                    if (nextTime > currentOldestTime) {
                        oldestFile = nextName;
                        currentOldestTime = nextTime;
                    }
                }
            }

            sftpChannel.exit();
            session.disconnect();
            log.info("AURGI: SFTP latest file currently is:" + oldestFile);
            log.info("AURGI: SFTP channell and session closed");

        }
        catch(JSchException | SftpException e)
        {
            log.error("AURGI: " + e);
        }

        return oldestFile;
    }


}


