package com.testprioritization.domain.dao;

/**
 * ChangesRepository contains for each line and each test in a project the
 * number of times the line has been changed when the test failed (i.e. the
 * probability that a line is changed given that a test failed).
 */
public interface ChangesRepository {

	/**
	 * Retrieves the number of times the line with id |lineId| was changed given
	 * that |test| failed.
	 * 
	 * @param lineId
	 *            line id
	 * @param test
	 *            name of the test
	 * @return probability that line with id |lineid| is changed given that
	 *         |test| failed
	 */
	int getProbChanged(Integer lineId, String test);

	/**
	 * Increments the number of times the line with id |lineId| was changed when
	 * |test| failed.
	 * 
	 * @param lineId
	 *            line id
	 * @param test
	 *            name of the test
	 */
	void incrementChangeCount(Integer lineId, String test);

	/**
	 * Removes the entry associated with the given line id.
	 * 
	 * @param lineId
	 *            line id
	 */
	void deleteChangesForLineId(Integer lineId);

}
