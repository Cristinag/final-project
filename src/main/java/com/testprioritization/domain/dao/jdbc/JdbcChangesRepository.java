package com.testprioritization.domain.dao.jdbc;

import java.util.List;

import javax.inject.Inject;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.testprioritization.domain.dao.ChangesRepository;

@Repository
public class JdbcChangesRepository implements ChangesRepository {

	private final JdbcTemplate jdbcTemplate;

	@Inject
	public JdbcChangesRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public int getProbChanged(String project, int lineId, String test) {
		String selectQuery = "SELECT (count) FROM changes WHERE project=?"
				+ " AND test=? AND lineid=?;";
		List<Integer> changes = jdbcTemplate.queryForList(selectQuery,
				new Object[] { project, test, lineId }, Integer.class);
		if (changes.isEmpty()) {
			return 0;
		}
		return changes.get(0);
	}

	@Override
	public void incrementChangeCount(String project, String test, int lineId) {
		String updateSql = "UPDATE changes SET count=count + 1 WHERE project=?"
				+ " AND test=? AND lineid=?;";
		int rowsModif = jdbcTemplate.update(updateSql, new Object[] { project,
				test, lineId });

		if (rowsModif < 1) {
			jdbcTemplate.update(
					"INSERT INTO changes (project, test, lineid, count)"
							+ " VALUES (?, ?, ?, 1)", project, test, lineId);
		}
	}
}
