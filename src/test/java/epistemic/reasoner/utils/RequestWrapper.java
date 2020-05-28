package epistemic.reasoner.utils;

import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RequestWrapper {
    public enum ReqMethod {
        GET,
        POST
    }

    private final HttpUriRequest request;

    public RequestWrapper(@NotNull HttpUriRequest request) {
        this.request = request;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequestWrapper)) return false;

        RequestWrapper that = (RequestWrapper) o;
        return Objects.equals(request.getMethod(), that.request.getMethod()) &&
                Objects.equals(request.getURI().getPath(), that.request.getURI().getPath()) &&
                Objects.equals(request.getURI().getQuery(), that.request.getURI().getQuery());
    }

    @Override
    public int hashCode() {
        return Objects.hash(request.getMethod(), request.getURI().getPath(), request.getURI().getQuery());
    }
}
