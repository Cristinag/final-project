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
	Integer addLine(Line line);

	/**
	 * Removes the line with the given id from the repository.
	 * 
	 * @param lineId
	 *            the id of the line to be removed
	 */
	void deleteLine(Integer lineId);

	/**
	 * Returns the id of a line given its number and the file and project to
	 * which it belongs.
	 * 
	 * @param lineNo
	 *            the number of the line to be retrieved in the file |file|
	 * @param file
	 *            file containing the line to be retrieved
	 * @param project
	 *            project containing |file|
	 * @return line id or null if line not found
	 */
	Integer getLineId(int lineNo, String file, String project);

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

	/**
	 * Replaces the number and contents of the line at number |lineNo| with
	 * those of line |newLine|, or adds |newLine| if no line exists at the given
	 * number.
	 * 
	 * @param lineNo
	 *            the number of the line to be replaced
	 * @param newLine
	 *            the replacing line
	 * @return the id of the new line
	 */
	Integer replaceLine(Integer lineNo, Line newLine);

}
