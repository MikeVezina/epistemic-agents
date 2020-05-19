package epistemic.reasoner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import epistemic.wrappers.WrappedLiteral;
import epistemic.ManagedWorlds;
import epistemic.World;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class ReasonerSDK {
    private static final String HOST = "http://localhost:9090";
    private static final String CREATE_MODEL_URI = HOST + "/api/model";
    private static final String UPDATE_PROPS = HOST + "/api/props";
    private static final String MODEL_CHECK_URI = HOST + "/api/evaluate";
    private static final String EVALUATE_RESULT_KEY = "result";
    private static final String UPDATE_PROPS_SUCCESS_KEY = "success";
    private static final String UPDATE_PROPS_RESULT_KEY = "result";
    private final CloseableHttpClient client = HttpClients.createDefault();

    public ReasonerSDK() {
    }


    public void createModel(ManagedWorlds managedWorlds) {
        // Maybe have the managed worlds object be event-driven for information updates.
        JsonObject managedJson = new JsonObject();
        managedJson.add("initialModel", ManagedWorldsToJson(managedWorlds));

        System.out.println(managedJson.toString());

        var request = RequestBuilder
                .post(CREATE_MODEL_URI)
                .setEntity(new StringEntity(managedJson.toString(), ContentType.APPLICATION_JSON))
                .build();

        var resp = sendRequest(request, true);
        System.out.println("Model Post Response: " + resp.getStatusLine().toString());
    }


    public boolean evaluateFormula(String formula) {
        var req = RequestBuilder
                .get(MODEL_CHECK_URI)
                .addParameter("formula", formula)
                .build();

        var resultJson = sendRequest(req, ReasonerSDK::jsonTransform).getAsJsonObject();
        return (resultJson != null && resultJson.has(EVALUATE_RESULT_KEY)) && resultJson.get(EVALUATE_RESULT_KEY).getAsBoolean();
    }

    /**
     * Updates the currently believed propositions
     * @param props THe list of believed props.
     * @return A set of newly-inferred props from the epistemic.reasoner.
     */
    public Set<String> updateProps(List<String> props) {
        if (props == null)
            throw new IllegalArgumentException("Props should not be null");

        // Convert list to string array
        String[] propsArr = new String[props.size()];
        props.toArray(propsArr);

        return updateProps(propsArr);
    }

    public Set<String> updateProps(String[] props) {
        JsonArray propsArray = new JsonArray();

        for (String prop : props)
            propsArray.add(prop);

        var req = RequestBuilder
                .put(UPDATE_PROPS)
                .setEntity(new StringEntity(propsArray.toString(), ContentType.APPLICATION_JSON))
                .build();

        var resultJson = sendRequest(req, ReasonerSDK::jsonTransform).getAsJsonObject();

        Set<String> inferredProps = new HashSet<>();

        // If the result is null, success == false, or there is no result entry, then return an empty set.
        if(resultJson == null || !resultJson.has(UPDATE_PROPS_SUCCESS_KEY) || !resultJson.get(UPDATE_PROPS_SUCCESS_KEY).getAsBoolean() || !resultJson.has(UPDATE_PROPS_RESULT_KEY))
            return inferredProps;

        var resultPropsJson = resultJson.getAsJsonObject(UPDATE_PROPS_RESULT_KEY);

        // Only look at true props (for now)
        // Todo: Adjust for enumerations with false values
        for(var key : resultPropsJson.entrySet())
        {
            if(key.getValue().getAsBoolean())
            {
                inferredProps.add(key.getKey());
            }

        }

        return inferredProps;
    }

    /**
     * Sends the request without closing the response.
     *
     * @param request
     * @return
     */
    private CloseableHttpResponse sendRequest(HttpUriRequest request, boolean shouldClose) {

        try {
            var res = client.execute(request);

            if (shouldClose)
                res.close();

            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a request, processes the response and closes the response stream.
     * @param request
     * @param responseProcessFunc
     * @param <R>
     * @return
     */
    private <R> R sendRequest(HttpUriRequest request, @NotNull Function<CloseableHttpResponse, R> responseProcessFunc) {
        try (var res = sendRequest(request, false))
        {
            return responseProcessFunc.apply(res);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

    private static JsonElement jsonTransform(CloseableHttpResponse response) {
        try {
            BufferedInputStream bR = new BufferedInputStream(response.getEntity().getContent());
            String jsonStr = new String(bR.readAllBytes());
            return (new JsonParser()).parse(jsonStr).getAsJsonObject();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    public static JsonObject ManagedWorldsToJson(ManagedWorlds managedWorlds) {
        JsonObject modelObject = new JsonObject();

        JsonArray worldsArray = new JsonArray();
        JsonArray edgesArray = new JsonArray();

        for (World world : managedWorlds) {
            worldsArray.add(WorldToJson(world));
            edgesArray.addAll(CreateEdges(world));

        }

        modelObject.add("worlds", worldsArray);

        // TODO : Change this to the hashcode of an actual pointed world.
        // No pointed world, the epistemic.reasoner will choose one at random.
        // modelObject.addProperty("pointedWorld", getWorldName(managedWorlds.getPointedWorld()));
        return modelObject;
    }

    public static JsonObject WorldToJson(World world) {
        JsonObject worldObject = new JsonObject();
        JsonArray propsArray = new JsonArray();

        worldObject.addProperty("name", world.getUniqueName());
        for (WrappedLiteral wrappedLiteral : world.wrappedValueSet()) {
            propsArray.add(String.valueOf(wrappedLiteral.toSafePropName()));
        }
        worldObject.add("props", propsArray);

        return worldObject;
    }

    private static JsonArray CreateEdges(World world) {
        var element = new JsonArray();

        for (var accessibleWorldEntries : world.getAccessibleWorlds().entrySet()) {
            var name = accessibleWorldEntries.getKey();
            var worlds = accessibleWorldEntries.getValue();

            for (var accWorld : worlds) {
                var edgeElem = new JsonObject();
                edgeElem.addProperty("agentName", name);
                edgeElem.addProperty("worldOne", world.getUniqueName());
                edgeElem.addProperty("worldTwo", accWorld.getUniqueName());
                element.add(edgeElem);
            }
        }

        return element;
    }
}
