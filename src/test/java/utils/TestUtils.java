package utils;

import wrappers.LiteralKey;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public final class TestUtils {
    public static Map<LiteralKey, LinkedList<Literal>> createHandEnumeration(String agent, String... values) {
        var map = new HashMap<LiteralKey, LinkedList<Literal>>();
        var key = createHandWithVariable(agent);
        var valueList = new LinkedList<Literal>();

        for (String val : values) {
            valueList.add(createHandWithValue(agent, val).getLiteral());
        }

        map.put(key, valueList);
        return map;
    }

    public static Map<LiteralKey, Literal> createHandEntry(String agent, String value) {

        var map = new HashMap<LiteralKey, Literal>();

        var key = createHandWithVariable(agent);
        var val = createHandWithValue(agent, value);
        map.put(key, val.getLiteral());

        return map;
    }

    public static LiteralKey createHandWithValue(String termOne, String termTwo) {
        return new LiteralKey(ASSyntax.createLiteral("hand", ASSyntax.createString(termOne), ASSyntax.createString(termTwo)));
    }

    public static LiteralKey createHandWithVariable(String termOne, String varName) {
        return new LiteralKey(ASSyntax.createLiteral("hand", ASSyntax.createString(termOne), ASSyntax.createVar(varName)));
    }

    public static LiteralKey createHandWithVariable(String termOne) {
        return new LiteralKey(ASSyntax.createLiteral("hand", ASSyntax.createString(termOne), ASSyntax.createVar()));
    }
}
