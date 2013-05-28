package com.testprioritization.service.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.testprioritization.domain.dao.ChangesRepository;
import com.testprioritization.domain.dao.LinesRepository;
import com.testprioritization.domain.model.Line;

// A line in a file can either be added, changed, deleted or stay the same.
enum ChangeType {
	ADD, CHANGE, DELETE, NOCHANGE
};

// Class used for parsing unified diffs.
public class RevisionParser {
	// Pattern used to match the range information which is of the form
	// |@@ -l,s +l,s @@| where l is the starting line and s is the number of
	// lines the change hunk applies to for each respective file.
	static Pattern RANGE_INFO = Pattern
			.compile("^@@\\s\\-(\\d*),*(\\d*)\\s\\+(\\d*),*(\\d*)\\s@@");
	// How similar two lines should be so that one is considered to be obtained
	// by changing the other.
	private static final double similarityThreshold = 0.4;
	LinesRepository linesRepo;
	ChangesRepository changesRepo;

	String project;

	public RevisionParser(LinesRepository linesRepo, String project,
			ChangesRepository changesRepo) {
		this.linesRepo = linesRepo;
		this.project = project;
		this.changesRepo = changesRepo;
	}

	// Processed the changes in the diff file.
	public List<Integer> processChanges(BufferedReader diffFileReader)
			throws IOException {
		List<Integer> changedLines = new ArrayList<Integer>();
		String line;
		while ((line = diffFileReader.readLine()) != null) {
			// Match the two-line header for each changed file.
			// The original file is preceded by "---" and the new file is
			// preceded by "+++". eg:
			// |--- a/path/to/original ''timestamp''|
			// |+++ b/path/to/new ''timestamp''|
			if (line.startsWith("--- ")) {
				// Extract the file path by removing the "a/" prefix and
				// discarding the timestamp.
				// TODO: Some diff files do no have these prefixes for file
				// paths.
				String originalFile = line.substring(line.indexOf('/') + 1,
						line.length()).split("\\t")[0];
				line = diffFileReader.readLine();
				if (line == null) {
					break;
				}
				if (!line.startsWith("+++ ")) {
					continue;
				}
				String newFile;
				if (!line.contains("/")) {
					// This deals with the case when the file is deleted and we
					// don't want |newFile| to be the empty string.
					newFile = originalFile;
				} else {
					// Remove the "/b" prefix, discard timestamp.
					newFile = line.substring(line.indexOf('/') + 1,
							line.length()).split("\\t|\\s")[0];
				}
				if (!newFile.isEmpty()) {
					changedLines.addAll(getChangedLinesInFile(diffFileReader,
							newFile));
				}
			}
		}
		return changedLines;
	}

