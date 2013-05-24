package com.testprioritization.service.utils;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;

public class TapParserTest {
	private static final String tapFilePath = "src/test/resources/test-data/TAP.txt";

	@Test
	public void shouldParseTapFile() throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(tapFilePath));
		Map<String, Boolean> testOutcomes = TAPParser.parseTestsRaport(br);
		br.close();
		assertTrue(testOutcomes.size() == 4);
		assertTrue(testOutcomes.get("1"));
		assertTrue(!testOutcomes.get("2"));
		assertTrue(testOutcomes.get("3"));
		assertTrue(!testOutcomes.get("4"));
	}
}
