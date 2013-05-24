package com.testprioritization.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.testprioritization.domain.dao.exceptions.NoSuchProjectException;
import com.testprioritization.domain.dao.exceptions.TestResultsAlreadyProcessedException;
import com.testprioritization.service.TPService;
import com.testprioritization.web.exceptions.DuplicateReferenceException;
import com.testprioritization.web.exceptions.ResourceNotFoundException;

/**
 * TPController class exposes RESTful endpoints for saving changes and test
 * results and prioritizing test cases.
 */

@Controller
public class TPController extends ExceptionHandlingController {

	private static final Logger logger = LoggerFactory
			.getLogger(TPController.class);
	private final TPService tpService;

	@Inject
	public TPController(TPService tpService) {
		this.tpService = tpService;
	}

	/**
	 * Saves the changes made to the given project.
	 * 
	 * @param project
	 *            the project title
	 * @param diffFile
	 *            unified diff file containing all the changes since the last
	 *            build
	 * @throws IOException
	 */
	@RequestMapping(value = "/", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	public void saveChanges(@RequestParam("project") String project,
			@RequestPart("diff") MultipartFile diffFile,
			HttpServletResponse response) throws IOException {
		logger.info("Saving changes for project " + project);
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(diffFile.getInputStream()));
		tpService.processChanges(bufferedReader, project);
		bufferedReader.close();
		response.setHeader("Location", "/" + project);
	}

	/**
	 * Returns the tests corresponding to the referenced project ordered
	 * according to their probability of failing.
	 * 
	 * @param project
	 *            project for which to prioritise tests
	 * @return list of prioritised tests
	 */
	@RequestMapping(value = "/{project}", method = RequestMethod.GET)
	public @ResponseBody
	List<String> prioritizeTests(@PathVariable String project) {
		logger.info("Prioritizing tests for project " + project);
		List<String> orderedTests;
		try {
			orderedTests = tpService.prioritizeTests(project);
		} catch (NoSuchProjectException e) {
			throw new ResourceNotFoundException(e);
		}
		logger.info("The prioritized tests for project " + project + " are "
				+ orderedTests);
		return orderedTests;
	}

	/**
	 * Saves the test results for a given project.
	 * 
	 * @param project
	 *            project to which the tests correspond
	 * @param testReport
	 *            file containing the test results in TAP format
	 * @throws IOException
	 */
	@RequestMapping(value = "/{project}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	public void saveTestResults(@PathVariable String project,
			@RequestPart("test-report") MultipartFile testReport)
			throws IOException {
		logger.info("Saving the test results for project " + project);
		InputStream inputStream = testReport.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		try {
			tpService.processTestResults(bufferedReader, project);
			bufferedReader.close();
		} catch (TestResultsAlreadyProcessedException e) {
			throw new DuplicateReferenceException(e);
		} catch (NoSuchProjectException e) {
			throw new ResourceNotFoundException(e);
		}
	}
}
