package com.testprioritization.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.testprioritization.domain.dao.BuildsRepository;
import com.testprioritization.domain.dao.ChangesRepository;
import com.testprioritization.domain.dao.LinesRepository;
import com.testprioritization.domain.dao.TestsRepository;
import com.testprioritization.domain.dao.exceptions.NoSuchProjectException;
import com.testprioritization.domain.dao.exceptions.TestResultsAlreadyProcessedException;
import com.testprioritization.service.utils.BayesPrioritizer;
import com.testprioritization.service.utils.RevisionParser;
import com.testprioritization.service.utils.TAPParser;

@Service
public class TPServiceImpl implements TPService {

	private final BuildsRepository buildsRepo;
	private final ChangesRepository changesRepo;
	private final LinesRepository linesRepo;
	private final TestsRepository testsRepo;

	@Inject
	public TPServiceImpl(BuildsRepository buildsRepo,
			ChangesRepository changesRepo, LinesRepository linesRepo,
			TestsRepository testsRepo) {
		this.buildsRepo = buildsRepo;
		this.changesRepo = changesRepo;
		this.linesRepo = linesRepo;
		this.testsRepo = testsRepo;
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> prioritizeTests(String project)
			throws NoSuchProjectException {
		BayesPrioritizer prioritizer = new BayesPrioritizer(testsRepo,
				changesRepo);
		return prioritizer.orderTest(buildsRepo.getChanges(project), project);
	}

	@Override
	@Transactional
	public void processChanges(BufferedReader diffFile, String project)
			throws IOException {
		RevisionParser revParser = new RevisionParser(linesRepo, project);
		buildsRepo.saveChanges(project, revParser.processChanges(diffFile));
	}

	@Override
	@Transactional
	public void processTestResults(BufferedReader testResults, String project)
			throws FileNotFoundException, IOException,
			TestResultsAlreadyProcessedException, NoSuchProjectException {
		// TODO: Don't increment the changes count if a test failed in the
		// previous build as well (as this means the new changes did not cause
		// the test failure).
		buildsRepo.setTestsResultsSaved(project);
		List<Integer> changes = buildsRepo.getChanges(project);
		Map<String, Boolean> testOutcomes = TAPParser
				.parseTestsRaport(testResults);
		testsRepo.saveTestResults(project, testOutcomes);
		Set<String> tests = testOutcomes.keySet();
		Iterator<String> testIt = tests.iterator();
		while (testIt.hasNext()) {
			// For each test, if it failed increment the the "change count" for
			// each line that was changed in the previous build.
			String test = testIt.next();
			if (!testOutcomes.get(test)) {
				Iterator<Integer> lineIt = changes.iterator();
				while (lineIt.hasNext()) {
					changesRepo.incrementChangeCount(project, test,
							lineIt.next());
				}
			}
		}
	}
}
