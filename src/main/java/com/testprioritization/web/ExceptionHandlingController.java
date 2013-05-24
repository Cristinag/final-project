package com.testprioritization.web;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.testprioritization.web.exceptions.DuplicateReferenceException;
import com.testprioritization.web.exceptions.ResourceNotFoundException;

public abstract class ExceptionHandlingController {

	private final Logger LOG = LoggerFactory
			.getLogger(ExceptionHandlingController.class);

	/**
	 * Maps DuplicateReferenceException to BAD_REQUEST (400) HTTP status code.
	 */
	@ExceptionHandler(DuplicateReferenceException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public void mapBadRequest(Exception e, HttpServletResponse response)
			throws IOException {
		LOG.error("Caught exception! Bad request:", e.getMessage());
		response.getWriter().write(
				"{ status: 400, description: 'Bad request', message: '"
						+ e.getMessage() + "'}");

	}

	/**
	 * Maps ResourceNotFoundException to NOT_FOUND (404) HTTP status code.
	 */
	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public void mapNotFound(Exception e, HttpServletResponse response)
			throws IOException {
		LOG.error("Caught exception! Mapping to Not found:", e);
		response.getWriter().write(
				"{ status: 404, description: 'Not found', message: '"
						+ e.getMessage() + "'}");

	}

	/**
	 * Maps java exceptions to INTERNAL_SERVER_ERROR (500) HTTP status code.
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public void mapInternalServerError(Exception e, HttpServletResponse response)
			throws IOException {
		LOG.error("Caught exception! Mapping to Internal server error:", e);
		response.getWriter().write(
				"{ status: 500, description: 'Internal server error', message: '"
						+ e.getMessage() + "'}");

	}
}