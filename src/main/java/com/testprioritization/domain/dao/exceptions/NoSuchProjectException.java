package com.testprioritization.domain.dao.exceptions;

public class NoSuchProjectException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NoSuchProjectException(String project) {
		super("No project " + project + " was found.");
	}
}
