package jpa.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUtil {
	static final Logger logger = LogManager.getLogger(FileUtil.class);

	public static byte[] loadFromFile(String filePath, String fileName) {
		InputStream is = getInputStream(filePath, fileName);
		
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		try {
			while ((len=bis.read(buffer))>0) {
				baos.write(buffer, 0, len);
			}
			return baos.toByteArray();
		}
		catch (IOException e) {
			throw new RuntimeException("IOException caught: " + e.getMessage());
		}
	}

	public static List<String> loadFromTextFile(String filePath, String fileName) {
		InputStream is = getInputStream(filePath, fileName);
		
		BufferedReader br = new BufferedReader( new InputStreamReader(is));
		List<String> lineList = new ArrayList<String>();
		String line = null;
		try {
			while ((line=br.readLine())!=null) {
				lineList.add(line);
			}
			return lineList;
		}
		catch (IOException e) {
			throw new RuntimeException("IOException caught: " + e.getMessage());
		}
	}

	private static InputStream getInputStream(String filePath, String fileName) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		//loader = TestUtil.class.getClassLoader(); // works too
		if (!filePath.endsWith(File.separator) && !filePath.endsWith("/")) {
			filePath += File.separator;
		}
		InputStream is = loader.getResourceAsStream(filePath + fileName);
		if (is == null) {
			// try again with class loader from this class
			is = FileUtil.class.getClassLoader().getResourceAsStream(filePath + fileName);
			if (is == null) {
				// try system class loader
				is = ClassLoader.getSystemResourceAsStream(filePath + fileName);
				if (is == null) {
					throw new RuntimeException("File (" + filePath + fileName + ") not found!");
				}
			}
		}
		logger.info("Loading file from location: " + filePath + fileName);
		return is;
	}
}
