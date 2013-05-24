package com.testprioritization.domain.dao.exceptions;

public class TestResultsAlreadyProcessedException extends Exception {

	private static final long serialVersionUID = 1L;

	public TestResultsAlreadyProcessedException(String project) {
		super("The test results for the last build in project " + project
				+ " have already been saved.");
	}

}
