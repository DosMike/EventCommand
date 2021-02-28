package de.dosmike.sponge.eventcommand;

import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Computing {

	static final MathEval maths = new MathEval();
	String mathExpr;
	String variableTarget;
	private static final Predicate<String> onlyLetters = Pattern.compile("^\\p{L}+$").asPredicate();

	public Computing(String line) {

		if (!line.toLowerCase(Locale.ROOT).startsWith("let "))
			throw new IllegalArgumentException("Expected 'let' for mathematics");
		line = line.substring(4).trim();
		int idx = first(line, " :=");
		if (idx == 0) throw new IllegalArgumentException("Expected variable in mathematics, found assignment operator");
		if (idx < 0) throw new IllegalArgumentException("Unexpected end of line, expected assignment operator");

		variableTarget = line.substring(0,idx);
		if (!onlyLetters.test(variableTarget)) throw new IllegalArgumentException("Variables names may only contain letters");

		line = line.substring(idx).trim();

		if (line.startsWith("be")) {
			if (line.length()<3) throw new IllegalArgumentException("Unexpected end of line, expected math expression");
			if (line.charAt(2)!=' ') throw new IllegalArgumentException("Expected space after assignment keyword");
			line = line.substring(2).trim();
			mathExpr = line;
		} else if (line.startsWith(":=")) {
			if (line.length()<3) throw new IllegalArgumentException("Unexpected end of line, expected math expression");
			mathExpr = line.substring(2).trim();
		} else if (line.startsWith("=")) {
			if (line.length()<2) throw new IllegalArgumentException("Unexpected end of line, expected math expression");
			mathExpr = line.substring(1).trim();
		}

	}

	private int first(String haystack, String needles) {
		char[] nx = needles.toCharArray();
		int min = Integer.MAX_VALUE;
		boolean found = false;
		for (char c : nx) {
			int at = haystack.indexOf(c);
			if (at>=0) {
				found = true;
				if (at < min) min = at;
			}
		}
		return found ? min : -1;
	}

	void mutateState(Map<String,Object> variables) {
		variables.put(variableTarget, maths.evaluate(Utils.resolveVariablesNumeric(mathExpr, variables)));
	}

}
