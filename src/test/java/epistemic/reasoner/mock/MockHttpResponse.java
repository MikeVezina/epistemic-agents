package epistemic.reasoner.mock;

import com.google.gson.*;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class MockHttpResponse extends BasicHttpResponse implements CloseableHttpResponse
{
    private static final ProtocolVersion MOCK_VERSION = new ProtocolVersion("mocked", 1, 0);
    private static final StatusLine OK_STATUS = createStatus(200, null);
    private static final JsonParser JSON_PARSER = new JsonParser();
    private boolean isClosed;

    private MockHttpResponse(StatusLine status) {
        super(status);
        isClosed = false;
    }

    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
    }

    private static StatusLine createStatus(int code, @Nullable String reason)
    {
        return new BasicStatusLine(MOCK_VERSION, code, reason);
    }

    public static MockHttpResponse createMockResponse(HttpEntity entity)
    {
        var mocked = new MockHttpResponse(OK_STATUS);
        mocked.setEntity(entity);
        return mocked;
    }

    public static MockHttpResponse createMockResponse(String content, ContentType type)
    {
        return createMockResponse(new StringEntity(content,type));
    }

    public static MockHttpResponse createMockResponse(JsonElement response)
    {
        return createMockResponse(new StringEntity(response.toString(), ContentType.APPLICATION_JSON));
    }

    public static MockHttpResponse createJsonResponse(String response)
    {
        return createMockResponse(JSON_PARSER.parse(response));
    }


}
