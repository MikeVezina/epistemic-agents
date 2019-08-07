package eis.internal;

import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import utils.Utils;

public class AttachedDAO {
    private static final String ATTACHED_FUNCTOR = "attached";
    private static final int X_TERM_IDX = 0;
    private static final int Y_TERM_IDX = 1;
    private Literal attachedLiteral;
    private int x = 0;
    private int y = 0;

    public AttachedDAO(Literal attached)
    {
        if(attached == null || !attached.getFunctor().equalsIgnoreCase(ATTACHED_FUNCTOR) || attached.getArity() != 2)
            throw new NullPointerException("Invalid Literal: " + attached);

        this.x = (int) Utils.SolveNumberTerm(attached.getTerm(X_TERM_IDX));
        this.y = (int) Utils.SolveNumberTerm(attached.getTerm(Y_TERM_IDX));

        this.attachedLiteral = attached.copy();
    }

    public Literal getLiteral() {
        return attachedLiteral;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
        this.attachedLiteral.setTerm(X_TERM_IDX, new NumberTermImpl(x));
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
        this.attachedLiteral.setTerm(Y_TERM_IDX, new NumberTermImpl(y));
    }

}
