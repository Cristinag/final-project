package com.testprioritization.domain.dao;

/**
 * ChangesRepository contains for each project, each line in that project and
 * each test the number of times the line has been changed when the test failed.
 * This represents the probability that a line is changed given that a test
 * failed.
 */
public interface ChangesRepository {

	/**
	 * Retrieves the number of times |line| was changed given that |test| failed
	 * in |project|.
	 * 
	 * @param project
	 *            the project to which |line| and |test| correspond
	 * @param line
	 *            line id as stored in the LinesRepository
	 * @param test
	 *            name of the test
	 * @return the number of times |line| was changed when |test| failed
	 */
	int getProbChanged(String project, int line, String test);

	/**
	 * Increments the number of times |line| was changed when |test| failed in
	 * |project|.
	 * 
	 * @param project
	 *            the project to which |line| and |test| correspond
	 * @param test
	 *            name of the test
	 * @param line
	 *            line id
	 */
	void incrementChangeCount(String project, String test, int line);

}
