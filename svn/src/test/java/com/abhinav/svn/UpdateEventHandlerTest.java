package com.abhinav.svn;

import static org.junit.Assert.assertNotEquals;

import java.util.List;

import org.junit.Test;

public class UpdateEventHandlerTest {

	@Test
	public void test() {
		UpdateEventHandler eventHandler = new UpdateEventHandler("C:\\microservices\\branches\\PHIX_Payroll_0.1\\services\\payroll\\db", null, null);
		List<List<String>> fileNames = eventHandler.populateFileNames();
		assertNotEquals("Files Length", fileNames.get(0).size(), fileNames.get(1).size());
	}

}
