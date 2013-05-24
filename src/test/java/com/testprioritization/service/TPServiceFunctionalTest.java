package com.testprioritization.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.testprioritization.domain.dao.BuildsRepository;
import com.testprioritization.domain.dao.exceptions.TestResultsAlreadyProcessedException;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:config/test-config.xml")
@ActiveProfiles("dev")
public class TPServiceFunctionalTest {

	@Autowired
	private TPService tpService;

	@Autowired
	private BuildsRepository buildsRepo;

	@Test(expected = TestResultsAlreadyProcessedException.class)
	@Transactional
	public void shouldThrowExceptionWhenTestResultsSavedTwice()
			throws IOException, TestResultsAlreadyProcessedException {
		String projectInDb = "project1";
		tpService.processTestResults(new BufferedReader(new StringReader("")),
				projectInDb);
		tpService.processTestResults(new BufferedReader(new StringReader("")),
				projectInDb);
	}

	@Test
	@Transactional
	public void shouldPrioritizeTest() throws IOException,
			TestResultsAlreadyProcessedException {
		List<String> tests = tpService.prioritizeTests("project2");
		assertEquals(4, tests.size());
		assertTrue(tests.contains(new String("1")));
		assertTrue(tests.contains(new String("2")));
		assertTrue(tests.contains(new String("3")));
		assertTrue(tests.contains(new String("4")));
	}
}
