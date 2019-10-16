package eis.functions;

import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.Option;
import jason.asSyntax.Atom;

import java.util.List;
import java.util.stream.Collectors;

public class CustomAgent extends Agent {
    @Override
    public Option selectOption(List<Option> options) {

        // We want to look for plans that are non-default handlers
        // Specifically used in action handling
        if(options.size() > 1)
        {
            List<Option> defaultOptions = options.stream().filter(o -> o.getPlan().getLabel().hasAnnot(new Atom("default"))).collect(Collectors.toList());
            List<Option> nonDefaultOptions = options.stream().filter(o -> !o.getPlan().getLabel().hasAnnot(new Atom("default"))).collect(Collectors.toList());

            if(!nonDefaultOptions.isEmpty())
                return super.selectOption(nonDefaultOptions);
            else
                return super.selectOption(defaultOptions);
        }

        return super.selectOption(options);
    }
}
