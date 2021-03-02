package de.dosmike.sponge.eventcommand.statements;

import de.dosmike.sponge.eventcommand.Utils;
import de.dosmike.sponge.eventcommand.VariableContext;
import de.dosmike.sponge.eventcommand.exception.StatementParseException;
import de.dosmike.sponge.eventcommand.exception.VariableTypeException;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterFactory {

	static class PlayerCooldown implements Filter {
		long duration;
		String variable;
		PlayerCooldown(String playerVariable, String duration) {
			this.variable = playerVariable;
			this.duration = parseDuration(duration);
		}
		@Override
		public boolean test(VariableContext variables, Filtered ruleSet) {
			Object vvalue = variables.require(variable);
			UUID player = Utils.toPlayerUUID(vvalue);
			if (player == null) throw new VariableTypeException("Could not find a player from the value \""+vvalue.toString()+"\" of variable \""+variable+"\"");

			//clean up cooldowns
			long now = System.currentTimeMillis()/1000L;
			List<UUID> expired = new LinkedList<>();
			for (Map.Entry<UUID,Long> e : ruleSet.clientCD.entrySet()) {
				if (e.getValue()+duration >= now) expired.add(e.getKey());
			}
			expired.forEach(uuid->ruleSet.clientCD.remove(uuid));

			//putIfAbsent returns the current value. if there is no current value (==null) there is no cooldown and the test passes
			//if the player is already on cooldown the value will not be replaced.
			return ruleSet.clientCD.putIfAbsent(player, now) == null;
		}
	}
	static class GlobalCooldown implements Filter {
		long duration;
		GlobalCooldown(String duration) {
			this.duration = parseDuration(duration);
		}
		@Override
		public boolean test(VariableContext variables, Filtered ruleSet) {
			long now = System.currentTimeMillis()/1000L;
			if (ruleSet.globalCD+duration < now) return false;
			ruleSet.globalCD = now;
			return true;
		}
	}
	static class PlayerPermission implements Filter {
		String permission;
		String variable;
		PlayerPermission(String playerVariable, String permission) {
			this.variable = playerVariable;
			this.permission = permission;
		}
		@Override
		public boolean test(VariableContext variables, Filtered ruleSet) {
			Object vvalue = variables.require(variable);
			UUID player = Utils.toPlayerUUID(vvalue);
			if (player == null) throw new VariableTypeException("Could not find a player from the value \""+vvalue.toString()+"\" of variable \""+variable+"\"");

			return Sponge.getServer().getPlayer(player).orElseThrow(()->new IllegalStateException("Player for UUID \""+player.toString()+"\" seems to be currently offline")).hasPermission(permission);
		}
	}
	static class NumericCondition implements Filter {
		Double leftAsNumber, rightAsNumber;
		Token leftToken, rightToken;
		BiPredicate<Double,Double> comp;
		NumericCondition(Token left, String comparator, Token right) {
			if (left.type != Type.NUMERIC && left.type != Type.VARIABLE)
				throw new StatementParseException("Numeric conditions left hand was not a number or variable");
			if (right.type != Type.NUMERIC && right.type != Type.VARIABLE)
				throw new StatementParseException("Numeric conditions right hand was not a number or variable");
			if (left.type == Type.NUMERIC && right.type == Type.NUMERIC)
				throw new StatementParseException("Numeric conditions is constant. Please resolve manually!");
			this.leftToken = left;
			this.rightToken = right;
			this.leftAsNumber = left.type == Type.NUMERIC ? Double.parseDouble(left.srep) : null;
			this.rightAsNumber = right.type == Type.NUMERIC ? Double.parseDouble(right.srep) : null;

			this.comp = getComparator(comparator);
		}
		@Override
		public boolean test(VariableContext variables, Filtered ruleSet) {
			Double left, right;
			if (leftToken.type == Type.NUMERIC) {
				left = leftAsNumber;
			} else {
				Object vvalue = variables.require(leftToken.srep);
				try { left = Utils.toDouble(vvalue); }
				catch (IllegalArgumentException e) { throw new VariableTypeException("The Variable \""+leftToken.srep+"\" is not numeric (value: ``"+vvalue.toString()+"``)"); }
			}
			if (rightToken.type == Type.NUMERIC) {
				right = rightAsNumber;
			} else {
				Object vvalue = variables.require(rightToken.srep);
				try { right = Utils.toDouble(vvalue); }
				catch (IllegalArgumentException e) { throw new VariableTypeException("The Variable \""+rightToken.srep+"\" is not numeric (value: ``"+vvalue.toString()+"``)"); }
			}
			return comp.test(left,right);
		}
		static BiPredicate<Double,Double> getComparator(String fromSymbol) {
			switch (fromSymbol) {
				case "=":
				case "==":
					return (a, b) -> a.compareTo(b) == 0;
				case "<=":
					return (a, b) -> a.compareTo(b) <= 0;
				case ">=":
					return (a, b) -> a.compareTo(b) >= 0;
				case "!=":
				case "<>": //vb style, i like it :P
					return (a, b) -> a.compareTo(b) != 0;
				case "<":
					return (a, b) -> a.compareTo(b) < 0;
				case ">":
					return (a, b) -> a.compareTo(b) > 0;
				default:
					throw new StatementParseException("Unknown numeric comparator");
			}
		}
	}
	static class StringCondition implements Filter {
		Token leftToken, rightToken;
		BiPredicate<String,String> comp;
		StringCondition(Token left, String comparator, Token right) {
			if (left.type != Type.STRING && left.type != Type.VARIABLE)
				throw new StatementParseException("Numeric conditions left hand was not a number or variable");
			if (right.type != Type.STRING && right.type != Type.VARIABLE)
				throw new StatementParseException("Numeric conditions right hand was not a number or variable");
			if (left.type == Type.STRING && right.type == Type.STRING)
				throw new StatementParseException("Numeric conditions is constant. Please resolve manually!");
			this.leftToken = left;
			this.rightToken = right;

			this.comp = getComparator(comparator);
		}
		@Override
		public boolean test(VariableContext variables, Filtered ruleSet) {
			String left, right;
			if (leftToken.type == Type.STRING) {
				left = leftToken.srep;
			} else {
				left = variables.require(leftToken.srep).toString();
			}
			if (rightToken.type == Type.STRING) {
				right = rightToken.srep;
			} else {
				right = variables.require(rightToken.srep).toString();
			}
			return comp.test(left,right);
		}
		static BiPredicate<String,String> getComparator(String fromSymbol) {
			switch (fromSymbol) {
				case "=":
				case "==":
					return String::equalsIgnoreCase;
				case "===":
					return String::equals;
				case "<=":
					return (a, b) -> a.compareTo(b) <= 0;
				case ">=":
					return (a, b) -> a.compareTo(b) >= 0;
				case "!=":
				case "<>": //vb style, i like it :P
					return (a, b) -> a.compareTo(b) != 0;
				case "<":
					return (a, b) -> a.compareTo(b) < 0;
				case ">":
					return (a, b) -> a.compareTo(b) > 0;
				case "matches":
					return String::matches;
				default:
					throw new StatementParseException("Unknown string comparator");
			}
		}
	}
	static class VariableCondition implements Filter {
		String leftVar, rightVar;
		String cmp;
		VariableCondition(String leftVar, String comparator, String rightVar) {
			this.leftVar = leftVar;
			this.rightVar = rightVar;
			switch (comparator) {
				case "=":
				case "==":
				case "===":
				case "<=":
				case ">=":
				case "!=":
				case "<>": //vb style, i like it :P
				case "<":
				case ">":
				case "matches":
					cmp = comparator;
					break;
				default:
					throw new StatementParseException("Unknown value comparator");
			}
		}
		@Override
		public boolean test(VariableContext variables, Filtered ruleSet) {
			Object left, right;
			left = variables.require(leftVar);
			right = variables.require(rightVar);

			try {
				Double lad = Utils.toDouble(left), rad = Utils.toDouble(right);
				return NumericCondition.getComparator(cmp).test(lad,rad);
			} catch (Exception e) {
				return StringCondition.getComparator(cmp).test(left.toString(),right.toString());
			}
		}
	}


	public static Filter create(String rule) throws IOException {
		List<Token> tokens = tokenize(rule);
		boolean negate;
		Filter result = null;
		try {
			negate = (tokens.size() > 0 && tokens.get(0).type == Type.KEYWORD && tokens.get(0).ciEquals("not"));
			if (negate) tokens.remove(0);

			if (tokens.size() == 3) {
				if (tokens.get(1).type == Type.KEYWORD && tokens.get(1).ciEquals("every") &&
					tokens.get(2).type == Type.DURATION) {
					if (tokens.get(0).type == Type.KEYWORD && tokens.get(0).ciEquals("global"))
						result = new GlobalCooldown(tokens.get(2).srep.toLowerCase(Locale.ROOT));
					else if (tokens.get(0).type == Type.VARIABLE)
						result = new PlayerCooldown(tokens.get(0).srep.toLowerCase(Locale.ROOT), tokens.get(2).srep.toLowerCase(Locale.ROOT));
					else
						throw new StatementParseException("Invalid cooldown condition. Subject needs to be 'global' or variable at \""+rule+"\"");
				}
				else if (tokens.get(1).type == Type.KEYWORD && tokens.get(1).ciEquals("hasPermission")) {
					if (tokens.get(2).type != Type.STRING)
						throw new StatementParseException("Right side of hasPermission has to be a quoted string at \""+rule+"\"");
					if (tokens.get(0).type != Type.VARIABLE)
						throw new StatementParseException("Left side of hasPermission has to be a player variable at \""+rule+"\"");
					result = new PlayerPermission(tokens.get(0).srep, tokens.get(2).srep);
				}
				else if ((tokens.get(1).type == Type.OTHER || (tokens.get(1).type == Type.KEYWORD &&tokens.get(1).ciEquals("matches") )) &&
					(tokens.get(0).type == Type.STRING || tokens.get(0).type == Type.VARIABLE) &&
					(tokens.get(2).type == Type.STRING || tokens.get(2).type == Type.VARIABLE)) {
					if (tokens.get(0).type == Type.VARIABLE && tokens.get(1).type == Type.VARIABLE)
						result = new VariableCondition(tokens.get(0).srep, tokens.get(1).srep.toLowerCase(Locale.ROOT), tokens.get(2).srep);
					else
						result = new StringCondition(tokens.get(0), tokens.get(1).srep.toLowerCase(Locale.ROOT), tokens.get(2));
				}
				else if (tokens.get(1).type == Type.OTHER &&
					(tokens.get(0).type == Type.NUMERIC || tokens.get(0).type == Type.VARIABLE) &&
					(tokens.get(2).type == Type.NUMERIC || tokens.get(2).type == Type.VARIABLE)) {
					if (tokens.get(0).type == Type.VARIABLE && tokens.get(1).type == Type.VARIABLE)
						//can this be reached?
						result = new VariableCondition(tokens.get(0).srep, tokens.get(1).srep.toLowerCase(Locale.ROOT), tokens.get(2).srep);
					else
						result = new NumericCondition(tokens.get(0), tokens.get(1).srep, tokens.get(2));
				}
			}
			if (result == null) throw new StatementParseException("Unknown filter condition syntax for \""+rule+"\"");
			return negate ? Filter.negate(result) : result;
		} catch (RuntimeException e) {
			throw new StatementParseException("Failed to parse condition \""+rule+"\"", e);
		}
	}

	/** @return seconds */
	private static Long parseDuration(String duration) {
		int i;
		if ((i = duration.indexOf(':'))>=0) {
			long val = 0;
			int o=0;
			List<Integer> parts = new LinkedList<>();
			for (; i>=0; i=duration.indexOf(':', o)) {
				parts.add(Integer.parseInt(duration.substring(o,i)));
				o=i+1;
			}
			if (parts.size()<2 || parts.size()>3) throw new IllegalArgumentException("Invalid duration format");
			if (parts.size() == 3) {
				val = parts.remove(0) * 3600;
			}
			if (parts.get(0)>=60) throw new IllegalArgumentException("You can specify a max of 59 minutes in an hour");
			if (parts.get(1)>=60) throw new IllegalArgumentException("You can specify a max of 59 seconds in a minute");
			return val + parts.get(0) * 60 + parts.get(1);
		} else {
			long mul;
			int sl=1;
			if (duration.endsWith("min")) {
				mul = 60L; sl = 3;
			} else if (duration.endsWith("sec")) {
				mul = 1L; sl = 3;
			} else if (duration.endsWith("h")) {
				mul = 3600L;
			} else if (duration.endsWith("m")) {
				mul = 60L;
			} else if (duration.endsWith("s")) {
				mul = 1L;
			} else throw new IllegalArgumentException("Unknown duration suffix");
			if (duration.length()<=sl) throw new IllegalArgumentException("Duration suffix without value");
			duration = duration.substring(0,duration.length()-sl);
			return Long.parseLong(duration) * mul;
		}
	}
	private static class Token {
		String srep;
		Type type;
		public Token(String srep, Type type) {
			this.srep = srep;
			this.type = type;
		}

		boolean ciEquals(String other) {
			return srep.equalsIgnoreCase(other);
		}

		@Override
		public String toString() {
			return srep;
		}
	}
	enum Type {
		STRING, NUMERIC, VARIABLE, DURATION, KEYWORD, OTHER
	}
	private static final Pattern numericPattern = Pattern.compile("^([-]?[0-9]+(?:\\.[0-9]+)?(?:e[+-]?[0-9]+)?)");
	private static final Pattern variablePattern = Pattern.compile("^\\$\\{(\\p{L}+)}");
	private static final Pattern durationPattern = Pattern.compile("^[0-9]+(?:(?:h|m(?:in)?|s(?:ec)?)|(?::[0-9]{2}(?::[0-9]{2})?))");
	private static final Pattern keywordPattern = Pattern.compile("^(\\p{L}+)(?:\\b|$)");
	private static final Pattern symbolsPattern = Pattern.compile("^([^\\p{L}\\p{Digit}\\p{javaWhitespace}]+)");
	static List<Token> tokenize(String rule) throws IOException {
		int q,s,off=0;
		List<Token> tokens = new LinkedList<>();
		while (off < rule.length()) {
			while (Character.isWhitespace(rule.charAt(off))) off++;
			if (rule.charAt(off) == '"') { //collect string
				q = off;
				while (true) {
					q = Utils.firstIndexOf(rule, "\\\"", q + 1);
					if (q==-1) throw new StatementParseException("Unterminated String!"); //should not happen here
					if (rule.charAt(q)=='\\') {
						if (q+1 == rule.length()) throw new StatementParseException("Unterminated String!"); //escape at eol?
						char escaped = rule.charAt(q+1);
						if (escaped != 'n' && escaped != 't' && escaped != '\\' && escaped != '"') throw new StatementParseException("Invalid escape: \\n \\t \\\\ and \\\" are supported");
						q++;//skip escaped char
					} else {
						String token = rule.substring(off+1, q-1);
						StringBuilder unescaped = new StringBuilder(token.length());
						off = q+1;
						int lastIndex = q = 0;
						while ((q=token.indexOf('\\',q))>=0) {
							char rep;
							switch (token.charAt(q+1)) {
								case 'n': rep = '\n'; break;
								case 't': rep = '\t'; break;
								case '\\': rep = '\\'; break;
								case '"': rep = '"'; break;
								default: throw new StatementParseException("Failsafe: unsupported escape");
							}
							unescaped.append(token, 0, q).append(rep);
							q+=2;
							lastIndex = q;
						}
						if (lastIndex < token.length()) unescaped.append(token.substring(lastIndex));
						tokens.add(new Token(unescaped.toString(), Type.STRING));
					}
				}
			} else if (rule.charAt(off)>='0' && rule.charAt(off)<='9' ||
					(rule.charAt(off)=='-' && off+1<rule.length() && rule.charAt(off+1)>='0' && rule.charAt(off+1)<='9' ) ) { //collect number
				s = rule.indexOf(' ', off); //peek next space
				String poke = s==-1?rule.substring(off):rule.substring(off,s);
				if (poke.indexOf(':')>0 || Character.isLetter(poke.charAt(poke.length()-1))) {
					//probably a duration
					Matcher m = durationPattern.matcher(rule.substring(off));
					if (!m.find()) throw new StatementParseException("Could not parse duration around char "+off+" in rule `"+rule+"`");
					tokens.add(new Token(m.group(), Type.DURATION));
					off += m.end();
				} else {
					//probably a numeric
					Matcher m = numericPattern.matcher(rule.substring(off));
					if (!m.find()) throw new StatementParseException("Could not parse number around char "+off+" in rule `"+rule+"`");
					tokens.add(new Token(m.group(), Type.NUMERIC));
					off += m.end();
				}
			} else if (rule.charAt(off) == '$') {
				Matcher m = variablePattern.matcher(rule.substring(off));
				if (!m.find()) throw new StatementParseException("Malformed variable indicator around char "+off+" in rule `"+rule+"`");
				tokens.add(new Token(m.group(1), Type.VARIABLE));
				off += m.end();
			} else {
				Matcher m = keywordPattern.matcher(rule.substring(off));
				if (m.find()) {
					tokens.add(new Token(m.group(), Type.KEYWORD));
					off += m.end();
					continue;
				}
				m = symbolsPattern.matcher(rule.substring(off));
				if (m.find()) {
					tokens.add(new Token(m.group(), Type.OTHER));
					off += m.end();
				}
			}
		}
		return tokens;
	}

}
