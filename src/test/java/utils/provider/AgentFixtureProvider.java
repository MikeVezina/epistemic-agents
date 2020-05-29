package utils.provider;

import epistemic.agent.stub.StubAgArch;
import jason.JasonException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.Preconditions;

import java.io.InputStream;
import java.util.stream.Stream;

public class AgentFixtureProvider implements ArgumentsProvider, AnnotationConsumer<AgentFixtureTest> {

    private String aslFixture;
    private ExtensionContext ctx;

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        this.ctx = context;
        return getAgArchArgs();
        // @formatter:on
    }

    private Stream<Arguments> getAgArchArgs() {
        return Stream.of(aslFixture)
                .map(resource -> openInputStream(ctx, resource))
                .map(this::beginParsing)
                .flatMap(this::toStream);
    }

    private Stream<Arguments> toStream(StubAgArch stubAgArch) {
        return Stream.of(
                Arguments.of(stubAgArch)
        );
    }

    private StubAgArch beginParsing(InputStream inputStream) {
        StubAgArch agArch = new StubAgArch(false);
        try {
            agArch.getAgSpy().load(inputStream, aslFixture);
        } catch (JasonException e) {
            throw new RuntimeException("Failed to create agent from stream. ", e);
        }

        return agArch;
    }

    private InputStream openInputStream(ExtensionContext context, String resource) {
        Preconditions.notBlank(resource, "Classpath resource [" + resource + "] must not be null or blank");
        Class<?> testClass = context.getRequiredTestClass();
        return Preconditions.notNull(testClass.getResourceAsStream(resource),
                () -> "Classpath resource [" + resource + "] does not exist");
    }

    @Override
    public void accept(AgentFixtureTest agentFixtureTest) {
        var aslFile = agentFixtureTest.asl();

        if(aslFile.isEmpty())
            this.aslFixture = null;
        else
            this.aslFixture = "/fixtures/asl/" + aslFile;

    }
}
