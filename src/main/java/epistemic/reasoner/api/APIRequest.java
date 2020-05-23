package epistemic.reasoner.api;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.http.HttpClient;

public abstract class APIRequest<T> {
    protected static final String HOST = "http://localhost:9090";
    private final CloseableHttpClient client;
    protected APIRequest(CloseableHttpClient client)
    {
        this.client = client;
    }

    protected abstract HttpUriRequest createRequest();

    protected abstract T receivedResponse(CloseableHttpResponse response);

    public final T sendRequest(boolean shouldClose)
    {
        var request = createRequest();

        try {
            var res = client.execute(request);

            if (shouldClose)
                res.close();

            return receivedResponse(res);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public T sendRequest()
    {
        return sendRequest(true);
    }

}
