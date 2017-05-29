package jpa.util;

public class EnvUtil {

	public static String getEnv() {
		return (System.getProperty("env","dev"));
	}
}
