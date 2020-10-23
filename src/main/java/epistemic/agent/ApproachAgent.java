package epistemic.agent;

import jason.JasonException;
import jason.RevisionFailedException;
import jason.asSemantics.Agent;
import jason.asSemantics.Intention;
import jason.asSyntax.*;

import java.util.*;

public class ApproachAgent extends Agent {

    private static final String ALICE = "Alice";
    private static final String BOB = "Bob";
    private static final String CHARLIE = "Charlie";
    private Map<String, Set<String>> kbPlayerCards = new HashMap<>();
    private Map<String, String> bbPlayerCards = new HashMap<>();


    @Override
    public List<Literal>[] brf(Literal beliefToAdd, Literal beliefToDel, Intention i) throws RevisionFailedException {
        if (!isCardLiteral(beliefToAdd))
            return super.brf(beliefToAdd, beliefToDel, i);

        String name = getPlayer(beliefToAdd);
        String card = getPlayerCards(beliefToAdd);

        if (beliefToAdd.getNS().equals(ASSyntax.createAtom("kb"))) {
            if (!kbPlayerCards.containsKey(name))
                kbPlayerCards.put(name, new HashSet<>());
            kbPlayerCards.get(name).add(card);

            return super.brf(beliefToAdd, beliefToDel, i);
        }

        // Update player cards (allow overwriting cards for testing)
        bbPlayerCards.put(name, card);

        // Make sure there are no more than 4 aces or 4 eights
        validateState();

        // If we don't have enough information to infer
        if(!bbPlayerCards.containsKey(BOB)
                || !bbPlayerCards.containsKey(CHARLIE)
                || !bbPlayerCards.get(BOB).equals(bbPlayerCards.get(CHARLIE))) // Not same cards
            return super.brf(beliefToAdd,beliefToDel,i);

        // Inferred knowledge rules:
        return inferKnowledge(beliefToAdd,beliefToDel,i);
    }

    private List<Literal>[] inferKnowledge(Literal beliefToAdd, Literal beliefToDel, Intention i) throws RevisionFailedException {
        var revision = new RevisionResult();
        String bobCard = bbPlayerCards.get(BOB);
        String charlieCard = bbPlayerCards.get(CHARLIE);

        // Call BRF for adding/removing belief
        revision.addResult(super.brf(beliefToAdd,beliefToDel,i));


        getTS().getAg().getLogger().info("BRF Inferring....");
        // BRF approach
        if(bobCard.equals("AA"))
            revision.addResult(super.brf(ASSyntax.createLiteral("hand", new StringTermImpl(ALICE), new StringTermImpl("88")),null,i));
        else if(bobCard.equals("88"))
            revision.addResult(super.brf(ASSyntax.createLiteral("hand", new StringTermImpl(ALICE), new StringTermImpl("AA")),null,i));

        return revision.buildResult();
    }

    private void validateState() throws RevisionFailedException{
        if(bbPlayerCards.size() != 3)
            return;

        Map<Character, Integer> allCards = new HashMap<>();

        allCards.put('A', 0);
        allCards.put('8', 0);

        for(var cards : bbPlayerCards.values())
        {

            for(char c : cards.toCharArray())
            {
                if(!allCards.containsKey(c))
                    throw new RevisionFailedException("The term (" + cards + ") is not part of a valid hand");
                allCards.computeIfPresent(c, (key, val) -> val + 1);
            }
        }

        int numAces = allCards.get('A');
        int numEights = allCards.get('8');
        int total = numAces + numEights;

        // Invalid number of cards!
        if (numAces > 4 || numEights > 4 || total != 6) {
            throw new RevisionFailedException("Too many cards! " + numAces + ", " + numEights + ", " + total);
        }
    }

    private boolean isCardLiteral(Literal literal) {
        return literal != null && literal.getFunctor().equals("hand") && literal.getArity() == 2;
    }

    private String getPlayer(Literal belief) {
        return ((StringTerm) belief.getTerm(0)).getString();
    }

    private String getPlayerCards(Literal belief) {
        return ((StringTerm) belief.getTerm(1)).getString();
    }
}
