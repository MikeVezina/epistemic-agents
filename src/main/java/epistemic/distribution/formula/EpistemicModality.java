package epistemic.distribution.formula;

public enum EpistemicModality {
    KNOW("know"),
    POSSIBLE("possible");


    private final String functor;
    EpistemicModality(String functor) {
        this.functor = functor;
    }

    public boolean isFunctor(String functor) {
        return this.functor.equals(functor);
    }

    /**
     * Finds the first functor enum value with the given functor.
     *
     * @param functor The string functor to look for in enum values.
     * @return The first corresponding enum value or null if the functor could not be found.
     */
    public static EpistemicModality findFunctor(String functor) {
        for (EpistemicModality functorVal : EpistemicModality.values()) {
            if (functorVal.isFunctor(functor))
                return functorVal;
        }
        return null;
    }

    public String getFunctor() {
        return functor;
    }
}
