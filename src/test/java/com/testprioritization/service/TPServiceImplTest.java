package com.testprioritization.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.testprioritization.domain.dao.BuildsRepository;
import com.testprioritization.domain.dao.ChangesRepository;
import com.testprioritization.domain.dao.LinesRepository;
import com.testprioritization.domain.dao.TestsRepository;
import com.testprioritization.domain.dao.exceptions.NoSuchProjectException;
import com.testprioritization.domain.dao.exceptions.TestResultsAlreadyProcessedException;

@RunWith(MockitoJUnitRunner.class)
public class TPServiceImplTest {
	private TPServiceImpl tpService;

	@Mock
	private ChangesRepository changesRepo;
	@Mock
	private LinesRepository linesRepo;
	@Mock
	private TestsRepository testsRepo;
	@Mock
	private BuildsRepository buildsRepo;

	private static final String project = "project";

	@Before
	public void setup() {
		tpService = new TPServiceImpl(buildsRepo, changesRepo, linesRepo,
				testsRepo);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldSaveChanges() throws Exception {
		BufferedReader br = Mockito.mock(BufferedReader.class);
		given(br.readLine()).willReturn(null);
		tpService.processChanges(br, project);
		verify(buildsRepo, times(1)).saveChanges(Mockito.eq(project),
				Mockito.anyList());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldSaveTestResults() throws NoSuchProjectException,
			TestResultsAlreadyProcessedException, IOException {
		BufferedReader br = Mockito.mock(BufferedReader.class);
		given(br.readLine()).willReturn(null);
		tpService.processTestResults(br, project);
		verify(buildsRepo, times(1)).setTestsResultsSaved(project);
		verify(testsRepo, times(1)).saveTestResults(Mockito.eq(project),
				Mockito.anyMap());
	}

	@Test
	public void shouldPrioritizeTests() {
		List<Integer> changes = new ArrayList<Integer>();
		changes.add(1);
		List<com.testprioritization.domain.model.Test> tests = new ArrayList<com.testprioritization.domain.model.Test>();
		com.testprioritization.domain.model.Test test = new com.testprioritization.domain.model.Test(
				"test", 0, 0);
		tests.add(test);
		given(testsRepo.getTests(project)).willReturn(tests);
		given(
				changesRepo.getProbChanged(Mockito.eq(project),
						Mockito.anyInt(), Mockito.eq("test"))).willReturn(0);
		List<String> returnedTests = tpService.prioritizeTests(project);
		List<String> expectedTests = new ArrayList<String>();
		expectedTests.add(test.getName());
		assertEquals(returnedTests, expectedTests);
	}
}
