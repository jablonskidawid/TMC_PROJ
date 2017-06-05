package com.tmc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpCsvDownloader {

	public static void downloadFile(String fileURL, String saveDir) throws IOException {
		URL url = new URL(fileURL);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		int responseCode = httpConn.getResponseCode();
		
		String fileName = "";
		String disposition = httpConn.getHeaderField("Content-Disposition");
		String contentType = httpConn.getContentType();
		int contentLength = httpConn.getContentLength();
		
		fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,fileURL.length());
		
		InputStream inputStream = httpConn.getInputStream();
		String saveFilePath = saveDir + File.separator + fileName;
		
		FileOutputStream outputStream = new FileOutputStream(saveFilePath);
		
		int bytesRead = -1;
		byte[] buffer = new byte[1048576];
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		
		outputStream.close();
		inputStream.close();
		
		httpConn.disconnect();
	}
}
