package com.testprioritization.domain.dao;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:config/test-config.xml")
@ActiveProfiles("dev")
public class TestsRepositoryFunctionalTest {
	@Autowired
	private TestsRepository testsRepo;

	@Test
	public void shouldGetTests() {
		String projectInDb = "project3";
		List<com.testprioritization.domain.model.Test> tests = testsRepo
				.getTests(projectInDb);
		assertTrue(tests.size() == 1);
		com.testprioritization.domain.model.Test testInDb = new com.testprioritization.domain.model.Test(
				"test5", 4, 3);
		assertTrue(tests.get(0).equals(testInDb));
	}

	@Test
	public void shouldSaveTestResults() {
		String newProject = "newProject";
		Map<String, Boolean> testOutcomes = new HashMap<String, Boolean>();
		testOutcomes.put("test1", true);
		testOutcomes.put("test2", false);
		testsRepo.saveTestResults(newProject, testOutcomes);
		List<com.testprioritization.domain.model.Test> expectedTests = new ArrayList<com.testprioritization.domain.model.Test>();
		expectedTests.add(new com.testprioritization.domain.model.Test("test1",
				1, 0));
		expectedTests.add(new com.testprioritization.domain.model.Test("test2",
				1, 1));
		List<com.testprioritization.domain.model.Test> returnedTests = testsRepo
				.getTests(newProject);
		assertTrue(returnedTests.containsAll(expectedTests));
		assertTrue(expectedTests.containsAll(returnedTests));
	}
}
