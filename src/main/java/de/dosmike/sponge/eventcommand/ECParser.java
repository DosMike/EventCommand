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

public class ECParser {

	private enum LineType {
		INVALID,
		TRIGGER,
		WITH_CHAIN,
		ACTION
	}

	List<Trigger<?>> parsed = new LinkedList<>();
	List<WithChain> variables = new ArrayList<>();
	List<Action> actions = new ArrayList<>();
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

			while ((line = br.readLine()) != null) {
				lineNo++;
				line = line.trim();
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
					case ACTION: {
						parseAction(line);
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

	private LineType guessType(String line) {
		LineType type;
		if (line.charAt(0) == '@') {
			type = LineType.TRIGGER;
		} else if (line.toLowerCase(Locale.ROOT).startsWith("with")) {
			type = LineType.WITH_CHAIN;
		} else {
			type = LineType.ACTION;
		}
		if (type == LineType.TRIGGER && lastParsed == LineType.TRIGGER) {
			throw new IllegalStateException("Expected 'with'-chains or actions at line " + lineNo);
		} else if (type == LineType.WITH_CHAIN) {
			if (lastParsed == LineType.INVALID) {
				throw new IllegalStateException("Expected event name at line " + lineNo);
			} else if (lastParsed == LineType.ACTION) { //actions can be repeated or followed by new triggers, we're in another with chain!
				throw new IllegalStateException("Expected action or event name at line " + lineNo);
			}
		} else if (type == LineType.ACTION && lastParsed == LineType.INVALID) {
			throw new IllegalStateException("Expected event name at line " + lineNo);
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

	private void parseAction(String line) {
		if (line.startsWith("!")) {
			actions.add(new Action(line.substring(1).trim(), CommandSourceResolver.Factory.Server()));
		} else {
			actions.add(new Action(line.trim(), CommandSourceResolver.Factory.Player()));
		}
		lastParsed = LineType.ACTION;
	}

	private void closeTrigger() throws IOException {
		if (nextTrigger != null) {
			if (actions.isEmpty()) {
				throw new IllegalStateException("Trigger \"" + nextTrigger + "\" does not specify any actions before line " + lineNo);
			}
			try {
				parsed.add(TriggerFactory.get().create(nextTrigger, variables, actions));
			} catch (Exception e) {
				throw new IOException("Unable to create trigger \"" + nextTrigger + "\" before line " + lineNo, e);
			}
		}
		variables.clear();
		actions.clear();
	}

}
