package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
        private static String authenticPrivacyEngineKey = "useAuthenticPrivacyEngine";
        private static String stepCountingSizeKey = "stepCountingSize";

        public static Properties loadProperties(String resourceFileName) throws IOException, IOException {
            Properties configuration = new Properties();
            InputStream inputStream = PropertiesLoader.class
                    .getClassLoader()
                    .getResourceAsStream(resourceFileName);
            configuration.load(inputStream);
            inputStream.close();
            return configuration;
        }

    public static String getAuthenticPrivacyEngineKey() {
        return authenticPrivacyEngineKey;
    }

    public static String getStepCountingSizeKey() {
        return stepCountingSizeKey;
    }
}
