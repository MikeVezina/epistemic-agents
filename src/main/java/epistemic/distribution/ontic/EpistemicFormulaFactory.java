package epistemic.distribution.ontic;

import epistemic.agent.EpistemicAgent;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;

public class EpistemicFormulaFactory {
    public static String fromTerm(EpistemicAgent epistemicAgent, Term expr, Unifier un) {
        String ret = "";

        if (expr instanceof InternalActionLiteral)
            ret = "bb:" + expr;
        else if (expr instanceof LogExpr)
            ret = fromLogExpr(epistemicAgent, (LogExpr) expr, un);
        else if (expr instanceof RelExpr) {
            var rel = (RelExpr) expr;
            if(!rel.capply(un).isGround())
                return "false";
            else
                return "true";
        }
        else if (expr instanceof Literal) {
            if (((Literal) expr).negated()) {
                ret = "not ";
                ((Literal) expr).setNegated(Literal.LPos);
            }

            ret += expr.toString().replaceAll("\\(", "[").replaceAll("\\)", "]");
        } else
            ret = expr.toString();

        return ret;
    }


    /**
     * Expects an expression (expr) that is a logical consequence of the belief base (wrt unifier un)
     *
     * @param epistemicAgent
     * @param expr
     * @param un
     * @return
     */
    public static String fromLogExpr(EpistemicAgent epistemicAgent, LogExpr expr, Unifier un) {
        // Be careful with not x(...) because it will never be ground.
        //

        if(expr.getOp() == LogExpr.LogicalOp.or)
        {
            var newExp = new LogExpr(LogExpr.LogicalOp.not, new LogExpr(new LogExpr(LogExpr.LogicalOp.not, expr.getLHS()), LogExpr.LogicalOp.and, new LogExpr(LogExpr.LogicalOp.not, expr.getRHS())));
            System.out.println();
        }


        StringBuilder res = new StringBuilder();

        switch (expr.getOp()) {
            case none:
                break;

            case not:
                // Assume everything in not is for the belief base
                if (!expr.getLHS().logicalConsequence(epistemicAgent, un).hasNext()) {
                    res.append("NOT (").append(fromTerm(epistemicAgent, expr.getLHS(), un)).append(")");
                }
                break;
            case and:
                res.append("(");
                res.append(fromTerm(epistemicAgent, expr.getLHS(), un));
                res.append(") AND (");
                res.append(fromTerm(epistemicAgent, expr.getRHS(), un));
                res.append(")");
                break;
            case or:
                res.append("(");
                res.append(fromTerm(epistemicAgent, expr.getLHS(), un));
                res.append(") OR (");
                res.append(fromTerm(epistemicAgent, expr.getRHS(), un));
                res.append(")");
                break;
        }
        System.out.println(res);
        return res.toString();
    }
}
