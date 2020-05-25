import jason.JasonException;
import jason.asSyntax.ASSyntax;
import jason.infra.centralised.RunCentralisedMAS;

public class ProjectRunner {
    public static void main(String[] args) throws JasonException {

        RunCentralisedMAS.main(new String[] {"epistemic-agents.mas2j"});
    }
}
