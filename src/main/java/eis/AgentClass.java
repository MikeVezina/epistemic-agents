package eis;

import java.util.Queue;

import jason.NoValueException;
import jason.asSemantics.Agent;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSyntax.*;

public class AgentClass extends Agent {

    private static final String STEP_FUNCTOR = "step";

    @Override
    public Intention selectIntention(Queue<Intention> intentions) {

        // Select recurring events (not fully implemented yet)
        // return temporalIntentionSelection(intentions);

        return super.selectIntention(intentions);
    }

    private Intention temporalIntentionSelection(Queue<Intention> intentions) {
        int currentStep = updateCurrentStep();

        if (currentStep == -1)
            return super.selectIntention(intentions);

        for (Intention intention : intentions) {
            for (IntendedMeans im : intention) {
                Literal intentionLiteral = im.getTrigger().getLiteral();

                // Look for 'recurrent' and 'lastOccurrence' annotations on the intentions
                Literal recurrentAnnotation = intentionLiteral.getAnnot("recurrent");
                Literal lastOccurrenceAnnotation = intentionLiteral.getAnnot("lastOccurrence");

                if (recurrentAnnotation != null) {
                    int recurringInterval = solveWithoutTry(recurrentAnnotation.getTerm(0));

                    int lastOccurrence = 0;
                    if (lastOccurrenceAnnotation != null) {
                        lastOccurrence = solveWithoutTry(lastOccurrenceAnnotation.getTerm(0));
                    }

                    if (currentStep >= lastOccurrence + recurringInterval) {
                        System.out.println("Executing recurrent intention: " + intentionLiteral.getFunctor());
                        return intention;
                    }

                }
            }
        }

        // Default intention selection
        return super.selectIntention(intentions);
    }

    private static int solveWithoutTry(NumberTerm term) {
        try {
            return (int) term.solve();
        } catch (NoValueException ignored) {
            System.err.println("Failed to solve for value: " + term);
        }
        return -1;
    }

    private static int solveWithoutTry(Term term) {
        return solveWithoutTry((NumberTerm) term);
    }

    private Pred findAnnotation(ListTerm annotations, String functor) {
        for (Term term : annotations) {
            if (!term.isPred())
                continue;

            Pred predicate = (Pred) term;
            if (predicate.getFunctor().equals(functor))
                return predicate;

        }
        return null;
    }

    private int step = 0;
    private int updateCurrentStep() {

        getBB().getPercepts().forEachRemaining(percept -> {
            // Find the step perception
            if (!percept.getFunctor().equals(STEP_FUNCTOR))
                return;

            // Parse the current step into an int
            NumberTerm currentStepTerm = (NumberTerm) percept.getTerm(0);
            try {
                this.step = (int) currentStepTerm.solve();
            } catch (NoValueException ignored) {
                this.step = -1;
            }

        });

        return step;

    }
}
