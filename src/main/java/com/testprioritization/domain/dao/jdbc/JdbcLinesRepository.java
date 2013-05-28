package com.testprioritization.domain.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Inject;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import com.testprioritization.domain.dao.LinesRepository;
import com.testprioritization.domain.model.Line;

@Repository
public class JdbcLinesRepository implements LinesRepository {

	private final JdbcTemplate jdbcTemplate;

	@Inject
	public JdbcLinesRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Integer addLine(final Line line) {
		final String sql = "INSERT INTO lines (project, file, lineno, line)"
				+ " VALUES (?, ?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		PreparedStatementCreator psc = new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				PreparedStatement ps = con.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, line.getProject());
				ps.setString(2, line.getFile());
				ps.setInt(3, line.getLineNo());
				ps.setString(4, line.getLineContents());
				return ps;
			}
		};
		jdbcTemplate.update(psc, keyHolder);
		Number lineId = (Number) keyHolder.getKeys().get("lineid");
		if (lineId == null) {
			// In testing the lineId is not assigned.
			return 0;
		}
		return lineId.intValue();
	}

	@Override
	public void deleteLine(Integer lineId) {
		jdbcTemplate.update("DELETE FROM lines WHERE lineid=?;", lineId);
	}

	@Override
	public Integer getLineId(final int lineNo, final String file,
			final String project) {
		final String sql = "SELECT (lineid) FROM lines WHERE "
				+ "project=? AND file=? AND lineno=?;";
		SqlRowSet lineRows = jdbcTemplate.queryForRowSet(sql, new Object[] {
				project, file, lineNo });
		if (lineRows.first()) {
			return lineRows.getInt("lineId");
		}
		return null;
	}

	@Override
	public Integer replaceLine(Integer lineNo, Line newLine) {
		int rowsModif = jdbcTemplate.update(
				"UPDATE lines SET lineno=?, line=? WHERE project=?"
						+ " AND file=? AND lineno=?;", newLine.getLineNo(),
				newLine.getLineContents(), newLine.getProject(),
				newLine.getFile(), lineNo);
		if (rowsModif < 1) {
			return addLine(newLine);
		}
		return getLineId(newLine.getLineNo(), newLine.getFile(),
				newLine.getProject());
	}

	@Override
	public void shiftLines(int startIndex, int shiftAmount, String file,
			String project) {
		String updateSql = "UPDATE lines SET lineno=lineno+?"
				+ " WHERE lineno >= ? and project=? and file=?;";
		jdbcTemplate.update(updateSql, shiftAmount, startIndex, project, file);
	}
}
