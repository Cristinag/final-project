package com.testprioritization.web;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import com.testprioritization.domain.dao.exceptions.NoSuchProjectException;
import com.testprioritization.domain.dao.exceptions.TestResultsAlreadyProcessedException;
import com.testprioritization.service.TPService;
import com.testprioritization.web.exceptions.DuplicateReferenceException;
import com.testprioritization.web.exceptions.ResourceNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class TPControllerTest {
	private static final String project = "project_name";
	private TPController controller;

	@Mock
	private TPService tpService;

	@Before
	public void setup() {
		controller = new TPController(tpService);
	}

	@Test
	public void shouldPrioritizeTests() throws Exception {
		List<String> tests = new ArrayList<String>();
		tests.add("test1");
		tests.add("test2");
		given(tpService.prioritizeTests(project)).willReturn(tests);
		List<String> returnedTests = controller.prioritizeTests(project);
		assertThat(returnedTests, is(sameInstance(tests)));
	}

	@Test(expected = ResourceNotFoundException.class)
	public void shouldThrowProjectDoesNotExistException() throws Exception {
		willThrow(new NoSuchProjectException(project.toString())).given(
				tpService).prioritizeTests(project);
		controller.prioritizeTests(project);
	}

	@Test(expected = DuplicateReferenceException.class)
	public void shouldThrowTestResultsAlreadyProcessedException()
			throws Exception {
		willThrow(new TestResultsAlreadyProcessedException(project.toString()))
				.given(tpService).processTestResults(
						Mockito.any(BufferedReader.class), Mockito.eq(project));
		MultipartFile file = Mockito.mock(MultipartFile.class);
		given(file.getInputStream())
				.willReturn(Mockito.mock(InputStream.class));
		controller.saveTestResults(project, file);
		verify(file, times(1)).getInputStream();
	}
}