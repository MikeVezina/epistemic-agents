import jason.JasonException;
import jason.infra.centralised.RunCentralisedMAS;

public class ProjectRunner {
    public static void main(String[] args) throws JasonException {

        RunCentralisedMAS.main(new String[] {"epistemicagents.mas2j"});
    }
}
