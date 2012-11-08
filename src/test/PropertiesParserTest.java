package test;

import static org.junit.Assert.*;
import java.io.FileNotFoundException;
import org.junit.Test;

import tools.PropertiesParser;

public class PropertiesParserTest 
{
	private PropertiesParser pp;
		
	
	@Test(expected = FileNotFoundException.class)
	public void parseNonExistingFile() throws FileNotFoundException {
		pp = new PropertiesParser("nonexisting");
	}
	
	@Test
	public void parseExistingProperty() throws FileNotFoundException {
		pp = new PropertiesParser("registry.properties");
		assertEquals("localhost", pp.getProperty("registry.host"));
	}
	
	@Test
	public void parseNonExistingProperty() throws FileNotFoundException {
		pp = new PropertiesParser("registry.properties");
		assertNull(pp.getProperty("registry.hallolololo"));
	}
}
