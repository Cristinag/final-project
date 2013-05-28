package com.testprioritization.service.utils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestReportParser {
	static Pattern TEST_RESULTS = Pattern
			.compile("TEST:\\s([\\S]*)\\sOUTCOME:\\s([\\S]*)");
	
	public static Map<String, Boolean> parseTestsRaport(BufferedReader testReport) throws FileNotFoundException,
			IOException {
		Map<String, Boolean> newTestReport = new HashMap<String, Boolean>();
		// Get the object of DataInputStream
		String reportLine;
		while ((reportLine = testReport.readLine()) != null) {
			Matcher m = TEST_RESULTS.matcher(reportLine);
			// Print the content on the console
			if (m.find()) {
				String testName = m.group(1);
				String outcome = m.group(2);

				if (outcome.equals("SKIPPED"))
					continue;
				boolean passed = false;
				if (outcome.equals("OK"))
					passed = true;
				//TODO: Only add the first time the test failed
				if (!testName.isEmpty() && !Character.isDigit(testName.charAt(0))) {
					newTestReport.put(testName, new Boolean(passed));
				}
			} 
		}
		return newTestReport;
	}

}
