package com.stratio.microservice.motortownsync.service;

import com.jcraft.jsch.*;
import com.stratio.microservice.motortownsync.entity.CsvRow;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
public class SftpWriter {

    public List<String> readCsvFileFromSftp(String user,String host, String sftpkey, String remoteFile,String ECOMMERCE)
    {

        //2019_04_04_03_30.zip
        int port=22;

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);


            if (sftpkey.equalsIgnoreCase("/home/apaniagua/.ssh/id_rsa")) {
                jsch.addIdentity(sftpkey,"LOCAL_KEY_PASSWORD");
            }
            else {
                jsch.addIdentity(sftpkey);
            }

            session.setConfig("StrictHostKeyChecking", "no");
            log.info(ECOMMERCE + ": Establishing Connection..." + user + "@" + host + " with " + sftpkey);

            session.connect();
            log.info(ECOMMERCE +": Connection established.");

            //log.info(ECOMMERCE+ ": Creating SFTP Channel.");
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            InputStream out= null;
            log.info(ECOMMERCE + ": SFTP Getting file " + remoteFile);
            out= sftpChannel.get(remoteFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(out));

            List<String> strings = new ArrayList<String>();

            String line;
            while ((line = br.readLine()) != null)
            {
                strings.add(line);
            }

            out.close();
            br.close();
            sftpChannel.disconnect();
            session.disconnect();
            log.info(ECOMMERCE + ": SFTP channell and session closed");

            return strings;
        }
        catch(JSchException | SftpException | IOException e)
        {
            log.error(ECOMMERCE + ": " + e);
        }

        return null;

    }

    public boolean writeCsvFileToSftp(String user, String host, String sftpkey,List<String> csvlines, String remoteFile, String fileFormat,String header,String ECOMMERCE)
    {

        int port=22;

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);


            //TODO change before deploy (no passphrase)
            //String privateKey = "/home/apaniagua/.ssh/id_rsa";

            if (sftpkey.equalsIgnoreCase("/home/apaniagua/.ssh/id_rsa")) {
                jsch.addIdentity(sftpkey,"LOCAL_KEY_PASSWORD");
            }
            else {
                jsch.addIdentity(sftpkey);
            }

            session.setConfig("StrictHostKeyChecking", "no");
            log.info(ECOMMERCE + ": SFTP Establishing Connection..." + user + "@" + host + " with " + sftpkey);

            session.connect();
            log.info(ECOMMERCE + ": SFTP Connection established.");

            //log.info(ECOMMERCE + ": SFTP Creating channel.");
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            //log.info(ECOMMERCE + ": SFTP Channel created.");


            String content = header + "\n";
            Iterator<String> lisIterator = csvlines.iterator();

            log.info(ECOMMERCE + ": SFTP: will write " + csvlines.size() + " lines to file " + remoteFile);


            while (lisIterator.hasNext()) {

                content += lisIterator.next() + "\n";
            }

            InputStream stream = new ByteArrayInputStream(content.getBytes ());

            if (fileFormat.equalsIgnoreCase("csv")) {
                sftpChannel.put (stream, remoteFile + ".csv");
            }
            else if (fileFormat.equalsIgnoreCase("zip")) {
                InputStream compressedStream=getCompressed(stream);
                sftpChannel.put (compressedStream, remoteFile + ".zip");
            }


            stream.close();
            sftpChannel.disconnect();
            session.disconnect();
            content="";
            csvlines.clear();

            log.info(ECOMMERCE + ": SFTP Channel and session closed.");



            return true;
        }
        catch(JSchException | IOException | SftpException e)
        {
            log.error("MOTORTOWN: " + e);
        }

        return false;

    }


    public InputStream getCompressed( InputStream is )
            throws IOException
    {
        byte data[] = new byte[2048];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream( bos );
        BufferedInputStream entryStream = new BufferedInputStream( is, 2048);
        ZipEntry entry = new ZipEntry( "products.csv" );
        zos.putNextEntry( entry );
        int count;
        while ( ( count = entryStream.read( data, 0, 2048) ) != -1 )
        {
            zos.write( data, 0, count );
        }
        entryStream.close();
        zos.closeEntry();
        zos.close();

        return new ByteArrayInputStream( bos.toByteArray() );
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
                jsch.addIdentity(sftpkey,"LOCAL_KEY_PASSWORD");
            }
            else {
                jsch.addIdentity(sftpkey);
            }

            session.setConfig("StrictHostKeyChecking", "no");
            //log.info("MOTORTOWN: SFTP Establishing Connection..." + user + "@" + host + " with " + sftpkey);

            session.connect();
            //log.info("MOTORTOWN: SFTP Connection established.");

            //log.info("MOTORTOWN: SFTP Creating channel.");
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


        }
        catch(JSchException | SftpException e)
        {
            log.error("MOTORTOWN: " + e);
        }

        return oldestFile;
    }


    public List<CsvRow> rowsFromSftpZip(String user, String host, String sftpkey, String remoteZipFile, String ECOMMERCE)
    {

        int port=22;
        List<CsvRow> rowsReaded = new ArrayList<>();

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);


            if (sftpkey.equalsIgnoreCase("/home/apaniagua/.ssh/id_rsa")) {
                jsch.addIdentity(sftpkey,"LOCAL_KEY_PASSWORD");
            }
            else {
                jsch.addIdentity(sftpkey);
            }

            session.setConfig("StrictHostKeyChecking", "no");
            log.info(ECOMMERCE + ": Establishing Connection..." + user + "@" + host + " with " + sftpkey);

            session.connect();
            log.info(ECOMMERCE + ": Connection established.");


            //log.info(ECOMMERCE + ": Creating SFTP Channel.");
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            //log.info(ECOMMERCE + ": SFTP Reading zip file " + remoteZipFile);

            List<InputStream> inputStreams = new ArrayList<>();
            List<String> inputFilenames = new ArrayList<>();

            try (ZipInputStream zipInputStream = new ZipInputStream(sftpChannel.get(remoteZipFile))) {

                ZipEntry entry = zipInputStream.getNextEntry();

                while (entry != null) {

                    log.info(ECOMMERCE + ": Zip file contains this: " + entry.getName());

                    InputStream in = convertToInputStream(zipInputStream);

                    inputStreams.add(in);
                    inputFilenames.add(entry.getName());

                    Scanner scanner = new Scanner(in);
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        CsvRow row = new CsvRow(line, entry.getName(),remoteZipFile);
                        rowsReaded.add(row);
                    }

                    zipInputStream.closeEntry();
                    entry = zipInputStream.getNextEntry();
                }
            }

            sftpChannel.disconnect();
            log.info(ECOMMERCE + ": SFTP GET channel disconnect");


            return rowsReaded;
        }


        catch(JSchException | IOException | SftpException e)
        {
            log.error(ECOMMERCE + ": " + e);
        }

        return null;

    }

    private static InputStream convertToInputStream(final ZipInputStream inputStreamIn) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(inputStreamIn, out);
        return new ByteArrayInputStream(out.toByteArray());
    }

}
