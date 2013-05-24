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
	public int addLine(final Line line) {
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
	public void deleteLine(Line line) {
		jdbcTemplate.update("DELETE FROM lines WHERE project=?"
				+ " AND file=? AND lineno=?;", line.getProject(),
				line.getFile(), line.getLineNo());
	}

	@Override
	public Line getLine(final int lineNo, final String file,
			final String project) {
		final String sql = "SELECT * FROM lines WHERE "
				+ "project=? AND file=? AND lineno=?;";
		SqlRowSet lineRows = jdbcTemplate.queryForRowSet(sql, new Object[] {
				project, file, lineNo });
		if (lineRows.first()) {
			return new Line(lineNo, lineRows.getString("line"), file, project,
					lineRows.getInt("lineId"));
		}
		return null;
	}

	@Override
	public int replaceLine(Line oldLine, Line newLine) {
		if (oldLine == null) {
			return addLine(newLine);
		}
		int rowsModif = jdbcTemplate.update(
				"UPDATE lines SET lineno=?, line=? WHERE project=?"
						+ " AND file=? AND lineno=?;", newLine.getLineNo(),
				newLine.getLineContents(), oldLine.getProject(),
				oldLine.getFile(), oldLine.getLineNo());
		if (rowsModif < 1) {
			// |oldLine| was not stored, just add |newLine|.
			return addLine(newLine);
		}
		return getLine(newLine.getLineNo(), oldLine.getFile(),
				oldLine.getProject()).getLineId();
	}

	@Override
	public void shiftLines(int startIndex, int shiftAmount, String file,
			String project) {
		String updateSql = "UPDATE lines SET lineno=lineno+?"
				+ " WHERE lineno >= ? and project=? and file=?;";
		jdbcTemplate.update(updateSql, shiftAmount, startIndex, project, file);
	}
}
