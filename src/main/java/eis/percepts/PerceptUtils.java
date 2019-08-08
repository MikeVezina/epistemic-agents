package eis.percepts;

import eis.iilang.Identifier;
import eis.iilang.Parameter;
import eis.iilang.Percept;

import java.util.LinkedList;
import java.util.List;

public final class PerceptUtils {

    /**
     * Checks the first parameter of the percept to see if the name matches. The first parameter must be of
     * type Identifier
     * @param p The percept to check.
     * @param name The name of the identifier
     * @return True if the identifier name matches the parameter name, false otherwise.
     */
    public static boolean MatchPerceptFirstIdentifier(Percept p, String name) {
        Parameter firstParam = GetFirstParameter(p);

        if(firstParam == null)
            return false;

        if(!(firstParam instanceof Identifier))
            return false;

        /* Ensures the action result was a success */
        String paramValue = ((Identifier) firstParam).getValue();
        return paramValue.equalsIgnoreCase(name);
    }

    public static Parameter GetFirstParameter(Percept p)
    {
        if(p == null)
            return null;

        LinkedList<Parameter> parameterList = p.getParameters();

        if(parameterList == null || parameterList.size() == 0)
            return null;

        return parameterList.getFirst();
    }
}
