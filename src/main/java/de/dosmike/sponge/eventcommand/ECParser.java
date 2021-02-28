package de.dosmike.sponge.eventcommand;

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
				while (Character.isWhitespace(line.charAt(off))) {
					if (globalIndent != 0 && line.charAt(off) != globalIndent) throw new IOException("Mixed indentation between lines is not supported");
					else if (globalIndent == 0) globalIndent = line.charAt(off);
					if (off == 0) spaceChar = line.charAt(off);
					else if (line.charAt(off) != spaceChar) throw new IOException("Mixed indentation within line is not supported");
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
			throw new IOException("Unable to load file \"" + path.toString() + "\"", e);
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

	private static final Predicate<String> checkPatternWith = Pattern.compile("^let\\b").asPredicate();
	private static final Predicate<String> checkPatternMaths = Pattern.compile("^with\\b").asPredicate();
	private static final Predicate<String> checkPatternFor = Pattern.compile("^(?:for|otherwise)\\b").asPredicate();
	private LineType guessType(String line) {
		LineType type;
		if (line.charAt(0) == '@') {
			type = LineType.TRIGGER;
		} else if (checkPatternWith.test(line.toLowerCase(Locale.ROOT))) {
			type = LineType.WITH_CHAIN;
		} else if (checkPatternMaths.test(line.toLowerCase(Locale.ROOT))) {
			type = LineType.MATHS;
		} else if (checkPatternFor.test(line.toLowerCase(Locale.ROOT))) {
			type = LineType.FILTER;
		} else {
			type = LineType.ACTION;
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
			throw new IllegalStateException("Expected event name at line " + lineNo);
		} else if (type == LineType.TRIGGER) {
			if (lastParsed == LineType.TRIGGER) { // 1 <- 1 -> 2,4
				throw new IllegalStateException("Empty Event. Expected 'with'-chains, filters or actions at line " + lineNo+", found event name");
			} else if (lastParsed == LineType.WITH_CHAIN) { // 1 <- 2 -> 2,3,4
				throw new IllegalStateException("No actions for event. Expected 'with'-chains, mathematics, filter or actions at line " + lineNo+", found event name");
			} else if (lastParsed == LineType.MATHS) { // 1 <- 3 -> 3,4
				throw new IllegalStateException("No actions for event. Expected mathematics, filter or actions at line " + lineNo+", found event name");
			}
		} else if (type == LineType.WITH_CHAIN) {
			if (lastParsed == LineType.MATHS) { // 2 <- 3 -> 3,4
				throw new IllegalStateException("'With'-chain after mathematics. Expected mathematics, filter or actions at line " + lineNo + ", found 'with'-chain");
			} else if (lastParsed == LineType.ACTION || lastParsed == LineType.FILTER) { // 2 <- 4 -> 1,4
				throw new IllegalStateException("'With'-chain after actions. Expected filter, actions or next event name at line " + lineNo + ", found 'with'-chain");
			}
		} else if (type == LineType.MATHS) {
			if (lastParsed == LineType.TRIGGER) { // 3 <- 1 -> 2,4
				throw new IllegalStateException("Mathematics before 'with'-chains. Expected 'with'-chain, filter or actions at line " + lineNo + ", found 'with'-chain");
			} else if (lastParsed == LineType.ACTION || lastParsed == LineType.FILTER) { // 3 <- 4 -> 1,4
				throw new IllegalStateException("Mathematics after actions. Expected filter, actions or next event name at line " + lineNo + ", found 'with'-chain");
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

	private void parseFilter(String line, int indent) throws IOException {
		setActionGroup(indent);
		Filtered f = new Filtered(line);
		f.parent = actions;
		actions = f;
	}

	private void parseAction(String line, int indent) throws IOException {
		setActionGroup(indent);
		if (line.startsWith("!")) {
			actions.add(new Action(line.substring(1).trim(), CommandSourceResolver.Factory.Server()));
		} else {
			actions.add(new Action(line.trim(), CommandSourceResolver.Factory.Player()));
		}
		lastParsed = LineType.ACTION;
	}

	private void setActionGroup(int indent) throws IOException {
		if (actions == null) {
			actions = new ActionGroup(indent);
		} else {
			if (actions.depth == -1 && indent > actions.parent.depth) { //sub-group was created, but it doesn't know it's depth yet
				actions.depth = indent;
			} else if (indent > actions.depth) { //arbitrarily stepping in does not really make sense
				//actions = new ActionGroup(indent, actions);
				throw new IOException("Cannot arbitrarily increase indentation, preceding filter is required");
			} else if (indent < actions.depth) {
				actions = actions.findParent(indent);
			}
		}
	}

	private void closeTrigger() throws IOException {
		if (nextTrigger != null) {
			if (actions == null) {
				throw new IllegalStateException("Trigger \"" + nextTrigger + "\" does not specify any actions before line " + lineNo);
			}
			try {
				parsed.add(TriggerFactory.get().create(nextTrigger, variables, actions.getRoot()));
			} catch (Exception e) {
				throw new IOException("Unable to create trigger \"" + nextTrigger + "\" before line " + lineNo, e);
			}
		}
		variables.clear();
		actions = null;
	}

}
