package de.dosmike.sponge.eventcommand.statements;

import de.dosmike.sponge.eventcommand.EventCommand;
import de.dosmike.sponge.eventcommand.VariableContext;
import de.dosmike.sponge.eventcommand.exception.StatementParseException;

import java.io.IOException;
import java.util.*;

public class Filtered extends ActionGroup {

	List<Filter> filters = new LinkedList<>();
	Map<UUID, Long> clientCD;
	Long globalCD;
	boolean lastCase;

	public Filtered(String line) throws IOException {
		super(-1);

		if (lastCase = line.toLowerCase(Locale.ROOT).equals("otherwise")) { //no filters for the "else" case
			return;
		}
		// skip the 'for'
		// for every csv parse the condition, keep quoted strings in mind
		line = line.substring(3).trim();
		if (line.isEmpty()) throw new StatementParseException("Expected conditions, found end of line");

		int q,c,off=0,mark=0;
		//we always scan from mark, but copy from off
		//this way we can skip over quotes with mark
		//and still substring the whole filter
		while(off < line.length()) {
			q = line.indexOf('"',mark);
			c = line.indexOf(',',mark);
			if (q == -1 || q>c) {
				if (c == -1) {
					filters.add(FilterFactory.create(line.substring(off).trim()));
					break; //last filter
				} else {
					filters.add(FilterFactory.create(line.substring(off,c).trim()));
					off = mark = c+1;
				}
			} else { //q<c requires c>0
				//search end quote
				while(true) {
					q = line.indexOf('"', q + 1);
					if (q<0) {
						throw new StatementParseException("Quotes did not terminate, starting around offset "+(mark+3));
					} else if (q==0 || line.charAt(q-1)!='\\') {
						//end here, continue search for ',' after quotes
						mark = q+1;
						break;
					}
				}
			}
		}
	}

	public boolean isLastCase() {
		return lastCase;
	}

	public void setLastCase(boolean lastCase) {
		this.lastCase = lastCase;
	}

	public boolean test(VariableContext variables) {
		int i=0;
		if (EventCommand.isVerboseLogging())
			EventCommand.l("  Testing case...");
		for (Filter f : filters) {
			i++;
			if (!f.test(variables, this)) {
				if (EventCommand.isVerboseLogging())
					EventCommand.l("  Case failed at rule "+i);
				return false;
			}
		}
		if (EventCommand.isVerboseLogging())
			EventCommand.l("  Case passed");
		return true;
	}

}
