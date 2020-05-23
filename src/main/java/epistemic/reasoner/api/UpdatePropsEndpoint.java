package epistemic.reasoner.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import epistemic.formula.EpistemicLiteral;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.List;

public class UpdatePropsEndpoint extends APIRequest<UpdatePropsEndpoint.UpdatePropsResponse> {

    private static final String UPDATE_PROPS = HOST + "/api/props";
    private final List<String> props;
    private final List<EpistemicLiteral> epistemicLiterals;

    public static class UpdatePropsResponse {

    }

    public UpdatePropsEndpoint(CloseableHttpClient client, List<String> props, List<EpistemicLiteral> epistemicLiterals)
    {
        super(client);
        this.props = props;
        this.epistemicLiterals = epistemicLiterals;
    }

    @Override
    protected HttpUriRequest createRequest() {
        JsonArray propsArray = new JsonArray();
        JsonArray formulasArray = new JsonArray();

        for (String prop : props)
            propsArray.add(prop);

        for(var formula : epistemicLiterals)
            formulasArray.add(formula.toFormulaJSON());

        JsonObject bodyElement = new JsonObject();
        bodyElement.add("props", propsArray);
        bodyElement.add("formulas", formulasArray);

        var req = RequestBuilder
                .put(UPDATE_PROPS)
                .setEntity(new StringEntity(bodyElement.toString(), ContentType.APPLICATION_JSON))
                .build();

        return req;
    }

    @Override
    protected void receivedResponse(CloseableHttpResponse response) {

    }
}
