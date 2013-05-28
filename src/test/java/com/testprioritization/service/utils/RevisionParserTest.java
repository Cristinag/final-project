package com.testprioritization.service.utils;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.testprioritization.domain.dao.ChangesRepository;
import com.testprioritization.domain.dao.LinesRepository;
import com.testprioritization.domain.model.Line;

@RunWith(MockitoJUnitRunner.class)
public class RevisionParserTest {
	private RevisionParser parser;

	@Mock
	private LinesRepository linesRepo;

	@Mock
	private ChangesRepository changesRepo;

	private static final String project = "project";
	private static final String diffFilePath = "src/test/resources/test-data/diff.txt";
	private static final String changedFileInDiff = "path/to/file/changed.txt";

	@Before
	public void setup() {
		parser = new RevisionParser(linesRepo, project, changesRepo);
	}

	private void mockGetLine() {
		given(
				linesRepo.getLineId(Mockito.anyInt(), Mockito.anyString(),
						Mockito.anyString())).willReturn(1);
	}

	@Test
	public void shouldUpdatesContentsOfUnchangedLines() throws IOException {
		mockGetLine();
		BufferedReader br = new BufferedReader(new FileReader(diffFilePath));
		parser.processChanges(br);
		br.close();
		Mockito.verify(linesRepo).replaceLine(Mockito.eq(248),
				Mockito.argThat(new LineNumberArgumentMatcher(248)));
		Mockito.verify(linesRepo).replaceLine(Mockito.eq(250),
				Mockito.argThat(new LineNumberArgumentMatcher(250)));
		Mockito.verify(linesRepo).replaceLine(Mockito.eq(253),
				Mockito.argThat(new LineNumberArgumentMatcher(254)));
		Mockito.verify(linesRepo).replaceLine(Mockito.eq(254),
				Mockito.argThat(new LineNumberArgumentMatcher(257)));
	}

	@Test
	public void shouldDiscoverChangedLines() throws IOException {
		Integer lineAddedId = 3;
		new Line(249, "changed line content 1(line: 249)", changedFileInDiff,
				project);
		new Line(249, "changed line content 2(line: 249)", changedFileInDiff,
				project);
		given(
				linesRepo.replaceLine(Mockito.eq(249),
						Mockito.argThat(new LineNumberArgumentMatcher(249))))
				.willReturn(lineAddedId);
		mockGetLine();
		BufferedReader br = new BufferedReader(new FileReader(diffFilePath));
		List<Integer> changes = parser.processChanges(br);
		br.close();
		assertTrue(changes.contains(lineAddedId));
	}

	@Test
	public void shouldShiftLines() throws IOException {
		mockGetLine();
		BufferedReader br = new BufferedReader(new FileReader(diffFilePath));
		parser.processChanges(br);
		br.close();
		Mockito.verify(linesRepo)
				.shiftLines(255, 3, changedFileInDiff, project);
	}

	@Test
	public void shouldDeleteLines() throws IOException {
		mockGetLine();
		BufferedReader br = new BufferedReader(new FileReader(diffFilePath));
		parser.processChanges(br);
		br.close();
		Mockito.verify(linesRepo, Mockito.times(2))
				.deleteLine(Mockito.anyInt());
	}

	@Test
	public void shouldAddNewLines() throws IOException {
		mockGetLine();
		BufferedReader br = new BufferedReader(new FileReader(diffFilePath));
		parser.processChanges(br);
		br.close();
		Mockito.verify(linesRepo, Mockito.times(5)).addLine(
				Mockito.any(Line.class));
	}
}

class LineArgumentMatcher extends ArgumentMatcher<Line> {
	Line line;

	public LineArgumentMatcher(Line thisObject) {
		this.line = thisObject;
	}

	@Override
	public boolean matches(Object argument) {
		if (!(argument instanceof Line)) {
			return false;
		}
		return line.equals(argument);
	}
}

class LineNumberArgumentMatcher extends ArgumentMatcher<Line> {
	int lineNo;

	public LineNumberArgumentMatcher(int lineNo) {
		this.lineNo = lineNo;
	}

	@Override
	public boolean matches(Object argument) {
		if (!(argument instanceof Line)) {
			return false;
		}
		return lineNo == ((Line) argument).getLineNo();
	}
}
