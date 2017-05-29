package jpa.test.bean;

import java.net.URLClassLoader;

public class PrintClassPath {

	public static void print() {
		ClassLoader cl = ClassLoader.getSystemClassLoader();

		java.net.URL[] urls = ((URLClassLoader) cl).getURLs();

		for (java.net.URL url : urls) {
			System.out.println("ClassPath: " + url.getFile());
		}
	}
}
