package jpa.model;

import org.springframework.instrument.classloading.SimpleLoadTimeWeaver;

public class ClassCastClassloaderWeaver extends SimpleLoadTimeWeaver {
	/*
	 *  XXX solves sick problem with MyBean cannot be cast to MyBean(non-Javadoc)
	 * @see org.springframework.instrument.classloading.SimpleLoadTimeWeaver#getInstrumentableClassLoader()
	 */
	@Override
	public ClassLoader getInstrumentableClassLoader() {
		return super.getInstrumentableClassLoader().getParent();
	}
}
