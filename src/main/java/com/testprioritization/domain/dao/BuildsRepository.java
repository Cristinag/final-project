package com.testprioritization.domain.dao;

import java.util.List;

import com.testprioritization.domain.dao.exceptions.NoSuchProjectException;
import com.testprioritization.domain.dao.exceptions.TestResultsAlreadyProcessedException;

/**
 * BuildsRepository contains the last build for all projects. A build for a
 * project is represented by the ids of the changes made in that build (as
 * stored in the changes repository) and whether the test outcomes have been
 * processed for that build or not.
 */
public interface BuildsRepository {

	/**
	 * Retrieves the changes made in the last build of |project|.
	 * 
	 * @param project
	 * @return changes a list with the ids of the changes made in the last
	 *         build.
	 * @throws NoSuchProjectException
	 */
	public List<Integer> getChanges(String project)
			throws NoSuchProjectException;

	/**
	 * Saves the changes made since the last build for |project|.
	 * 
	 * @param project
	 *            the project whose build is to be updated
	 * @param changes
	 *            the ids of the changes corresponding to the last build
	 */
	public void saveChanges(String project, List<Integer> changes);

	/**
	 * Marks the fact that the test results corresponding to the last build of
	 * |project| were saved.
	 * 
	 * @param project
	 * @throws NoSuchProjectException
	 *             , TestResultsAlreadyProcessedException
	 */
	public void setTestsResultsSaved(String project)
			throws NoSuchProjectException, TestResultsAlreadyProcessedException;
}
