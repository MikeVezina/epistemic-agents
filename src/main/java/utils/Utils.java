package utils;

import jason.JasonException;
import jason.NoValueException;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.util.Pair;

public class Utils {
    public static String RelativeLocationToDirection(int x, int y) {
        String dir = "w";

        switch (x) {
            case -1:
                dir = "w";
                break;
            case 1:
                dir = "e";
                break;
            default:
                break;
        }

        switch (y) {
            case -1:
                dir = "n";
                break;
            case 1:
                dir = "s";
                break;
            default:
                break;
        }

        return dir;
    }

    public static String RelativeLocationToDirection(Position location) {
        return RelativeLocationToDirection(location.getX(), location.getY());
    }

    public static Position DirectionToRelativeLocation(String direction) {
        int x = 0;
        int y = 0;

        if (direction.equalsIgnoreCase("w"))
            x = -1;
        if (direction.equalsIgnoreCase("e"))
            x = 1;
        if (direction.equalsIgnoreCase("n"))
            y = -1;
        if (direction.equalsIgnoreCase("s"))
            y = 1;

        return new Position(x, y);

    }

    public static double SolveNumberTerm(Term t) {
        if (!(t instanceof NumberTermImpl))
            throw new NullPointerException("Term is not a NumberTermImpl");
        try {
            return ((NumberTerm) t).solve();
        } catch (NoValueException nVe) {
        	nVe.printStackTrace();
        	throw new NullPointerException("Failed to solve number term.");

        }
    }

    public static double Distance(Integer xArg, Integer yArg) {
        return Math.sqrt(Math.pow(xArg, 2) + Math.pow(yArg, 2));
    }
}
