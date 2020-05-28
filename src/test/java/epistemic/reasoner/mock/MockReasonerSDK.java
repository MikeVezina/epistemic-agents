package epistemic.reasoner.mock;

import epistemic.ManagedWorlds;
import epistemic.formula.EpistemicFormula;
import epistemic.reasoner.ReasonerSDK;
import epistemic.wrappers.WrappedLiteral;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;

public class MockReasonerSDK extends ReasonerSDK {
    private ManagedWorlds curManagedWorlds;
    private final Map<EpistemicFormula, Boolean> formulaValuations;
    private Set<WrappedLiteral> currentPropositionValues;

    public MockReasonerSDK()
    {
        super(mock(CloseableHttpClient.class));
        this.formulaValuations = new HashMap<>();
        currentPropositionValues = new HashSet<>();
    }

    @Override
    public void createModel(ManagedWorlds managedWorlds) {
        this.curManagedWorlds = managedWorlds;
        this.formulaValuations.clear();
        this.currentPropositionValues.clear();
    }

    public void setFormulaValuation(EpistemicFormula formula, boolean value)
    {
        formulaValuations.put(formula, value);
    }


    public ManagedWorlds getCurrentModel() {
        return curManagedWorlds;
    }

    public Set<WrappedLiteral> getCurrentPropositionValues() {
        return currentPropositionValues;
    }

    @Override
    public Map<EpistemicFormula, Boolean> evaluateFormulas(Collection<EpistemicFormula> formulas) {
        // Map all formulas to evaluate to true
        return formulas.stream()
                .map(formula -> new AbstractMap.SimpleEntry<>(formula, formulaValuations.getOrDefault(formula, false)))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    @Override
    public Map<EpistemicFormula, Boolean> updateProps(Collection<WrappedLiteral> propositionValues, Collection<EpistemicFormula> epistemicFormulas) {
        this.currentPropositionValues = Set.copyOf(propositionValues);
        return this.evaluateFormulas(epistemicFormulas);
    }
}
