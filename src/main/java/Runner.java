import epistemic.distribution.formula.EpistemicFormula;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;

public class Runner {

    public static void main(String[] args)
    {
        var formula = EpistemicFormula.fromLiteral(ASSyntax.createLiteral("possible", ASSyntax.createLiteral("cards")));
        System.out.println(formula.hashCode());

        formula = EpistemicFormula.fromLiteral(ASSyntax.createLiteral("poss", ASSyntax.createLiteral("cards")));
        System.out.println(formula.hashCode());

        formula = EpistemicFormula.fromLiteral(ASSyntax.createLiteral("possible", ASSyntax.createLiteral("cards").setNegated(Literal.LNeg)));
        System.out.println(formula.hashCode());

        formula = EpistemicFormula.fromLiteral(ASSyntax.createLiteral("possible", ASSyntax.createLiteral("cards")).setNegated(Literal.LNeg));
        System.out.println(formula.hashCode());

        formula = EpistemicFormula.fromLiteral(ASSyntax.createLiteral("possible", ASSyntax.createLiteral("cards").setNegated(Literal.LNeg)).setNegated(Literal.LNeg));
        System.out.println(formula.hashCode());

        formula = EpistemicFormula.fromLiteral(ASSyntax.createLiteral("poss", ASSyntax.createLiteral("cards")).setNegated(Literal.LNeg));
        System.out.println(formula.hashCode());
    }
}
