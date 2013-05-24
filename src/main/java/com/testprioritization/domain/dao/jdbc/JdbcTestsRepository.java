package com.testprioritization.domain.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.testprioritization.domain.dao.TestsRepository;
import com.testprioritization.domain.model.Test;

@Repository
public class JdbcTestsRepository implements TestsRepository {
	private final JdbcTemplate jdbcTemplate;

	@Inject
	public JdbcTestsRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void saveTestResults(String projectName, Map<String, Boolean> testOutcomes) {
		String insertSql = "INSERT INTO tests (project, test, runs, failures) "
				+ "SELECT ?, ?, 0, 0 WHERE NOT EXISTS "
				+ "(SELECT 1 FROM tests WHERE project=? AND test=?);";
		String updateSql = "UPDATE tests SET runs=runs + 1, failures=failures + ?"
				+ " WHERE project=?  AND test=?;";
		Iterator<String> testsIt = testOutcomes.keySet().iterator();
		while (testsIt.hasNext()) {
			String test = testsIt.next();
			jdbcTemplate
					.update(insertSql, projectName, test, projectName, test);
			Integer increment = 0;
			if (!testOutcomes.get(test)) {
				increment = 1;
			}
			jdbcTemplate.update(updateSql, increment, projectName, test);
		}
	}

	@Override
	public List<Test> getTests(String project) {
		return jdbcTemplate.query("SELECT * FROM tests WHERE project='"
				+ project + "';", new RowMapper<Test>() {
			@Override
			public Test mapRow(ResultSet rs, int rowNum) throws SQLException {
				Test test = new Test(rs.getString("test"), rs.getInt("runs"),
						rs.getInt("failures"));
				return test;
			}
		});
	}
}
