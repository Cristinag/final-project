package com.testprioritization.service.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.testprioritization.domain.dao.ChangesRepository;
import com.testprioritization.domain.dao.TestsRepository;
import com.testprioritization.domain.model.Test;

public class BayesPrioritizer {

	ChangesRepository changesRepo;
	TestsRepository testsRepo;

	public BayesPrioritizer(TestsRepository testsRepo,
			ChangesRepository changesRepo) {
		this.testsRepo = testsRepo;
		this.changesRepo = changesRepo;
	}

	// Takes a list of changes and a project and returns the tests corresponding
	// to the project in descending order of their probability of failing.
	public List<String> orderTest(List<Integer> changes, String project) {
		// TODO: (Improvement) Give a higher priority to tests that failed
		// recently as they are more likely to fail again.
		Iterator<Test> testIt = testsRepo.getTests(project).iterator();
		// |probs| contains for each test the probability that it will fail
		// given the changes in |linesChanged|.
		final Map<String, Double> probs = new HashMap<String, Double>();
		List<String> tests = new ArrayList<String>();
		while (testIt.hasNext()) {
			Test test = testIt.next();
			tests.add(test.getName());
			float numberFailures = test.getFailures();
			float numberRuns = test.getRuns();
			Iterator<Integer> changedLinesIt = changes.iterator();
			// |probTestFails| is the probability that |test| fails.
			double probTestFails = numberFailures / numberRuns;
			// |probChangesGivenTestFails| is the probability that the changes
			// |changes| occurred given that test |test| failed.
			double probChangesGivenTestFails = 1;
			if (numberFailures == 0) {
				probs.put(test.getName(), 0.0);
				continue;
			}

			while (changedLinesIt.hasNext()) {
				int changedLine = changedLinesIt.next();
				double probChangeGivenTestFails = changesRepo.getProbChanged(
						changedLine, test.getName());
				probChangesGivenTestFails *= (1 + (probChangeGivenTestFails / numberFailures));
			}
			// The probability that a test fails given a set of changes is
			// proportional to the product between the probability that the test
			// fails and the probability that the changes occur given the test
			// fails.
			probs.put(test.getName(), probChangesGivenTestFails * probTestFails);
		}

		// Sort the test in descending order of their probability of revealing
		// faults.
		Collections.sort(tests, new Comparator<String>() {
			@Override
			public int compare(String test1, String test2) {
				return probs.get(test2).compareTo(probs.get(test1));
			}
		});
		return tests;
	}
}
