import jason.asSyntax.*;
import jason.environment.Environment;
import jason.util.Pair;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AcesEnvironment extends Environment {
    private Stack<String> cardDeck = new Stack<>();
    private Map<String, String> agentDeal = new ConcurrentHashMap<>();
    private Map<String, String> agentNames = new ConcurrentHashMap<>();
    private int turn = 0;


    public AcesEnvironment() {
        super();


        agentNames.put("acesAndEights1", "Alice");
        agentNames.put("acesAndEights2", "Bob");
        agentNames.put("acesAndEights3", "Carl");

        cardDeck.add("A");
        cardDeck.add("A");
        cardDeck.add("A");
        cardDeck.add("A");

        cardDeck.add("E");
        cardDeck.add("E");
        cardDeck.add("E");
        cardDeck.add("E");

        Collections.shuffle(cardDeck);

    }

    private synchronized void dealCards() {
        if (!agentDeal.isEmpty())
            return;

        List<String> cards = new ArrayList<>();


        for (String agentName : super.getEnvironmentInfraTier().getRuntimeServices().getAgentsNames()) {
            String actualName = agentNames.get(agentName);

            agentDeal.compute(actualName, (agent, lastVal) -> {
                StringBuilder newCards = new StringBuilder();
                List<String> cardsTemp = new ArrayList<>();
                cardsTemp.add(cardDeck.pop());
                cardsTemp.add(cardDeck.pop());
                cardsTemp.sort(Comparator.naturalOrder());

                for (String c : cardsTemp)
                    newCards.append(c);

                cards.add(newCards.toString());
                return newCards.toString();
            });
        }

    }

    @Override
    public List<Literal> getPercepts(String agName) {
        Collection<Literal> ps = super.getPercepts(agName);
        List<Literal> percepts = ps == null ? new ArrayList<>() : new ArrayList<>(ps);
        //percepts.add(ASSyntax.createLiteral("hand", ASSyntax.createString("Alice"), ASSyntax.createString("AA")));

        var mapPercepts = getMapPercepts();
        var possibleSurroundings = getPossibleSurroundings(mapPercepts);

        // These are the surroundings that we KNOW
        for (Percept percept : mapPercepts.getPercepts()) {
            percepts.add(ASSyntax.createLiteral("location", ASSyntax.createNumber(percept.position.getFirst()), ASSyntax.createNumber(percept.position.getSecond()), percept.item.item));
        }

        // Better way to represent possibility here rather than inverse of set?
        for (var surroundingEntry : possibleSurroundings.entrySet()) {
            var location = surroundingEntry.getKey();
            var possiblePercepts = surroundingEntry.getValue();
            var possibleItems = new ArrayList<Percept.Item>();
            possiblePercepts.forEach(per -> possibleItems.add(per.item));

            // We use the inverted set to negate the impossible items
            var invSet = new HashSet<>(Percept.Item.valueSet());
            invSet.removeAll(possibleItems);

            for(Percept.Item item : invSet) {
                percepts.add(ASSyntax.createLiteral("location", ASSyntax.createNumber(location.getFirst()), ASSyntax.createNumber(location.getSecond()), item.item).setNegated(Literal.LNeg));
            }
        }

        return percepts;

    }

    /**
     * Get list of percepts that are possible for each surrounding location.
     *
     * @param mapPercepts The chunk of the map that is perceived (and known with 100% doc)
     * @return A map that provides a list of possible percepts for each location
     */
    private Map<Pair<Integer, Integer>, List<Percept>> getPossibleSurroundings(MapChunk mapPercepts) {
        var surroundingChunks = getMapSurroundings(mapPercepts);
        Map<Pair<Integer, Integer>, List<Percept>> mappedSurroundings = new HashMap<>();

        for (MapChunk chunk : surroundingChunks) {
            for (Percept p : chunk.getPercepts()) {
                mappedSurroundings.compute(p.position, (key, val) -> {
                    if (val == null)
                        val = new ArrayList<>();
                    val.add(p);
                    return val;
                });
            }
        }
        return mappedSurroundings;
    }

    /**
     * Stub: get surroundings from stored map (i.e. A* graph representation)
     *
     * @param mapPercepts
     * @return
     */
    private List<MapChunk> getMapSurroundings(MapChunk mapPercepts) {
        var chunk1 = new MapChunk(
                List.of(
                        new Percept(0, -1, Percept.Item.BLOCK)
                ));

        var chunk2 = new MapChunk(
                List.of(
                        new Percept(0, -1, Percept.Item.BLOCK)
                ));

        // Stub fake surroundings:
        return List.of(chunk1, chunk2);
    }

    private MapChunk getMapPercepts() {
        return new MapChunk(
                List.of(
                        new Percept(0, 1, Percept.Item.BLOCK)
                ));
    }

    @Override
    public boolean executeAction(String agName, Structure act) {
        if (act.getFunctor().equals("announce")) {
            System.out.println(agName + " Announcing");
            String action = ((StringTerm) act.getTerm(0)).getString();
            //worldRequest.performAction(action);
            turn = (turn + 1) % 3;
            return true;
        }

        return super.executeAction(agName, act);
    }

    /**
     * Called before the end of MAS execution
     */
    @Override
    public void stop() {
        super.stop();
    }


    public static class Percept {
        public enum Item {
            NONE(ASSyntax.createAtom("none")),
            BLOCK(ASSyntax.createAtom("block"));

            private Atom item;

            Item(Atom item) {
                this.item = item;
            }

            static Set<Item> valueSet() {
                return Set.of(Item.values());
            }

        }

        public Pair<Integer, Integer> position;
        public Item item;

        public Percept(int x, int y, Item item) {
            this.position = new Pair<>(x, y);
            this.item = item;
        }
    }

    private static class MapChunk {
        private List<Percept> percepts;

        public MapChunk(List<Percept> percepts) {
            this.percepts = percepts;
        }

        public List<Percept> getPercepts() {
            return percepts;
        }

        @Override
        public int hashCode() {
            return percepts.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj))
                return true;

            if (!(obj instanceof MapChunk))
                return false;

            MapChunk chunk = (MapChunk) obj;

            return chunk.percepts != null && chunk.percepts.equals(this.percepts);
        }
    }
}