	// Processes the hunk corresponding to the changes made to a file in the
	// project and returns the lines changed in that file.
	private List<Integer> getChangedLinesInFile(BufferedReader diffFileReader,
			String file) throws IOException {
		List<Integer> linesChanged = new ArrayList<Integer>();
		String line = diffFileReader.readLine();
		if (line == null) {
			return linesChanged;
		}
		// Match the range information.
		Matcher m = RANGE_INFO.matcher(line);
		m.find();
		if (m.groupCount() < 4) {
			return linesChanged;
		}
		// The length of the hunks is 1 by default, unless otherwise specified
		// in the range information.
		// Start line no and length of the chunk corresponding to the original
		// file.
		int origStart = Integer.parseInt(m.group(1));
		int origLength = 1;
		if (!m.group(2).isEmpty()) {
			origLength = Integer.parseInt(m.group(2));
		}
		// Start line no and length of the chunk corresponding to the new file.
		int newStart = Integer.parseInt(m.group(3));
		int newLength = 1;
		if (!m.group(4).isEmpty()) {
			newLength = Integer.parseInt(m.group(4));
		}
		line = line.substring(m.end(), line.length());
		List<List<Line>> addedParagraphs = new ArrayList<List<Line>>();
		List<List<Line>> deleteParagraphs = new ArrayList<List<Line>>();
		List<Line> currentParagraph = new ArrayList<Line>();
		ChangeType changeType = null;
		// The number of lines deleted/added/unchanged.
		int deletedCt = 0, addedCt = 0, unchangedCt = 0;
		while (unchangedCt + deletedCt < origLength
				|| unchangedCt + addedCt < newLength) {
			// Lines starting with empty space remain unchanged.
			int origLineNo = unchangedCt + deletedCt + origStart;
			int newLineNo = unchangedCt + addedCt + newStart;
			if (line.startsWith(" ")) {
				Line newLine = new Line(newLineNo, line.substring(1), file,
						project);
				linesRepo.replaceLine(origLineNo, newLine);
				if (changeType == ChangeType.ADD) {
					// This is the first unchanged line after an added
					// paragraph. Add the paragraph to the |addedParagraphs|
					// lists and start a new paragraph.
					addedParagraphs.add(currentParagraph);
					currentParagraph = new ArrayList<Line>();
				} else if (changeType == ChangeType.DELETE) {
					// Save the deleted paragraph and start a new one.
					deleteParagraphs.add(currentParagraph);
					currentParagraph = new ArrayList<Line>();
				}
				unchangedCt++;
				changeType = ChangeType.NOCHANGE;
			} else if (line.startsWith("-")) {
				// Lines starting with "-" are removed.
				Line origLine = new Line(origLineNo, line.substring(1), file,
						project);
				if (changeType == ChangeType.ADD) {
					// First deleted line after an added paragraph.
					addedParagraphs.add(currentParagraph);
					currentParagraph = new ArrayList<Line>();
				}
				deletedCt++;
				currentParagraph.add(origLine);
				changeType = ChangeType.DELETE;
			} else if (line.startsWith("+")) {
				// Lines starting with "+" are removed.
				Line newLine = new Line(newLineNo, line.substring(1), file,
						project);
				if (changeType == ChangeType.DELETE) {
					// A deleted paragraph just ended.
					deleteParagraphs.add(currentParagraph);
					currentParagraph = new ArrayList<Line>();
				}
				addedCt++;
				currentParagraph.add(newLine);
				changeType = ChangeType.ADD;
			}
			line = diffFileReader.readLine();
		}
		if (changeType == ChangeType.DELETE) {
			deleteParagraphs.add(currentParagraph);
		} else if (changeType == ChangeType.ADD) {
			addedParagraphs.add(currentParagraph);
		}
		// Shift the remaining lines in the file.
		linesRepo.shiftLines(origStart + origLength, addedCt - deletedCt, file,
				project);
		// Compute the similarities between the added and removed paragraphs.
		List<Double> similarities = computeSimilarities(addedParagraphs,
				deleteParagraphs);
		// Sort the similarity measures in descending order.
		ListIndexComparator comparator = new ListIndexComparator(similarities);
		Integer[] indeces = comparator.createIndexArray();
		Arrays.sort(indeces, comparator);
		boolean[] usedAddedParagraphs = new boolean[addedParagraphs.size()];
		boolean[] usedDeletedParagraphs = new boolean[deleteParagraphs.size()];
		for (Integer i : indeces) {
			int addedParagraphIndex = i / deleteParagraphs.size();
			int deletedParagraphIndex = i % deleteParagraphs.size();
			if (!usedAddedParagraphs[addedParagraphIndex]
					&& !usedDeletedParagraphs[deletedParagraphIndex]) {
				usedAddedParagraphs[addedParagraphIndex] = true;
				usedDeletedParagraphs[deletedParagraphIndex] = true;
				// Compute the lines in the addedParagraph that could be
				// obtained
				// by changing lines in the deletedParagraph.
				linesChanged.addAll(getChangedLinesInParagraphs(
						deleteParagraphs.get(deletedParagraphIndex),
						addedParagraphs.get(addedParagraphIndex)));
			}
		}
		// Add to linesChanged the remaining lines that are not obtained from
		// changes to the original lines.
		// Remove the deleted lines from the repository.
		for (Integer i : indeces) {
			int addedParagraphIndex = i / deleteParagraphs.size();
			int deletedParagraphIndex = i % deleteParagraphs.size();
			Iterator<Line> lineIt;
			if (!usedAddedParagraphs[addedParagraphIndex]) {
				// If this added paragraph haven't been used to compute changed
				// lines, just add all its lines to the |linesChanged|.
				lineIt = addedParagraphs.get(addedParagraphIndex).iterator();
				while (lineIt.hasNext()) {
					int lineId = linesRepo.addLine(lineIt.next());
					linesChanged.add(new Integer(lineId));
				}
				usedAddedParagraphs[addedParagraphIndex] = true;
			} else if (!usedDeletedParagraphs[deletedParagraphIndex]) {
				lineIt = deleteParagraphs.get(deletedParagraphIndex).iterator();
				while (lineIt.hasNext()) {
					deleteLine(lineIt.next());
				}
				usedDeletedParagraphs[deletedParagraphIndex] = true;
			}
		}
		return linesChanged;
	}

