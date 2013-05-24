package com.testprioritization.domain.dao;

import java.util.List;
import java.util.Map;

import com.testprioritization.domain.model.Test;

/**
 * TestsRepository contains the tests in all projects.
 */

public interface TestsRepository {

	/**
	 * Returns the tests associated with the given project.
	 * 
	 * @param project
	 *            the project whose tests are returned
	 * @return list of tests
	 */
	List<Test> getTests(String project);

	/**
	 * Save the test results for a given project.
	 * 
	 * @param project
	 *            the name of the project to which the tests correspond
	 * @param tests
	 *            the tests in |project| that were run and whether they failed
	 *            or not
	 */
	void saveTestResults(String project, Map<String, Boolean> tests);

}
