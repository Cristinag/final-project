package com.testprioritization.domain.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.testprioritization.domain.model.Line;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:config/test-config.xml")
@ActiveProfiles("dev")
public class LinesRepositoryFunctionalTest {
	private static final String projectInDb = "project3";
	private static final Line lineInDb = new Line(1, "line test", "file",
			projectInDb, 1);
	private static final Line newLine = new Line(4, "this is a new line",
			"file", projectInDb, 2);

	@Autowired
	private LinesRepository linesRepo;

	@Test
	public void shouldGetLine() {
		assertEquals(lineInDb.getLineId(),
				linesRepo.getLineId(1, lineInDb.getFile(), projectInDb));
	}

	@Test
	@Transactional
	public void shouldDeleteLine() {
		assertNotNull(linesRepo.getLineId(lineInDb.getLineNo(),
				lineInDb.getFile(), projectInDb));
		linesRepo.deleteLine(lineInDb.getLineId());
		assertNull(linesRepo.getLineId(lineInDb.getLineNo(),
				lineInDb.getFile(), projectInDb));
	}

	@Test
	@Transactional
	public void shouldAddLine() {
		Integer storedLineId = linesRepo.getLineId(newLine.getLineNo(),
				newLine.getFile(), projectInDb);
		assertNull(storedLineId);
		linesRepo.addLine(newLine);
		storedLineId = linesRepo.getLineId(newLine.getLineNo(),
				newLine.getFile(), projectInDb);
		assertNotNull(storedLineId);
	}

	@Test
	@Transactional
	public void shouldShiftLines() {
		int shiftAmount = 2;
		linesRepo.shiftLines(lineInDb.getLineNo(), shiftAmount,
				lineInDb.getFile(), projectInDb);
		Integer updatedLineId = linesRepo.getLineId(lineInDb.getLineNo()
				+ shiftAmount, lineInDb.getFile(), projectInDb);
		assertEquals(updatedLineId, lineInDb.getLineId());
	}

	@Test
	@Transactional
	public void shouldReplaceLine() {
		linesRepo.replaceLine(lineInDb.getLineNo(), newLine);
		assertNull(linesRepo.getLineId(lineInDb.getLineNo(),
				lineInDb.getFile(), projectInDb));
		assertNotNull(linesRepo.getLineId(newLine.getLineNo(),
				newLine.getFile(), projectInDb));
	}
}
