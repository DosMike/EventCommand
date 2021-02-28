package de.dosmike.sponge.eventcommand;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ActionGroup extends Action {

	int depth;
	List<Action> actions = new LinkedList<>();
	ActionGroup parent;
	ActionGroup(int depth) {
		this.depth = depth;
		this.parent = null;
	}
	ActionGroup(int depth, ActionGroup parent) {
		this.depth = depth;
		this.parent = parent;
	}

	public void add(Action action) {
		actions.add(action);
	}

	ActionGroup getRoot() {
		ActionGroup g = this;
		while (g.parent != null) g = g.parent;
		return g;
	}

	@Override
	void run(Object context, Map<String, Object> variables) {
		boolean inFiltered = false, fastForward = false;
		for (int i=0; i<actions.size(); i++) {
			Action a = actions.get(i);
			if (a instanceof Filtered) {
				Filtered f = (Filtered) a;
				if (!inFiltered) {
					if (f.lastCase) throw new RuntimeException("Filter consists of only \"otherwise\"!");
					inFiltered = true;
					fastForward = false;
				}
				if (fastForward) {
					if (f.lastCase) inFiltered = false;
				} else if (f.test(variables)) {
					fastForward = true;
					f.run(context, variables);
				}
			} else {
				inFiltered = false;
				EventCommand.l("  Executing %s", a.command);
				a.run(context, variables);
			}
		}
	}

	public ActionGroup findParent(int indent) {
		ActionGroup g = this;
		while (g.depth > indent && g.parent!=null) g = g.parent;
		if (g.depth != indent) throw new IllegalStateException("Indent miss-match. Couldn't find parent indentation level of depth "+indent);
		return g;
	}
}
