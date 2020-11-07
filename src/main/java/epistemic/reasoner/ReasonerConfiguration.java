package epistemic.reasoner;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.logging.Logger;

public class ReasonerConfiguration {
    private static final String REASONER_CONFIG_JSON = "reasoner-config.json";
    private static final String DEFAULT_FULL_HOST = "http://localhost:9090";
    private static final String API_EVALUATE = "/api/evaluate";
    private static final String API_PROPS = "/api/props";
    private static final String API_MODEL = "/api/model";
    private static ReasonerConfiguration instance;
    private static final Logger LOGGER = Logger.getLogger("Reasoner Configuration");

    private String fullHost;

    protected ReasonerConfiguration(String fullHost) {
        this.fullHost = fullHost;
    }

    String getModelCreateEndpoint() {
        return fullHost + API_MODEL;
    }

    String getPropUpdateEndpoint() {
        return fullHost + API_PROPS;
    }

    String getEvaluateEndpoint() {
        return fullHost + API_EVALUATE;
    }

    public static ReasonerConfiguration getInstance() {

        if (instance == null) {
            try {
                Gson gson = new Gson();
                JsonReader reader = new JsonReader(new FileReader(REASONER_CONFIG_JSON));
                instance = gson.fromJson(reader, ReasonerConfiguration.class);
            } catch (FileNotFoundException e) {
                LOGGER.info("No reasoner configuration file '" + REASONER_CONFIG_JSON + "' found. Using default host and port.");
                instance = new ReasonerConfiguration(DEFAULT_FULL_HOST);
            }
        }
        return instance;
    }
}
