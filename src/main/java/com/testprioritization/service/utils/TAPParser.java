package com.testprioritization.service.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TAPParser {
	static Pattern TEST_OK = Pattern.compile("ok\\s([\\S]*)\\s[\\S]*");
	static Pattern TEST_NOT_OK = Pattern
			.compile("not\\sok\\s([\\S]*)\\s[\\S]*");

	public static Map<String, Boolean> parseTestsRaport(
			BufferedReader testReport) throws FileNotFoundException,
			IOException {
		Map<String, Boolean> newTestReport = new HashMap<String, Boolean>();
		String reportLine;
		while ((reportLine = testReport.readLine()) != null) {
			Matcher m = TEST_OK.matcher(reportLine);
			if (m.find()) {
				String testName = m.group(1);
				if (!testName.isEmpty()) {
					newTestReport.put(testName, true);
				}
			}

			m = TEST_NOT_OK.matcher(reportLine);
			if (m.find()) {
				String testName = m.group(1);
				if (!testName.isEmpty()) {
					newTestReport.put(testName, false);
				}
			}
		}
		return newTestReport;
	}
}