package de.dosmike.sponge.eventcommand.statements;

import de.dosmike.sponge.eventcommand.EventCommand;
import de.dosmike.sponge.eventcommand.VariableContext;
import de.dosmike.sponge.eventcommand.exception.ScriptExecutionException;
import de.dosmike.sponge.eventcommand.exception.StatementParseException;

import java.util.LinkedList;

public class ActionGroup implements Action {

	int depth;
	LinkedList<Action> actions = new LinkedList<>();
	ActionGroup parent;
	public ActionGroup(int depth) {
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

	public ActionGroup getRoot() {
		ActionGroup g = this;
		while (g.parent != null) g = g.parent;
		return g;
	}

	public Action previousSibling() {
		return actions.isEmpty() ? null : actions.getLast();
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public LinkedList<Action> getActions() {
		return actions;
	}

	public ActionGroup getParent() {
		return parent;
	}

	public void setParent(ActionGroup parent) {
		this.parent = parent;
	}

	@Override
	public void run(Object context, VariableContext variables) {
		boolean inFiltered = false, fastForward = false;
		for (int i=0; i<actions.size(); i++) {
			Action a = actions.get(i);
			if (a instanceof Filtered) {
				Filtered f = (Filtered) a;
				if (!inFiltered) {
					if (f.lastCase) throw new ScriptExecutionException("Filter consists of only \"otherwise\"!");
					inFiltered = true;
					fastForward = false;
				}
				if (fastForward) {
					if (EventCommand.isVerboseLogging())
						EventCommand.l("  Case skipped");
					if (f.lastCase) inFiltered = false;
				} else if (f.test(variables)) {
					fastForward = true;
					f.run(context, variables);
				}
			} else {
				inFiltered = false;
				a.run(context, variables);
			}
		}
	}

	public ActionGroup findParent(int indent) {
		ActionGroup g = this;
		while (g.depth > indent && g.parent!=null) {
			g = g.parent;
		}
		if (g.depth != indent) throw new StatementParseException("Indent miss-match. Couldn't find parent indentation level of depth "+indent);
		return g;
	}
}
