package epistemic.reasoner;

import epistemic.reasoner.utils.RequestWrapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static epistemic.reasoner.utils.RequestWrapper.ReqMethod.GET;
import static epistemic.reasoner.utils.RequestWrapper.ReqMethod.POST;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ReasonerSDKTest {


    private static final int MODEL_SUCCESS = 200;
    private ReasonerSDK mockedReasoner;
    private Map<RequestWrapper, CloseableHttpResponse> requestHashToResponse;

    private static Stream<Arguments> createModelFixtures() {


        return Stream.of(
                // Need to pass in managed worlds
                Arguments.of(POST, "{}", MODEL_SUCCESS, "")
        );
    }

    @BeforeEach
    public void setUp() throws Exception {
        this.mockedReasoner = createMockReasonerSDK();
        this.requestHashToResponse = new HashMap<>();
    }



    @ParameterizedTest
    @MethodSource(value = "createModelFixtures")
    public void createModel() {
        // Need to create ManagedWorlds.... do these tests after managedworlds

    }

    @Test
    public void evaluateFormulas() {
    }

    @Test
    public void updateProps() {
    }

    @Test
    public void managedWorldsToJson() {
    }

    @Test
    public void worldToJson() {
    }

    /**
     * Creates a mocked/spy ReasonerSDK object.
     * Only mocks the {@link ReasonerSDK#sendRequest(HttpUriRequest, boolean)} method,
     * since that is called by all other methods to send an HTTPUriRequest to the reasoner.
     * The mock object utilized the response hashmap to return the corresponding response.
     *
     * @return A spy ReasonerSDK object with the request method patched.
     */
    private ReasonerSDK createMockReasonerSDK() {
        var mockedReasoner = Mockito.spy(ReasonerSDK.class);

        // Stubs the http responses
        // Maps a request
        doAnswer(invocation -> {
            var requestObject = (HttpUriRequest) invocation.getArgument(0);
            var reqKey = new RequestWrapper(requestObject);

            if(!requestHashToResponse.containsKey(reqKey))
                throw new RuntimeException("The request is not contained in the request mapping: " + requestObject.toString());

            // Return the response object for the corresponding request hash
            return requestHashToResponse.get(reqKey);
        }).when(mockedReasoner).sendRequest(argThat(Objects::nonNull), anyBoolean());

        return mockedReasoner;
    }
}

