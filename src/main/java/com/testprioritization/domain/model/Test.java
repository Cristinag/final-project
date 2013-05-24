package com.testprioritization.domain.model;

public class Test {
	private final int failures;
	private final int runs;
	private final String test;

	public Test(String name, int runs, int failures) {
		this.test = name;
		this.runs = runs;
		this.failures = failures;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Test)) {
			return false;
		}
		Test anotherTest = (Test) object;
		return anotherTest.getName().equals(test)
				&& anotherTest.getFailures() == failures
				&& anotherTest.getRuns() == runs;
	}

	public int getFailures() {
		return failures;
	}

	public String getName() {
		return test;
	}

	public int getRuns() {
		return runs;
	}

}
