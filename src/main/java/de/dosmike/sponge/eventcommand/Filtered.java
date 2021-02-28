package de.dosmike.sponge.eventcommand;

import java.io.IOException;
import java.util.*;

public class Filtered extends Action {

	List<Filter> filters = new LinkedList<>();
	Map<UUID, Long> clientCD;
	Long globalCD;

	List<Action> actions = new LinkedList<>();

	public Filtered(String line) throws IOException {
		if (line.toLowerCase(Locale.ROOT).equals("otherwise")) return; //no filters for the "else" case
		// skip the 'for'
		// for every csv parse the condition, keep quoted strings in mind
		line = line.substring(3).trim();
		if (line.isEmpty()) throw new IOException("Expected conditions, found end of line");

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
						throw new IOException("Quotes did not terminate, starting around offset "+(mark+3));
					} else if (q==0 || line.charAt(q-1)!='\\') {
						//end here, continue search for ',' after quotes
						mark = q+1;
						break;
					}
				}
			}
		}
	}

	public boolean evaluate(Map<String, Object> variables) {
		for (Filter f : filters) if (!f.test(variables, this)) return false;
		return true;
	}

	@Override
	void run(Object context, Map<String, Object> variables) {
		if (evaluate(variables))
			for (Action a : actions)
				a.run(context, variables);
	}
}
