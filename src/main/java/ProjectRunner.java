import jason.JasonException;
import jason.asSyntax.ASSyntax;
import jason.infra.centralised.RunCentralisedMAS;

public class ProjectRunner {
    public static void main(String[] args) throws JasonException {

        var base = ASSyntax.createLiteral("welcome", ASSyntax.createVar());
        var lit = ASSyntax.createLiteral("~hand", base);
        var lit2 = ASSyntax.createLiteral("hand", base.copy().setNegated(false));

        RunCentralisedMAS.main(new String[] {"epistemic-agents.mas2j"});
    }
}
