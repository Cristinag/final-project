package com.testprioritization.domain.dao.jdbc;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import com.testprioritization.domain.dao.BuildsRepository;
import com.testprioritization.domain.dao.exceptions.NoSuchProjectException;
import com.testprioritization.domain.dao.exceptions.TestResultsAlreadyProcessedException;

@Repository
public class JdbcBuildsRepository implements BuildsRepository {

	Environment env;

	private final JdbcTemplate jdbcTemplate;

	@Inject
	public JdbcBuildsRepository(JdbcTemplate jdbcTemplate, Environment env) {
		this.jdbcTemplate = jdbcTemplate;
		this.env = env;
	}

	@Override
	public void saveChanges(final String project, final List<Integer> changes) {
		PreparedStatementCreator updatePsc = new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				PreparedStatement ps = con
						.prepareStatement("UPDATE builds SET "
								+ "tests_processed=false, changes=? "
								+ "WHERE project=?");
				if (Arrays.asList(env.getActiveProfiles()).contains("dev")) {
					// For h2 database.
					ps.setObject(1, changes.toArray());
				} else {
					// For psql database.
					Array sqlArray = con
							.createArrayOf("int", changes.toArray());

					ps.setArray(1, sqlArray);
				}
				ps.setString(2, project);
				return ps;
			}
		};

		PreparedStatementCreator insertPsc = new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				PreparedStatement ps = con
						.prepareStatement("INSERT INTO builds "
								+ "(project, tests_processed, changes) VALUES "
								+ "(?, false, ?)");
				if (Arrays.asList(env.getActiveProfiles()).contains("dev")) {
					// For h2 database.
					ps.setObject(2, changes.toArray());
				} else {
					// For psql database.
					Array sqlArray = con
							.createArrayOf("int", changes.toArray());
					ps.setArray(2, sqlArray);
				}
				ps.setString(1, project);
				return ps;
			}
		};
		if (jdbcTemplate.update(updatePsc) < 1) {
			// This is the first build for the given project.
			jdbcTemplate.update(insertPsc);
		}
	}

	@Override
	public List<Integer> getChanges(String project)
			throws NoSuchProjectException {
		// The changes are represented as an array in the database, so they are
		// retrieved differently in the h2 database (used for testing) than in
		// the psql database.
		if (Arrays.asList(env.getActiveProfiles()).contains("dev")) {
			SqlRowSet changesRows = jdbcTemplate.queryForRowSet(
					"SELECT (changes) FROM builds WHERE project=?;",
					new Object[] { project });
			if (changesRows.first()) {
				Object[] changes = (Object[]) changesRows.getObject("changes");
				List<Integer> changesList = new ArrayList<Integer>();
				for (Object change : changes) {
					changesList.add((Integer) change);
				}
				return changesList;
			} else {
				throw new NoSuchProjectException(project);
			}
		}
		List<Array> changes = jdbcTemplate.queryForList(
				"SELECT changes FROM builds WHERE project=?;",
				new Object[] { project }, Array.class);
		if (changes.size() == 0) {
			throw new NoSuchProjectException(project);
		}
		try {
			Integer[] changesArray = (Integer[]) changes.get(0).getArray();
			return Arrays.asList(changesArray);
		} catch (SQLException e) {
			return new ArrayList<Integer>();
		}
	}

	@Override
	public void setTestsResultsSaved(String project)
			throws NoSuchProjectException, TestResultsAlreadyProcessedException {
		List<Boolean> testsProcessed = jdbcTemplate.queryForList(
				"SELECT (tests_processed) FROM builds WHERE project=?;",
				new Object[] { project }, Boolean.class);
		if (testsProcessed.size() == 0) {
			throw new NoSuchProjectException(project);
		}
		if (testsProcessed.get(0)) {
			throw new TestResultsAlreadyProcessedException(project);
		}
		jdbcTemplate.update(
				"UPDATE builds SET tests_processed=true WHERE project=?;",
				project);
	}
}
