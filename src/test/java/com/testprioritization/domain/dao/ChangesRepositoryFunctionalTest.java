package com.testprioritization.domain.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:config/test-config.xml")
@ActiveProfiles("dev")
public class ChangesRepositoryFunctionalTest {
	private static final int lineIdInDb = 1;
	private static final String testInDb = "test5";
	private static final int countInDb = 4;
	@Autowired
	private ChangesRepository changesRepo;

	@Test
	public void shouldGetProbChanged() {
		int probInDb = changesRepo.getProbChanged(lineIdInDb, testInDb);
		assertThat(probInDb, is(countInDb));
	}

	@Test
	public void shouldIncrementChangeCount() {
		changesRepo.incrementChangeCount(lineIdInDb, testInDb);
		int probInDb = changesRepo.getProbChanged(lineIdInDb, testInDb);
		assertThat(probInDb, is(countInDb + 1));
	}
}
