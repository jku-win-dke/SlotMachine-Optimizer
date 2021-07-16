package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import at.jku.dke.slotmachine.optimizer.OptimizerApplication;
import at.jku.dke.slotmachine.optimizer.Utils;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.solver.SolverConfig;

public class OptaplannerOptimizationConfiguration extends OptimizationConfiguration {
    private static final Logger logger = LogManager.getLogger();

    public SolverConfig getSolverConfig() {
        String optaplannerConfigurations = System.getProperty(OptimizerApplication.OPTAPLANNER_CONFIGURATIONS);

        SolverConfig solverConfig = null;

        if(this.getConfigurationName() != null) {
            logger.info("Load the solver configuration from the XML resource for " + this.getConfigurationName());
            String solverConfigResource = Utils.getMapFromJson(optaplannerConfigurations).get(this.getConfigurationName());

            logger.info("Read the solver configuration resource: " + solverConfigResource);
            solverConfig = SolverConfig.createFromXmlResource(solverConfigResource);
        }

        return solverConfig;
    }

    public void setConfigurationName(String configurationName) {
        this.setParameter("configurationName", configurationName);
    }

    public String getConfigurationName() {
        return this.getStringParameter("configurationName");
    }

    public Long getSecondsSpentLimit() {
        return this.getLongParameter("secondsSpentLimit");
    }

    public void setSecondsSpentLimit(Long secondsSpentLimit) {
        this.setParameter("secondsSpentLimit", secondsSpentLimit);
    }
}
