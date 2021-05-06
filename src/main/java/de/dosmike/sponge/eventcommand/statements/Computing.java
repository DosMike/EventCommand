package de.dosmike.sponge.eventcommand.statements;

import de.dosmike.sponge.eventcommand.MathEval;
import de.dosmike.sponge.eventcommand.Patterns;
import de.dosmike.sponge.eventcommand.Utils;
import de.dosmike.sponge.eventcommand.VariableContext;
import de.dosmike.sponge.eventcommand.exception.StatementParseException;

import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Computing {

	static final MathEval maths = new MathEval();
	String mathExpr;
	String variableTarget;

	public Computing(String line) {

		if (!line.toLowerCase(Locale.ROOT).startsWith("let "))
			throw new StatementParseException("Expected 'let' for mathematics");
		line = line.substring(4).trim();
		int idx = Utils.firstIndexOf(line, " :=",0);
		if (idx == 0) throw new StatementParseException("Expected variable in mathematics, found assignment operator");
		if (idx < 0) throw new StatementParseException("Unexpected end of line, expected assignment operator");

		variableTarget = line.substring(0,idx);
		if (!Patterns.allVariableCheck.test(variableTarget)) throw new StatementParseException("Variables names may only contain letters");

		line = line.substring(idx).trim();

		if (line.startsWith("be")) {
			if (line.length()<3) throw new StatementParseException("Unexpected end of line, expected math expression");
			if (line.charAt(2)!=' ') throw new StatementParseException("Expected space after assignment keyword");
			line = line.substring(2).trim();
			mathExpr = line;
		} else if (line.startsWith(":=")) {
			if (line.length()<3) throw new StatementParseException("Unexpected end of line, expected math expression");
			mathExpr = line.substring(2).trim();
		} else if (line.startsWith("=")) {
			if (line.length()<2) throw new StatementParseException("Unexpected end of line, expected math expression");
			mathExpr = line.substring(1).trim();
		}

	}

	void mutateState(VariableContext variables) {
		variables.put(variableTarget, maths.evaluate(variables.resolveVariablesNumeric(mathExpr)));
	}

}
