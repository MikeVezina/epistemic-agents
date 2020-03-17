import jason.JasonException;
import jason.infra.centralised.RunCentralisedMAS;
import map.Position;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ProjectRunner {
    public static void main(String[] args) throws JasonException {

        RunCentralisedMAS.main(new String[] {"massim2019.mas2j"});
    }
}
