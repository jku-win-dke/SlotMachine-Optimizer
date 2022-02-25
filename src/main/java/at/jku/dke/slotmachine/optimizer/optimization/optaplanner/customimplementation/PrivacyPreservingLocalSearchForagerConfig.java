package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import org.optaplanner.core.config.localsearch.decider.forager.FinalistPodiumType;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchPickEarlyType;
import org.optaplanner.core.config.util.ConfigUtils;

import javax.xml.bind.annotation.XmlType;
import java.util.function.Consumer;

@XmlType(propOrder = {
        "pickEarlyType",
        "acceptedCountLimit",
        "finalistPodiumType",
        "breakTieRandomly"
})
public class PrivacyPreservingLocalSearchForagerConfig extends LocalSearchForagerConfig {
    protected LocalSearchPickEarlyType pickEarlyType = null;
    protected Integer acceptedCountLimit = null;
    protected FinalistPodiumType finalistPodiumType = null;
    protected Boolean breakTieRandomly = null;

    public LocalSearchPickEarlyType getPickEarlyType() {
        return pickEarlyType;
    }

    public void setPickEarlyType(LocalSearchPickEarlyType pickEarlyType) {
        this.pickEarlyType = pickEarlyType;
    }

    public Integer getAcceptedCountLimit() {
        return acceptedCountLimit;
    }

    public void setAcceptedCountLimit(Integer acceptedCountLimit) {
        this.acceptedCountLimit = acceptedCountLimit;
    }

    public FinalistPodiumType getFinalistPodiumType() {
        return finalistPodiumType;
    }

    public void setFinalistPodiumType(FinalistPodiumType finalistPodiumType) {
        this.finalistPodiumType = finalistPodiumType;
    }

    public Boolean getBreakTieRandomly() {
        return breakTieRandomly;
    }

    public void setBreakTieRandomly(Boolean breakTieRandomly) {
        this.breakTieRandomly = breakTieRandomly;
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public LocalSearchForagerConfig withPickEarlyType(LocalSearchPickEarlyType pickEarlyType) {
        this.pickEarlyType = pickEarlyType;
        return this;
    }

    public LocalSearchForagerConfig withAcceptedCountLimit(int acceptedCountLimit) {
        this.acceptedCountLimit = acceptedCountLimit;
        return this;
    }

    public LocalSearchForagerConfig withFinalistPodiumType(FinalistPodiumType finalistPodiumType) {
        this.finalistPodiumType = finalistPodiumType;
        return this;
    }

    public LocalSearchForagerConfig withBreakTieRandomly(boolean breakTieRandomly) {
        this.breakTieRandomly = breakTieRandomly;
        return this;
    }

    @Override
    public LocalSearchForagerConfig inherit(LocalSearchForagerConfig inheritedConfig) {
        pickEarlyType = ConfigUtils.inheritOverwritableProperty(pickEarlyType,
                inheritedConfig.getPickEarlyType());
        acceptedCountLimit = ConfigUtils.inheritOverwritableProperty(acceptedCountLimit,
                inheritedConfig.getAcceptedCountLimit());
        finalistPodiumType = ConfigUtils.inheritOverwritableProperty(finalistPodiumType,
                inheritedConfig.getFinalistPodiumType());
        breakTieRandomly = ConfigUtils.inheritOverwritableProperty(breakTieRandomly,
                inheritedConfig.getBreakTieRandomly());
        return this;
    }

    @Override
    public LocalSearchForagerConfig copyConfig() {
        return new LocalSearchForagerConfig().inherit(this);
    }

    @Override
    public void visitReferencedClasses(Consumer<Class<?>> classVisitor) {
        // No referenced classes
        // TODO: custom forager ??
    }


}
