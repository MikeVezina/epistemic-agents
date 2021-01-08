package epistemic.reasoner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import epistemic.ManagedWorlds;
import epistemic.World;
import epistemic.distribution.formula.EpistemicFormula;
import epistemic.wrappers.WrappedLiteral;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;

public class ReasonerSDK {
    private static final String EVALUATE_RESULT_KEY = "result";
    private static final String UPDATE_PROPS_SUCCESS_KEY = "success";
    private static final String EVALUATION_FORMULA_RESULTS_KEY = "result";
    private static final int NS_PER_MS = 1000000;
    private final CloseableHttpClient client;
    private static final Logger LOGGER = Logger.getLogger(ReasonerSDK.class.getName());
    private final Logger metricsLogger = Logger.getLogger(getClass().getName() + " - Metrics");
    private final ReasonerConfiguration reasonerConfiguration;

    public ReasonerSDK(CloseableHttpClient client) {
        this.client = client;
        this.reasonerConfiguration = ReasonerConfiguration.getInstance();
    }

    public ReasonerSDK() {
        this(HttpClients.createDefault());
    }


    public void createModel(ManagedWorlds managedWorlds) {
        // Maybe have the managed worlds object be event-driven for information updates.
        JsonObject managedJson = new JsonObject();
        managedJson.add("initialModel", ManagedWorldsToJson(managedWorlds));

        LOGGER.info("Model Creation (Req. Body): " + managedJson.toString());
        metricsLogger.info("Creating model with " + managedWorlds.size() + " worlds");


        long initialTime = System.nanoTime();
        var request = RequestBuilder
                .post(reasonerConfiguration.getModelCreateEndpoint())
                .setEntity(new StringEntity(managedJson.toString(), ContentType.APPLICATION_JSON))
                .build();

        var resp = sendRequest(request, true);
        LOGGER.info("Model Post Response: " + resp.getStatusLine().toString());

        long creationTime = System.nanoTime() - initialTime;
        metricsLogger.info("Model creation time (ms): " + (creationTime / NS_PER_MS));
    }


    public Map<EpistemicFormula, Boolean> evaluateFormulas(Collection<EpistemicFormula> formulas) {
        Map<Integer, EpistemicFormula> formulaHashLookup = new HashMap<>();
        Map<EpistemicFormula, Boolean> formulaResults = new HashMap<>();

        if (formulas == null || formulas.isEmpty())
            return formulaResults;

        long initialTime = System.nanoTime();
        metricsLogger.info("Evaluating " + formulas.size() + " formulas");

        JsonObject formulaRoot = new JsonObject();
        JsonArray formulaArray = new JsonArray();

        for (EpistemicFormula formula : formulas) {
            formulaArray.add(toFormulaJSON(formula));
            formulaHashLookup.put(formula.hashCode(), formula);
        }

        formulaRoot.add("formulas", formulaArray);
        long jsonStringTime = System.nanoTime() - initialTime;
        metricsLogger.info("Formula JSON build time (ms): " + (jsonStringTime / NS_PER_MS));


        var req = RequestBuilder
                .post(reasonerConfiguration.getEvaluateEndpoint())
                .setEntity(new StringEntity(formulaRoot.toString(), ContentType.APPLICATION_JSON))
                .build();

        var resultJson = sendRequest(req, ReasonerSDK::jsonTransform).getAsJsonObject();

        long sendTime = System.nanoTime() - initialTime;
        metricsLogger.info("Reasoner formula evaluation time (ms): " + ((sendTime - jsonStringTime) / NS_PER_MS));

        // If the result is null, success == false, or there is no result entry, then return an empty set.
        if (resultJson == null || !resultJson.has(EVALUATION_FORMULA_RESULTS_KEY))
            return formulaResults;

        var resultPropsJson = resultJson.getAsJsonObject(EVALUATION_FORMULA_RESULTS_KEY);

        for (var key : resultPropsJson.entrySet()) {
            int formulaHashValue = Integer.parseInt(key.getKey());
            Boolean formulaValuation = key.getValue().getAsBoolean();

            // Get the formula associated with the hash in the response
            var trueFormula = formulaHashLookup.getOrDefault(formulaHashValue, null);

            if (trueFormula == null)
                LOGGER.warning("Failed to lookup formula: " + key.getKey());
            else
                formulaResults.put(trueFormula, formulaValuation);
        }

        return formulaResults;
    }

