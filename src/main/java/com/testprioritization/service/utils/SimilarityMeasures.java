package com.testprioritization.service.utils;

import java.util.Iterator;
import java.util.Map;

import com.testprioritization.domain.model.Line;

public class SimilarityMeasures {

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	// Calculates the Levenshtein distance between the given lines.
	public static int levenshteinDist(Line line1, Line line2) {
		String str1 = line1.getLineContents();
		String str2 = line2.getLineContents();
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++) {
			distance[i][0] = i;
		}
		for (int j = 1; j <= str2.length(); j++) {
			distance[0][j] = j;
		}

		for (int i = 1; i <= str1.length(); i++) {
			for (int j = 1; j <= str2.length(); j++) {
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]
								+ ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
										: 1));
			}
		}

		return distance[str1.length()][str2.length()];
	}

	// Calculates the cosinge similarity between the given bags of tokens.
	public static Double cosineSimilarity(Map<String, Integer> tokens1,
			Map<String, Integer> tokens2) {
		if (tokens1.isEmpty() && tokens2.isEmpty()) {
			return 1.0;
		}
		if (tokens1.isEmpty() || tokens2.isEmpty()) {
			return 0.0;
		}
		Iterator<String> tokensIt = tokens1.keySet().iterator();
		Double norm1 = 0.0, norm2 = 0.0, inner = 0.0;
		while (tokensIt.hasNext()) {
			String token = tokensIt.next();
			Integer occ1 = tokens1.get(token);
			norm1 += occ1 * occ1;
			Integer occ2 = tokens2.get(token);
			if (occ2 != null) {
				inner += occ1 * occ2;
			}
		}
		tokensIt = tokens2.keySet().iterator();
		while (tokensIt.hasNext()) {
			Integer occ2 = tokens2.get(tokensIt.next());
			norm2 += occ2 * occ2;
		}
		return inner / (Math.sqrt(norm1) * Math.sqrt(norm2));
	}
}
