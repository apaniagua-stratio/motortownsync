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
        String filenameDiff = sftpoutfolder + "magento_csv_products_" + fecha + "_DIFF";


        //TODO: write total file
        boolean resul = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, rows, filename,sftpoutputformat);
        log.info("AURGI write file: " + filename + " zipper to stfp file: " + resul);



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
                    boolean resulcont = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, contRows, filenameCont,sftpoutputformat);
                    log.info("AURGI write file: " + filenameCont + " to stfp file: " + resulcont);

                    contRows=new ArrayList<String>();
                }
            }
            //write rest of rows
            String filenameCont = sftpoutfolder + "magento_csv_products_" + fecha + "_END.csv";
            boolean resulcont = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, contRows, filenameCont,sftpoutputformat);
            log.info("AURGI write file: " + filenameCont + " to stfp file: " + resulcont);

        }


        //TODO: write just the differences
        boolean resuldiff = false;

        if (!lastFile.isEmpty()) {

            List<String> lastrows = writer.readCsvFileFromSftp(sftpuser, sftphost, sftpkey, sftpoutfolder + lastFile);

            log.info("AURGI: SFTP read rows " + lastrows.size() + " from  previoues file");

            List<String> diffrows = rows.stream()
                    .filter(not(new HashSet<>(lastrows)::contains))
                    .collect(Collectors.toList());

            resuldiff = writer.writeCsvFileToSftp(sftpuser, sftphost, sftpkey, diffrows, filenameDiff,sftpoutputformat);
            log.info("AURGI write file: " + filenameDiff + " to stfp file: " + resuldiff);

        }

        log.info("AURGI scheduled job end");
    }
}