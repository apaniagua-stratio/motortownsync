package com.stratio.microservice.motortownsync.service;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class SftpWriter {


    public boolean writeCsvFileToSftp(String user, String host, String sftpkey,List<String> csvlines, String remoteFile)
    {

        int port=22;

        //String remoteFile="sample.txt";

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);


            //change before deploy (no passphrase)
            //String privateKey = "/home/apaniagua/.ssh/id_rsa";


            log.info("AURGI SFTP READING KEY" + sftpkey);

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


            String content = "";
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


            return true;
        }
        catch(JSchException | SftpException e)
        {
            log.error("AURGI: " + e);
        }

        return false;

    }

}
