package com.tmc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Klasa odpowiadająca za pobieranie plików CSV z podanego zasobu
 */
public class HttpCsvDownloader {

    private static final int BUFFER_SIZE = 1048576;

    public static boolean downloadFile(String fileURL, String saveDir) {
        try {
            URL url = new URL(fileURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();
            if (responseCode / 100 == 2) {
                String fileName;
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());

                InputStream inputStream = httpConn.getInputStream();
                String saveFilePath = saveDir + File.separator + fileName;

                FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                int bytesRead;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();
                httpConn.disconnect();
                return true;
            } else {
                httpConn.disconnect();
                return false;
            }
        } catch (IOException e) {
            System.err.println("Wystąpił błąd podczas pobierania zasobu " + fileURL);
            return false;
        }
    }
}
