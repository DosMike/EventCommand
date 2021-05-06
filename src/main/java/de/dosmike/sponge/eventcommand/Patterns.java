package de.dosmike.sponge.eventcommand;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Patterns {
	public static final Predicate<String> allVariableCheck = Pattern.compile("^\\p{L}+$").asPredicate();
	public static final Pattern startNumeric = Pattern.compile("^([-]?[0-9]+(?:\\.[0-9]+)?(?:e[+-]?[0-9]+)?)");
	public static final Pattern startVariable = Pattern.compile("^\\$\\{(\\p{L}+)}");
	public static final Pattern wholeVariable = Pattern.compile("(\\$\\{\\p{L}+})");
	public static final Pattern startDuration = Pattern.compile("^[0-9]+(?:(?:h|m(?:in)?|s(?:ec)?)|(?::[0-9]{2}(?::[0-9]{2})?))");
	public static final Pattern startKeyword = Pattern.compile("^(\\p{L}+)(?:\\b|$)");
	public static final Pattern startSymbols = Pattern.compile("^([^\\p{L}\\p{Digit}\\p{javaWhitespace}]+)");
}
