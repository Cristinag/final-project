package com.testprioritization.domain.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.testprioritization.domain.dao.exceptions.NoSuchProjectException;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:config/test-config.xml")
@ActiveProfiles("dev")
public class BuildsRepositoryFunctionalTest {
	@Autowired
	private BuildsRepository buildsRepo;

	@Test
	public void shouldSaveChanges() {
		String project = "newProject";
		List<Integer> changes = new ArrayList<Integer>();
		changes.add(1);
		changes.add(2);
		try {
			changes = buildsRepo.getChanges(project);
			fail("No NoSuchProjectException");
		} catch (NoSuchProjectException e) {
			// OK, there are no changes for |project|.
		}
		buildsRepo.saveChanges(project, changes);
		assertEquals(buildsRepo.getChanges(project), changes);
	}

	@Test
	public void shouldReturnChanges() {
		String projectInDb = "project2";
		List<Integer> changesInDb = new ArrayList<Integer>();
		changesInDb.add(1);
		changesInDb.add(2);
		List<Integer> changesFound = buildsRepo.getChanges(projectInDb);
		assertThat(changesFound, is(changesInDb));
	}
}
