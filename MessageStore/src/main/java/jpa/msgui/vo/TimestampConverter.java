package jpa.msgui.vo;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public class TimestampConverter implements org.apache.johnzon.mapper.Converter<Timestamp> {
	final static Logger logger = Logger.getLogger(TimestampConverter.class);

	static SimpleDateFormat sdf_std = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSXXX");
	
	@Override
	public Timestamp fromString(String arg0) {
		try {
			Date dt = sdf_std.parse(arg0);
			return new Timestamp(dt.getTime());
		}
		catch (ParseException e) {
			List<String> formatStrs = Arrays.asList("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd HH:mm:ss.SSSZ",
					"yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss.SSS",
					"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss");
			for (String format : formatStrs) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat(format);
					Date dt = sdf.parse(arg0);
					return new Timestamp(dt.getTime());
				}
				catch (ParseException e2) {}
			}
			logger.error("Invalid timestamp: " + arg0);
		}
		return new Timestamp(System.currentTimeMillis());
	}

	@Override
	public String toString(Timestamp arg0) {
		return sdf_std.format(arg0);
	}

	public static void main(String[] args) {
		TimestampConverter cvtr = new TimestampConverter();
		String tmsStr = cvtr.toString(new Timestamp(System.currentTimeMillis()));
		logger.info(tmsStr);
		Timestamp tms = cvtr.fromString(tmsStr);
		logger.info(sdf_std.format(tms));
		logger.info(cvtr.fromString("2017-12-16T15:55:49.522-05:00"));
		logger.info(cvtr.fromString("2017-12-16 15:55:49.522"));
		logger.info(cvtr.fromString("2017-12-16T15:55:49.522-0500"));
		logger.info(cvtr.fromString("2017-12-16 15:55:49.522-0500"));
		logger.info(cvtr.fromString("2017-12-16 15:55:49"));
	}
}
