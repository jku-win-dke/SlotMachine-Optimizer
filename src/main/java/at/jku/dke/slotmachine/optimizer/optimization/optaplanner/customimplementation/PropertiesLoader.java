package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
        public static final String USE_AUTHENTIC_PRIVACY_ENGINE = "useAuthenticPrivacyEngine";
        public static final String STEP_COUNTING_SIZE = "stepCountingSize";
        public static final String MINIMUM_ACCEPTED_COUNT_LIMIT = "minimumAcceptedCountLimit";

        public static Properties loadProperties(String resourceFileName) throws IOException, IOException {
            Properties configuration = new Properties();
            InputStream inputStream = PropertiesLoader.class
                    .getClassLoader()
                    .getResourceAsStream(resourceFileName);
            configuration.load(inputStream);
            inputStream.close();
            return configuration;
        }
}
