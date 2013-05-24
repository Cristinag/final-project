package com.testprioritization.domain.dao;

import com.testprioritization.domain.model.Line;

/**
 * LinesRepository contains the lines in all projects. A line is represented by
 * its contents, the file in which it occurs, its number in that file, and the
 * project to which it corresponds.
 */
public interface LinesRepository {

	/**
	 * Adds |line| to the repository.
	 * 
	 * @param line
	 *            the line to be added
	 * @return the id of the added line
	 */
	int addLine(Line line);

	/**
	 * Removes |line| from the repository.
	 * 
	 * @param line
	 *            the line to be removed
	 */
	void deleteLine(Line line);

	/**
	 * Returns a line from the repository given its line number and the file and
	 * project to which it corresponds.
	 * 
	 * @param lineNo
	 *            the number of the line to be retrieved in the file |file|
	 * @param file
	 *            file containing the line to be retrieved
	 * @param project
	 *            project containing |file|
	 * @return line or null if line not found
	 */
	Line getLine(int lineNo, String file, String project);

	/**
	 * Replaces the number and contents of a given line with those of another
	 * line.
	 * 
	 * @param line1
	 *            the line to be replaced
	 * @param line2
	 *            the replacing line
	 * @return the id of the new line
	 */
	int replaceLine(Line line1, Line line2);

	/**
	 * Shifts the lines in the file |file| and project |project| starting at
	 * some number |startLineNo| by a shift amount |shiftAmount|.
	 * 
	 * @param startLineNo
	 *            the line number where the shift starts
	 * @param shiftAmount
	 *            the amount by which to shift the lines starting ad
	 *            |startLineNo|
	 * @param file
	 *            the file in which the shift occurs
	 * @param project
	 *            the project containing |file|
	 */
	void shiftLines(int startLineNo, int shiftAmount, String file,
			String project);
}
