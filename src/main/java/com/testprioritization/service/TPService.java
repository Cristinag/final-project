package com.testprioritization.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.testprioritization.domain.dao.exceptions.NoSuchProjectException;
import com.testprioritization.domain.dao.exceptions.TestResultsAlreadyProcessedException;

public interface TPService {

	/**
	 * Returns the tests corresponding to |project| ordered as to increase their
	 * probability of fault detection.
	 * 
	 * @param project
	 *            project for which the test ordering needs to be computed
	 * @return list of tests ordered according to their chance of revealing
	 *         faults
	 * @throws NoSuchProjectException
	 */
	List<String> prioritizeTests(String project) throws NoSuchProjectException;

	/**
	 * Parses a unified diff containing changes since the last build and stores
	 * these changes.
	 * 
	 * @param diffFile
	 *            buffered reader containing the changes made since the last
	 *            build in the form of a unified diff
	 * @param project
	 *            project to which the changes were made
	 * @throws IOException
	 */
	void processChanges(BufferedReader diffFile, String project)
			throws IOException;

	/**
	 * Parses a TAP file containing the results of the tests run on the latest
	 * build and stores these. This is not an immutable operation, so the method
	 * can be called only once for each build. Calling it more than once will
	 * throw a |TestResultsAlreadyProcessedException|.
	 * 
	 * @param testReport
	 *            file containing the test results in TAP format
	 * @param project
	 *            project on which tests were run
	 * @throws TestResultsAlreadyProcessedException
	 *             , FileNotFoundException, IOException, NoSuchProjectException
	 */
	void processTestResults(BufferedReader testReport, String project)
			throws FileNotFoundException, IOException,
			TestResultsAlreadyProcessedException, NoSuchProjectException;

}
