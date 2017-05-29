package jpa.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

import org.apache.log4j.Logger;

public final class BlobUtil {
	static final Logger logger = Logger.getLogger(BlobUtil.class);
	private BlobUtil() {
		// utility class
	}

	public static byte[] objectToBytes(Object obj) {
		if (obj == null) {
			return null;
		}
		// convert java object to a output stream
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// write the object to the output stream
		try {
			ObjectOutputStream objos = new ObjectOutputStream(baos);
			objos.writeObject(obj);
			objos.flush();
			objos.reset();
			objos.close();
			// get byte array
			byte[] baosarray = baos.toByteArray();
			baos.close();
			return baosarray;
		}
		catch (IOException e) {
			logger.error("IOException caught",e);
			throw new RuntimeException("IOException caught", e);
		}
	}

	public static byte[] beanToXmlBytes(Object obj) {
		if (obj == null) {
			return null;
		}
		// convert java object to a output stream using XMLEncoder
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLEncoder encoder = new XMLEncoder(baos);
		encoder.writeObject(obj);
		encoder.flush();
		encoder.close();
		// get byte array
		byte[] baosarray = baos.toByteArray();
		try {
			baos.close();
		}
		catch (IOException e) {
			logger.error("IOException caught",e);
		}
		return baosarray;
	}

	public static Object bytesToObject(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		// wrap the bytes into an object input stream
		try {
			ObjectInputStream objis = new ObjectInputStream(new ByteArrayInputStream(bytes));
			// get object from the input stream
			Object obj = objis.readObject();
			objis.close();
			return obj;
		}
		catch (ClassNotFoundException e) {
			logger.error("ClassNotFoundException caught",e);
			throw new RuntimeException("ClassNotFoundException caught", e);
		}
		catch (IOException e) {
			logger.error("IOException caught",e);
			throw new RuntimeException("IOException caught", e);
		}
	}

	public static Object xmlBytesToBean(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		// wrap the bytes into an XMLDecoder
		XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(bytes));
		// get object from XMLDecoder
		Object obj = decoder.readObject();
		decoder.close();
		return obj;
	}
	
    /**
	 * Returns a copy of the object, or null if the object cannot be serialized.
	 * @throws IllegalArgumentException if the object cannot be serialized.
	 */
    public static Object deepCopy(Object orig) {
    	if (orig == null) {
    		return null;
    	}
    	if (!(orig instanceof java.io.Serializable)) {
    		throw new IllegalArgumentException("Input object must be Serializable");
    	}
        Object obj = null;
        try {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();
            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        }
        catch(IOException e) {
        	logger.error("IOException caught", e);
        }
        catch(ClassNotFoundException cnfe) {
        	logger.error("ClassNotFoundException caught", cnfe);
        }
        return obj;
    }
    
    public static void main(String[] args) {
    	try {
    		Calendar cal1 = Calendar.getInstance();
			cal1.set(Calendar.DAY_OF_MONTH, cal1.getActualMaximum(Calendar.DAY_OF_MONTH));
			Calendar cal2 = (Calendar) deepCopy(cal1);
			System.err.println("Is deepCopy a success? " + cal2.equals(cal1));
			logger.info("Calendar 1: " + cal1.getTime());
			cal1.roll(Calendar.MONTH, false);
			logger.info("Calendar 2: " + cal1.getTime());
			cal2.roll(Calendar.MONTH, false);
			logger.info("Calendar 3: " + cal2.getTime());
			byte[] cal1bytes = objectToBytes(cal1);
			Calendar cal1Restored = (Calendar)bytesToObject(cal1bytes);
			System.err.println("Is Object<->Bytes conversion a success? " + cal1.equals(cal1Restored));
			byte[] cal2xmlbytes = beanToXmlBytes(cal2);
			Calendar cal2Restored = (Calendar)xmlBytesToBean(cal2xmlbytes);
			System.err.println("Is Object<->XmlBytes conversion a success? " + cal2.equals(cal2Restored));
    	}
    	catch (Exception e) {
    		logger.error("Exceeption", e);
    	}
    }
}
