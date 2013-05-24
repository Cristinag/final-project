package com.testprioritization.web.exceptions;


public class DuplicateReferenceException extends RuntimeException {

	public DuplicateReferenceException(Exception e) {
		super(e);
	}

	private static final long serialVersionUID = 1L;

}