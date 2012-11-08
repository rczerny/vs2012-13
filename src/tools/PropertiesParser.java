package tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class has the purpose of parsing a given properties file and providing its
 * entries.
 */
public class PropertiesParser 
{
	private InputStream is = null;
	private String filename;

	/**
	 * Creates an instance of the properties parser, which analyses the given file
	 * @param filename The filename of the file to parse.
	 * @throws FileNotFoundException 
	 */
	public PropertiesParser(String filename) throws FileNotFoundException {
		this.filename = filename;
		is = ClassLoader.getSystemResourceAsStream(filename);
		if (is == null) {
			throw new FileNotFoundException("Properties file not found!");
		}
	}

	/**
	 * Parses the properties file an returns the value to the given property name
	 * @param propertyName The property name, whichs value shall be returned
	 * @return The value to the given property name
	 */
	public String getProperty(String propertyName) {
		String property = null;
		Properties props = new Properties();
		is = ClassLoader.getSystemResourceAsStream(filename);
		try {
			props.load(is);
			property = props.getProperty(propertyName);
			is.close();
		} catch (IOException e) {
			System.err.println("Error loading the property " + propertyName);
			e.printStackTrace();
		}
		return property;
	}
}
