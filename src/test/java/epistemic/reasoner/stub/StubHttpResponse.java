package epistemic.reasoner.stub;

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

public class StubHttpResponse extends BasicHttpResponse implements CloseableHttpResponse
{
    private static final ProtocolVersion MOCK_VERSION = new ProtocolVersion("mocked", 1, 0);
    private static final StatusLine OK_STATUS = createStatus(200, null);
    private static final JsonParser JSON_PARSER = new JsonParser();
    private boolean isClosed;

    private StubHttpResponse(StatusLine status) {
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

    public static StubHttpResponse createMockResponse(HttpEntity entity)
    {
        var mocked = new StubHttpResponse(OK_STATUS);
        mocked.setEntity(entity);
        return mocked;
    }

    public static StubHttpResponse createMockResponse(String content, ContentType type)
    {
        return createMockResponse(new StringEntity(content,type));
    }

    public static StubHttpResponse createMockResponse(JsonElement response)
    {
        return createMockResponse(new StringEntity(response.toString(), ContentType.APPLICATION_JSON));
    }

    public static StubHttpResponse createJsonResponse(String response)
    {
        return createMockResponse(JSON_PARSER.parse(response));
    }


}