    /**
     * Updates the currently believed propositions
     *
     * @param propositionValues The list of believed props.
     * @param epistemicFormulas The formulas to evaluate immediately after updating the propositions.
     * @return The formula evaluation after updating the propositions. This will be empty if no formulas are provided.
     */
    public Map<EpistemicFormula, Boolean> updateProps(Set<Set<WrappedLiteral>> propositionValues, Collection<EpistemicFormula> epistemicFormulas) {

        if (propositionValues == null)
            throw new IllegalArgumentException("propositions list should not be null");

        long initialUpdateTime = System.nanoTime();

        JsonArray propValuation = new JsonArray();

        for (Set<WrappedLiteral> currentValues : propositionValues) {

            // Don't add anything if there is no knowledge/known possibility
            if (currentValues.isEmpty())
                continue;

            JsonObject propObject = new JsonObject();

            for (var prop : currentValues) {
                var propName = prop.toSafePropName();
                propObject.addProperty(propName, !prop.getCleanedLiteral().negated());
            }

            propValuation.add(propObject);
        }

        JsonObject bodyElement = new JsonObject();
        bodyElement.add("props", propValuation);

        var req = RequestBuilder
                .put(reasonerConfiguration.getPropUpdateEndpoint())
                .setEntity(new StringEntity(bodyElement.toString(), ContentType.APPLICATION_JSON))
                .build();

        long jsonStringTime = System.nanoTime() - initialUpdateTime;
        metricsLogger.info("Prop JSON build time (ms): " + (jsonStringTime / NS_PER_MS));

        var resultJson = sendRequest(req, ReasonerSDK::jsonTransform).getAsJsonObject();

        long totalTime = System.nanoTime() - initialUpdateTime;
        metricsLogger.info("Reasoner Update Time (ms): " + ((totalTime - jsonStringTime) / NS_PER_MS));

        if (resultJson == null || !resultJson.has(UPDATE_PROPS_SUCCESS_KEY) || !resultJson.get(UPDATE_PROPS_SUCCESS_KEY).getAsBoolean())
            LOGGER.warning("Failed to successfully update props: " + bodyElement.toString());
        else
            LOGGER.info("Updated props successfully. Request Body: " + bodyElement.toString());

        return evaluateFormulas(epistemicFormulas);
    }


    /**
     * Sends the request without closing the response.
     *
     * @param request
     * @return
     */
    CloseableHttpResponse sendRequest(HttpUriRequest request, boolean shouldClose) {

        try {
            var res = client.execute(request);

            if (shouldClose)
                res.close();

            return res;
        } catch (IOException e) {

            throw new RuntimeException("Failed to connect to the reasoner!", e);
        }
    }

    /**
     * Sends a request, processes the response and closes the response stream.
     *
     * @param request
     * @param responseProcessFunc
     * @param <R>
     * @return
     */
    private <R> R sendRequest(HttpUriRequest request, @NotNull Function<CloseableHttpResponse, R> responseProcessFunc) {
        try (var res = sendRequest(request, false)) {
            return responseProcessFunc.apply(res);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    static JsonElement jsonTransform(CloseableHttpResponse response) {
        try {
            BufferedInputStream bR = new BufferedInputStream(response.getEntity().getContent());
            String jsonStr = new String(bR.readAllBytes());
            return (new JsonParser()).parse(jsonStr).getAsJsonObject();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    static JsonObject ManagedWorldsToJson(ManagedWorlds managedWorlds) {
        JsonObject modelObject = new JsonObject();

        JsonArray worldsArray = new JsonArray();

        Map<Integer, World> hashed = new HashMap<>();

        for (World world : managedWorlds) {
            if (hashed.containsKey(world.hashCode())) {
                var oldW = hashed.get(world.hashCode());
                LOGGER.warning("Hashing collision. The worlds: " + oldW + " and " + world + " have then same hash but are not equal.");
            }

            hashed.put(world.hashCode(), world);
            worldsArray.add(WorldToJson(world));
        }

        modelObject.add("worlds", worldsArray);

        // TODO : Change this to the hashcode of an actual pointed world.
        // No pointed world, the epistemic.reasoner will choose one at random.
        // modelObject.addProperty("pointedWorld", getWorldName(managedWorlds.getPointedWorld()));
        return modelObject;
    }

    private static JsonObject WorldToJson(World world) {
        JsonObject worldObject = new JsonObject();
        JsonArray propsArray = new JsonArray();

        worldObject.addProperty("name", world.getUniqueName());
        for (WrappedLiteral wrappedLiteral : world) {
            propsArray.add(String.valueOf(wrappedLiteral.toSafePropName()));
        }
        worldObject.add("props", propsArray);

        return worldObject;
    }

    /**
     * Returns a JSON element containing data for a formula.
     * The JSON element should encode:
     * - ID of formula
     * - Epistemic Modality Type ("know" or "possible")
     * - Negation of Modality (i.e. "~possible")
     * - Contained Proposition (i.e. cards["Alice", "AA"])
     * - Proposition Negation (i.e. ~cards["Alice", "AA"])
     *
     * @param formula
     * @return
     */
    static JsonElement toFormulaJSON(EpistemicFormula formula) {
        var jsonElement = new JsonObject();
        jsonElement.addProperty("id", formula.hashCode());

        jsonElement.addProperty("modalityNegated", formula.isModalityNegated());
        jsonElement.addProperty("modality", formula.getEpistemicModality().getFunctor());

        jsonElement.addProperty("propNegated", formula.isPropositionNegated());
        jsonElement.addProperty("prop", formula.getAtomicProposition());


        return jsonElement;
    }
}