	private List<Integer> getChangedLinesInParagraphs(List<Line> deletedLines,
			List<Line> addedLines) {
		List<Integer> linesChanged = new ArrayList<Integer>();
		double[] lineSimilarities = new double[deletedLines.size()
				* addedLines.size()];
		int i = 0;
		Iterator<Line> deletedLinesIt = deletedLines.iterator();
		// Compute the (Levenshtein distance) similarity measures between all
		// possible pairs of added-deleted lines in the paragraphs.
		while (deletedLinesIt.hasNext()) {
			Line deletedLine = deletedLinesIt.next();
			Iterator<Line> addLineIt = addedLines.iterator();
			while (addLineIt.hasNext()) {
				Line addedLine = addLineIt.next();
				int maxLength = Math.max(
						deletedLine.getLineContents().length(), addedLine
								.getLineContents().length());
				double levenshteinDist = SimilarityMeasures.levenshteinDist(
						deletedLine, addedLine);
				lineSimilarities[i++] = (maxLength - levenshteinDist)
						/ maxLength;
			}
		}
		double candidateSim = 0;
		int lastAdd = -1, lastDel = -1, addedCt = 0, deletedCt = 0, changedCt = 0;
		while (deletedCt + changedCt < deletedLines.size()
				&& addedCt + changedCt < addedLines.size()) {
			// |maxDist| - maximum distance (in lines) between the last pair of
			// lines considered to be similar (one line obtained by changing the
			// other).
			double maxDist = (deletedLines.size() + addedLines.size());
			candidateSim = 0;
			for (int j = 0; j < lineSimilarities.length; j++) {
				int addLineIdx = j % addedLines.size();
				int delLineIdx = j / addedLines.size();
				double currDist = (addLineIdx + delLineIdx) / 2;
				if (addLineIdx > lastAdd && delLineIdx > lastDel) {
					if (lineSimilarities[j] == candidateSim
							&& currDist < maxDist
							|| lineSimilarities[j] > candidateSim) {
						maxDist = currDist;
						candidateSim = lineSimilarities[j];
						lastAdd = addLineIdx;
						lastDel = delLineIdx;
					}
				}
			}

			if (candidateSim >= similarityThreshold) {
				int lineId = linesRepo.replaceLine(deletedLines.get(lastDel)
						.getLineNo(), addedLines.get(lastAdd));
				linesChanged.add(new Integer(lineId));
				changedCt++;
			} else {
				int lineId = linesRepo.addLine(addedLines.get(lastAdd));
				linesChanged.add(new Integer(lineId));
				deleteLine(deletedLines.get(lastDel));
				addedCt++;
				deletedCt++;
			}
			while (addedCt + changedCt <= lastAdd) {
				int lineId = linesRepo.addLine(addedLines.get(addedCt
						+ changedCt - 1));
				linesChanged.add(new Integer(lineId));
				addedCt++;
			}
			while (deletedCt + changedCt <= lastDel) {
				deleteLine(deletedLines.get(deletedCt + changedCt - 1));
				deletedCt++;
			}
		}
		while (addedCt + changedCt < addedLines.size()) {
			int lineId = linesRepo.addLine(addedLines.get(addedCt + changedCt));
			linesChanged.add(new Integer(lineId));
			addedCt++;
		}
		while (deletedCt + changedCt < deletedLines.size()) {
			deleteLine(deletedLines.get(deletedCt + changedCt));
			deletedCt++;
		}
		return linesChanged;
	}

	private void deleteLine(Line line) {
		Integer lineId = linesRepo.getLineId(line.getLineNo(), line.getFile(),
				line.getProject());
		changesRepo.deleteChangesForLineId(lineId);
		linesRepo.deleteLine(lineId);
	}

	// Computes the similarities between two sets of paragraphs.
	private static List<Double> computeSimilarities(
			List<List<Line>> addedParagraphs, List<List<Line>> deleteParagraphs) {
		List<Double> similarities = new ArrayList<Double>(
				addedParagraphs.size() * deleteParagraphs.size());
		Iterator<List<Line>> addedIt = addedParagraphs.iterator();
		while (addedIt.hasNext()) {
			List<Line> addedParagraph = addedIt.next();
			Iterator<List<Line>> deletedIt = deleteParagraphs.iterator();
			while (deletedIt.hasNext()) {
				// For each pair of added paragraph - deleted paragraph
				// calculate
				// its cosine similarity.
				List<Line> deletedParagraph = deletedIt.next();
				Map<String, Integer> tokens1 = tokenize(addedParagraph);
				Map<String, Integer> tokens2 = tokenize(deletedParagraph);
				similarities.add(SimilarityMeasures.cosineSimilarity(tokens1,
						tokens2));
			}
		}
		return similarities;
	}

	// Tokenizes a given paragraph given as a set of lines and returns the
	// the tokens and the number of times each token occurs.
	private static Map<String, Integer> tokenize(List<Line> paragraph) {
		Iterator<Line> lineIt = paragraph.iterator();
		Map<String, Integer> tokens = new HashMap<String, Integer>();
		while (lineIt.hasNext()) {
			Scanner scanner = new Scanner(lineIt.next().getLineContents());
			while (scanner.hasNext()) {
				String token = scanner.next();
				Integer occ = tokens.get(token);
				if (occ == null) {
					occ = new Integer(0);
				}
				occ++;
				tokens.put(token, occ);
			}
			scanner.close();
		}
		return tokens;
	}
}

class ListIndexComparator implements Comparator<Integer> {
	private final List<Double> list;

	public ListIndexComparator(List<Double> list) {
		this.list = list;
	}

	@Override
	public int compare(Integer index1, Integer index2) {
		return list.get(index2).compareTo(list.get(index1));
	}

	public Integer[] createIndexArray() {
		Integer[] indexes = new Integer[list.size()];
		for (int i = 0; i < list.size(); i++) {
			indexes[i] = i;
		}
		return indexes;
	}
}
