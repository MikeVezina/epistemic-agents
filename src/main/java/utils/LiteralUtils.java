package utils;

import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import jason.asSyntax.*;

import java.util.LinkedList;
import java.util.List;

public final class LiteralUtils {

    /**
     * Checks the first parameter of the percept to see if the name matches. The first parameter must be of
     * type Identifier
     * @param p The Literal to check.
     * @param name The name of the identifier
     * @return True if the identifier name matches the parameter name, false otherwise.
     */
    public static boolean MatchPerceptFirstString(Literal p, String name) {
        Term firstParam = GetFirstParameter(p);

        if(firstParam == null)
            return false;

        if(!(firstParam instanceof StringTermImpl))
            return false;

        /* Ensures the action result was a success */
        String paramValue = ((Identifier) firstParam).getValue();
        return paramValue.equalsIgnoreCase(name);
    }

    public static Term GetParameter(Literal p, int index)
    {
        if(p == null)
            return null;

        List<Term> parameterList = p.getTerms();

        if(parameterList == null || parameterList.isEmpty())
            return null;

        return parameterList.get(index);
    }

    public static Term GetFirstParameter(Literal p)
    {
        return GetParameter(p, 0);
    }


    public static Number GetNumberParameter(Literal p, int index)
    {
        Term parameter = LiteralUtils.GetParameter(p, index);

        if(!(parameter instanceof NumberTermImpl))
            throw new RuntimeException("First parameter is not a numeral");

        return ((NumberTermImpl)parameter).solve();
    }

    public static Number GetFirstNumberParameter(Literal p)
    {
        return GetNumberParameter(p, 0);
    }

    public static String GetStringParameter(Literal p, int index)
    {
        Term parameter = LiteralUtils.GetParameter(p, index);

        if(parameter instanceof StringTermImpl)
            return ((StringTermImpl)parameter).getString();

        if(parameter instanceof Atom)
            return ((Atom)parameter).getFunctor();

        return null;
    }

    public static boolean GetBooleanParameter(Literal percept, int index) {
        String parameter = LiteralUtils.GetStringParameter(percept, index).toLowerCase();

        if(!parameter.equalsIgnoreCase(Boolean.TRUE.toString()) && !parameter.equalsIgnoreCase(Boolean.FALSE.toString()))
            throw new RuntimeException("Percept parameter was not boolean. Percept: " + percept);

        return Boolean.parseBoolean(parameter);
    }
}
