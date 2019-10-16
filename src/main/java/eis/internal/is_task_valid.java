package eis.internal;

import eis.agent.AgentContainer;
import eis.agent.AuthenticatedAgent;
import eis.percepts.requirements.Requirement;
import eis.watcher.SynchronizedPerceptWatcher;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Term;
import utils.LiteralUtils;

import java.util.*;
import java.util.stream.Collectors;

public class is_task_valid extends DefaultInternalAction {
    private static final long serialVersionUID = -6214881485708125130L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        // Get the task name
        String taskName = ((Atom) args[0]).getFunctor();
        SynchronizedPerceptWatcher synchronizedPerceptWatcher = SynchronizedPerceptWatcher.getInstance();
        return synchronizedPerceptWatcher.getSharedPerceptContainer().getTaskMap().isTaskValid(taskName);
    }
}
