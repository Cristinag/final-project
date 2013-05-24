package com.testprioritization.web.exceptions;

import com.testprioritization.domain.dao.exceptions.NoSuchProjectException;

public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(NoSuchProjectException e) {
		super(e);
	}

	private static final long serialVersionUID = 1L;

}