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
	public int getProbChanged(Integer lineId, String test) {
		String selectQuery = "SELECT (count) FROM changes WHERE "
				+ " test=? AND lineid=?;";
		List<Integer> changes = jdbcTemplate.queryForList(selectQuery,
				new Object[] { test, lineId }, Integer.class);
		if (changes.isEmpty()) {
			return 0;
		}
		return changes.get(0);
	}

	@Override
	public void incrementChangeCount(Integer lineId, String test) {
		String updateSql = "UPDATE changes SET count=count + 1 "
				+ "WHERE test=? AND lineid=?;";
		int rowsModif = jdbcTemplate.update(updateSql, new Object[] { test,
				lineId });

		if (rowsModif < 1) {
			jdbcTemplate.update("INSERT INTO changes (test, lineid, count)"
					+ " VALUES (?, ?, 1)", test, lineId);
		}
	}

	@Override
	public void deleteChangesForLineId(Integer lineId) {
		jdbcTemplate.update("DELETE FROM changes WHERE lineid=?;", lineId);
	}
}
