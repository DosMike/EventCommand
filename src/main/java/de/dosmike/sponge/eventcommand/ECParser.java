package de.dosmike.sponge.eventcommand;

import de.dosmike.sponge.eventcommand.exception.ScriptParseException;
import de.dosmike.sponge.eventcommand.statements.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ECParser {

	private enum LineType {
		INVALID,
		TRIGGER,
		WITH_CHAIN,
		MATHS,
		FILTER,
		ACTION
	}

	List<Trigger<?>> parsed = new LinkedList<>();
	List<WithChain> variables = new ArrayList<>();
	List<Computing> quickMaths = new ArrayList<>();
	ActionGroup actions = null;
	int lineNo = 0;
	Path path;
	LineType lastParsed = LineType.INVALID;
	String nextTrigger = null;

	ECParser(Path path) {
		this.path = path;
	}

	ECParser load() throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(Files.newInputStream(path)));
			String line;

			char globalIndent = 0;
			while ((line = br.readLine()) != null) {
				lineNo++;
				char spaceChar=0,off=0;
				while (off < line.length() && Character.isWhitespace(line.charAt(off))) {
					if (globalIndent != 0 && line.charAt(off) != globalIndent) throw new ScriptParseException(lineNo, "Mixed indentation between lines is not supported");
					else if (globalIndent == 0) globalIndent = line.charAt(off);
					if (off == 0) spaceChar = line.charAt(off);
					else if (line.charAt(off) != spaceChar) throw new ScriptParseException(lineNo, "Mixed indentation within line is not supported");
					off++;
				}
				line = line.substring(off).trim();
				if (line.startsWith("--") || line.isEmpty()) continue; //comments
				switch (guessType(line)) {
					case TRIGGER: {
						closeTrigger();
						parseTrigger(line);
						break;
					}
					case WITH_CHAIN: {
						parseWithChain(line);
						break;
					}
					case MATHS: {
						parseMaths(line);
						break;
					}
					case FILTER: {
						parseFilter(line, off);
						break;
					}
					case ACTION: {
						parseAction(line, off);
						break;
					}
				}
			}
			closeTrigger();

		} catch (Exception e) {
			throw new ScriptParseException(lineNo, "Unable to load file \"" + path.toString() + "\"", e);
		} finally {
			try {
				br.close();
			} catch (Exception ignore) {
			}
		}
		return this;
	}

	public List<Trigger<?>> getTriggers() {
		return parsed;
	}

	// asPredicate is (s)->matcher(s).find() - not (s)->matcher(s).matches() as expected
	private static final Predicate<String> checkPatternWith = Pattern.compile("^with\\b").asPredicate();
	private static final Predicate<String> checkPatternMaths = Pattern.compile("^let\\b").asPredicate();
	private static final Predicate<String> checkPatternFor = Pattern.compile("^(?:for|otherwise)\\b").asPredicate();
	private static final Predicate<String> checkPatternAction = Pattern.compile("^[!/][^\\s]").asPredicate();
	private LineType guessType(String line) throws IOException {
		LineType type;
		if (line.charAt(0) == '@') {
			type = LineType.TRIGGER;
		} else if (checkPatternWith.test(line.toLowerCase(Locale.ROOT))) {
			type = LineType.WITH_CHAIN;
		} else if (checkPatternMaths.test(line.toLowerCase(Locale.ROOT))) {
			type = LineType.MATHS;
		} else if (checkPatternFor.test(line.toLowerCase(Locale.ROOT))) {
			type = LineType.FILTER;
		} else if (checkPatternAction.test(line.toLowerCase(Locale.ROOT))) {
			type = LineType.ACTION;
		} else {
			//check for special keywords
			if (line.equalsIgnoreCase("cancel")) type = LineType.ACTION;
			else throw new ScriptParseException(lineNo, "Invalid Statement");
		}
		// 1 @event
		// 2  with
		// 3  math
		// 4  filter/action
		// legal progression
		// 1->2,4
		// 2->2,3,4
		// 3->3,4
		// 4->1,4
		// legal predecessors
		// 1<-4
		// 2<-1,2
		// 3<-2,3
		// 4<-1,2,3,4
		if (lastParsed == LineType.INVALID && type != LineType.TRIGGER) {
			throw new ScriptParseException(lineNo, "Expected event name");
		} else if (type == LineType.TRIGGER) {
			if (lastParsed == LineType.TRIGGER) { // 1 <- 1 -> 2,4
				throw new ScriptParseException(lineNo, "Empty Event. Expected 'with'-chains, filters or actions; found event name");
			} else if (lastParsed == LineType.WITH_CHAIN) { // 1 <- 2 -> 2,3,4
				throw new ScriptParseException(lineNo, "No actions for event. Expected 'with'-chains, mathematics, filter or actions; found event name");
			} else if (lastParsed == LineType.MATHS) { // 1 <- 3 -> 3,4
				throw new ScriptParseException(lineNo, "No actions for event. Expected mathematics, filter or actions; found event name");
			}
		} else if (type == LineType.WITH_CHAIN) {
			if (lastParsed == LineType.MATHS) { // 2 <- 3 -> 3,4
				throw new ScriptParseException(lineNo, "'With'-chain after mathematics. Expected mathematics, filter or actions; found 'with'-chain");
			} else if (lastParsed == LineType.ACTION || lastParsed == LineType.FILTER) { // 2 <- 4 -> 1,4
				throw new ScriptParseException(lineNo, "'With'-chain after actions. Expected filter, actions or next event name; found 'with'-chain");
			}
		} else if (type == LineType.MATHS) {
			if (lastParsed == LineType.TRIGGER) { // 3 <- 1 -> 2,4
				throw new ScriptParseException(lineNo, "Mathematics before 'with'-chains. Expected 'with'-chain, filter or actions; found maths");
			} else if (lastParsed == LineType.ACTION || lastParsed == LineType.FILTER) { // 3 <- 4 -> 1,4
				throw new ScriptParseException(lineNo, "Mathematics after actions. Expected filter, actions or next event name; found maths");
			}
		}
		return type;
	}

	private void parseTrigger(String line) {
		nextTrigger = line.substring(1).trim();
		lastParsed = LineType.TRIGGER;
	}

	private void parseWithChain(String line) {
		variables.add(new WithChain(line));
		lastParsed = LineType.WITH_CHAIN;
	}

	private void parseMaths(String line) {
		quickMaths.add(new Computing(line));
		lastParsed = LineType.MATHS;
	}

	private void parseFilter(String line, int indent) throws IOException {
		setActionGroup(indent);
		Filtered f = new Filtered(line);
		f.setParent(actions);
		actions.add(f);
		actions = f;
	}

	private void parseAction(String line, int indent) throws IOException {
		setActionGroup(indent);
		if (actions.previousSibling() instanceof Filtered) {
			((Filtered) actions.previousSibling()).setLastCase(true);
		}
		if (line.equalsIgnoreCase("cancel")) {
			actions.add(CancelEventAction.create(nextTrigger));
		} else if (line.startsWith("!")) {
			actions.add(new CommandAction(line.substring(1).trim(), CommandSourceResolver.Factory.Server()));
		} else {
			actions.add(new CommandAction(line.trim(), CommandSourceResolver.Factory.Player()));
		}
		lastParsed = LineType.ACTION;
	}

	private void setActionGroup(int indent) throws IOException {
		if (actions == null) {
			actions = new ActionGroup(indent);
		} else {
			if (actions.getDepth() == -1) { //sub-group was created, but it doesn't know it's depth yet
				if (indent > actions.getParent().getDepth()) {
					actions.setDepth(indent);
				} else { //empty body aka stepping up from the Filtered without adding things(or depth)
					actions.setDepth(Integer.MAX_VALUE); //so mock one in so we can ..
					actions = actions.findParent(indent); //.. go to the requested indent level
				}
			} else if (indent > actions.getDepth()) { //arbitrarily stepping in does not really make sense
				//actions = new ActionGroup(indent, actions);
				throw new ScriptParseException(lineNo, "Cannot arbitrarily increase indentation, preceding filter is required");
			} else if (indent < actions.getDepth()) {
				actions = actions.findParent(indent);
			}
		}
	}

	private void closeTrigger() throws IOException {
		if (nextTrigger != null) {
			if (actions == null) {
				throw new ScriptParseException(lineNo, "Trigger preceding \"" + nextTrigger + "\" does not specify any actions");
			}
			try {
				parsed.add(Trigger.create(nextTrigger, variables, quickMaths, actions.getRoot()));
			} catch (Exception e) {
				throw new ScriptParseException(lineNo, "Unable to create trigger \"" + nextTrigger + '"', e);
			}
		}
		variables.clear();
		actions = null;
	}

}
