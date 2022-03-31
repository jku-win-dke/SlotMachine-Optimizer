package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.acceptor;

import org.optaplanner.core.config.AbstractConfig;
import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorType;
import org.optaplanner.core.config.localsearch.decider.acceptor.LocalSearchAcceptorConfig;
import org.optaplanner.core.config.localsearch.decider.acceptor.stepcountinghillclimbing.StepCountingHillClimbingType;
import org.optaplanner.core.config.util.ConfigUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

@XmlType(
        propOrder = {"acceptorTypeList", "entityTabuSize", "entityTabuRatio", "fadingEntityTabuSize", "fadingEntityTabuRatio", "valueTabuSize", "valueTabuRatio", "fadingValueTabuSize", "fadingValueTabuRatio", "moveTabuSize", "fadingMoveTabuSize", "undoMoveTabuSize", "fadingUndoMoveTabuSize", "simulatedAnnealingStartingTemperature", "lateAcceptanceSize", "greatDelugeWaterLevelIncrementScore", "greatDelugeWaterLevelIncrementRatio", "stepCountingHillClimbingSize", "stepCountingHillClimbingType"}
)
public class AcceptorConfig extends AbstractConfig<AcceptorConfig> {
    @XmlElement(
            name = "acceptorType"
    )
    private List<AcceptorType> acceptorTypeList = null;
    protected Integer entityTabuSize = null;
    protected Double entityTabuRatio = null;
    protected Integer fadingEntityTabuSize = null;
    protected Double fadingEntityTabuRatio = null;
    protected Integer valueTabuSize = null;
    protected Double valueTabuRatio = null;
    protected Integer fadingValueTabuSize = null;
    protected Double fadingValueTabuRatio = null;
    protected Integer moveTabuSize = null;
    protected Integer fadingMoveTabuSize = null;
    protected Integer undoMoveTabuSize = null;
    protected Integer fadingUndoMoveTabuSize = null;
    protected String simulatedAnnealingStartingTemperature = null;
    protected Integer lateAcceptanceSize = null;
    protected String greatDelugeWaterLevelIncrementScore = null;
    protected Double greatDelugeWaterLevelIncrementRatio = null;
    protected Integer stepCountingHillClimbingSize = null;
    protected StepCountingHillClimbingType stepCountingHillClimbingType = null;
    public List<AcceptorType> getAcceptorTypeList() {
        return this.acceptorTypeList;
    }

    public void setAcceptorTypeList(List<AcceptorType> acceptorTypeList) {
        this.acceptorTypeList = acceptorTypeList;
    }

    public Integer getEntityTabuSize() {
        return this.entityTabuSize;
    }

    public void setEntityTabuSize(Integer entityTabuSize) {
        this.entityTabuSize = entityTabuSize;
    }

    public Double getEntityTabuRatio() {
        return this.entityTabuRatio;
    }

    public void setEntityTabuRatio(Double entityTabuRatio) {
        this.entityTabuRatio = entityTabuRatio;
    }

    public Integer getFadingEntityTabuSize() {
        return this.fadingEntityTabuSize;
    }

    public void setFadingEntityTabuSize(Integer fadingEntityTabuSize) {
        this.fadingEntityTabuSize = fadingEntityTabuSize;
    }

    public Double getFadingEntityTabuRatio() {
        return this.fadingEntityTabuRatio;
    }

    public void setFadingEntityTabuRatio(Double fadingEntityTabuRatio) {
        this.fadingEntityTabuRatio = fadingEntityTabuRatio;
    }

    public Integer getValueTabuSize() {
        return this.valueTabuSize;
    }

    public void setValueTabuSize(Integer valueTabuSize) {
        this.valueTabuSize = valueTabuSize;
    }

    public Double getValueTabuRatio() {
        return this.valueTabuRatio;
    }

    public void setValueTabuRatio(Double valueTabuRatio) {
        this.valueTabuRatio = valueTabuRatio;
    }

    public Integer getFadingValueTabuSize() {
        return this.fadingValueTabuSize;
    }

    public void setFadingValueTabuSize(Integer fadingValueTabuSize) {
        this.fadingValueTabuSize = fadingValueTabuSize;
    }

    public Double getFadingValueTabuRatio() {
        return this.fadingValueTabuRatio;
    }

    public void setFadingValueTabuRatio(Double fadingValueTabuRatio) {
        this.fadingValueTabuRatio = fadingValueTabuRatio;
    }

    public Integer getMoveTabuSize() {
        return this.moveTabuSize;
    }

    public void setMoveTabuSize(Integer moveTabuSize) {
        this.moveTabuSize = moveTabuSize;
    }

    public Integer getFadingMoveTabuSize() {
        return this.fadingMoveTabuSize;
    }

    public void setFadingMoveTabuSize(Integer fadingMoveTabuSize) {
        this.fadingMoveTabuSize = fadingMoveTabuSize;
    }

    public Integer getUndoMoveTabuSize() {
        return this.undoMoveTabuSize;
    }

    public void setUndoMoveTabuSize(Integer undoMoveTabuSize) {
        this.undoMoveTabuSize = undoMoveTabuSize;
    }

    public Integer getFadingUndoMoveTabuSize() {
        return this.fadingUndoMoveTabuSize;
    }

    public void setFadingUndoMoveTabuSize(Integer fadingUndoMoveTabuSize) {
        this.fadingUndoMoveTabuSize = fadingUndoMoveTabuSize;
    }

    public String getSimulatedAnnealingStartingTemperature() {
        return this.simulatedAnnealingStartingTemperature;
    }

    public void setSimulatedAnnealingStartingTemperature(String simulatedAnnealingStartingTemperature) {
        this.simulatedAnnealingStartingTemperature = simulatedAnnealingStartingTemperature;
    }

    public Integer getLateAcceptanceSize() {
        return this.lateAcceptanceSize;
    }

    public void setLateAcceptanceSize(Integer lateAcceptanceSize) {
        this.lateAcceptanceSize = lateAcceptanceSize;
    }

    public String getGreatDelugeWaterLevelIncrementScore() {
        return this.greatDelugeWaterLevelIncrementScore;
    }

    public void setGreatDelugeWaterLevelIncrementScore(String greatDelugeWaterLevelIncrementScore) {
        this.greatDelugeWaterLevelIncrementScore = greatDelugeWaterLevelIncrementScore;
    }

    public Double getGreatDelugeWaterLevelIncrementRatio() {
        return this.greatDelugeWaterLevelIncrementRatio;
    }

    public void setGreatDelugeWaterLevelIncrementRatio(Double greatDelugeWaterLevelIncrementRatio) {
        this.greatDelugeWaterLevelIncrementRatio = greatDelugeWaterLevelIncrementRatio;
    }

    public Integer getStepCountingHillClimbingSize() {
        return this.stepCountingHillClimbingSize;
    }

    public void setStepCountingHillClimbingSize(Integer stepCountingHillClimbingSize) {
        this.stepCountingHillClimbingSize = stepCountingHillClimbingSize;
    }

    public StepCountingHillClimbingType getStepCountingHillClimbingType() {
        return this.stepCountingHillClimbingType;
    }

    public void setStepCountingHillClimbingType(StepCountingHillClimbingType stepCountingHillClimbingType) {
        this.stepCountingHillClimbingType = stepCountingHillClimbingType;
    }

    public AcceptorConfig withAcceptorTypeList(List<AcceptorType> acceptorTypeList) {
        this.acceptorTypeList = acceptorTypeList;
        return this;
    }

    public AcceptorConfig withEntityTabuSize(Integer entityTabuSize) {
        this.entityTabuSize = entityTabuSize;
        return this;
    }

    public AcceptorConfig withEntityTabuRatio(Double entityTabuRatio) {
        this.entityTabuRatio = entityTabuRatio;
        return this;
    }

    public AcceptorConfig withFadingEntityTabuSize(Integer fadingEntityTabuSize) {
        this.fadingEntityTabuSize = fadingEntityTabuSize;
        return this;
    }

    public AcceptorConfig withFadingEntityTabuRatio(Double fadingEntityTabuRatio) {
        this.fadingEntityTabuRatio = fadingEntityTabuRatio;
        return this;
    }

    public AcceptorConfig withValueTabuSize(Integer valueTabuSize) {
        this.valueTabuSize = valueTabuSize;
        return this;
    }

    public AcceptorConfig withValueTabuRatio(Double valueTabuRatio) {
        this.valueTabuRatio = valueTabuRatio;
        return this;
    }

    public AcceptorConfig withFadingValueTabuSize(Integer fadingValueTabuSize) {
        this.fadingValueTabuSize = fadingValueTabuSize;
        return this;
    }

    public AcceptorConfig withFadingValueTabuRatio(Double fadingValueTabuRatio) {
        this.fadingValueTabuRatio = fadingValueTabuRatio;
        return this;
    }

    public AcceptorConfig withMoveTabuSize(Integer moveTabuSize) {
        this.moveTabuSize = moveTabuSize;
        return this;
    }

    public AcceptorConfig withFadingMoveTabuSize(Integer fadingMoveTabuSize) {
        this.fadingMoveTabuSize = fadingMoveTabuSize;
        return this;
    }

    public AcceptorConfig withUndoMoveTabuSize(Integer undoMoveTabuSize) {
        this.undoMoveTabuSize = undoMoveTabuSize;
        return this;
    }

    public AcceptorConfig withFadingUndoMoveTabuSize(Integer fadingUndoMoveTabuSize) {
        this.fadingUndoMoveTabuSize = fadingUndoMoveTabuSize;
        return this;
    }

    public  AcceptorConfig withSimulatedAnnealingStartingTemperature(String simulatedAnnealingStartingTemperature) {
        this.simulatedAnnealingStartingTemperature = simulatedAnnealingStartingTemperature;
        return this;
    }

    public AcceptorConfig withLateAcceptanceSize(Integer lateAcceptanceSize) {
        this.lateAcceptanceSize = lateAcceptanceSize;
        return this;
    }

    public AcceptorConfig withStepCountingHillClimbingSize(Integer stepCountingHillClimbingSize) {
        this.stepCountingHillClimbingSize = stepCountingHillClimbingSize;
        return this;
    }

    public AcceptorConfig withStepCountingHillClimbingType(StepCountingHillClimbingType stepCountingHillClimbingType) {
        this.stepCountingHillClimbingType = stepCountingHillClimbingType;
        return this;
    }

    @Override
    public AcceptorConfig inherit(AcceptorConfig inheritedConfig) {
        if (this.acceptorTypeList == null) {
            this.acceptorTypeList = inheritedConfig.getAcceptorTypeList();
        } else {
            List<AcceptorType> inheritedAcceptorTypeList = inheritedConfig.getAcceptorTypeList();
            if (inheritedAcceptorTypeList != null) {
                Iterator var3 = inheritedAcceptorTypeList.iterator();

                while(var3.hasNext()) {
                    AcceptorType acceptorType = (AcceptorType)var3.next();
                    if (!this.acceptorTypeList.contains(acceptorType)) {
                        this.acceptorTypeList.add(acceptorType);
                    }
                }
            }
        }

        this.entityTabuSize = (Integer) ConfigUtils.inheritOverwritableProperty(this.entityTabuSize, inheritedConfig.getEntityTabuSize());
        this.entityTabuRatio = (Double)ConfigUtils.inheritOverwritableProperty(this.entityTabuRatio, inheritedConfig.getEntityTabuRatio());
        this.fadingEntityTabuSize = (Integer)ConfigUtils.inheritOverwritableProperty(this.fadingEntityTabuSize, inheritedConfig.getFadingEntityTabuSize());
        this.fadingEntityTabuRatio = (Double)ConfigUtils.inheritOverwritableProperty(this.fadingEntityTabuRatio, inheritedConfig.getFadingEntityTabuRatio());
        this.valueTabuSize = (Integer)ConfigUtils.inheritOverwritableProperty(this.valueTabuSize, inheritedConfig.getValueTabuSize());
        this.valueTabuRatio = (Double)ConfigUtils.inheritOverwritableProperty(this.valueTabuRatio, inheritedConfig.getValueTabuRatio());
        this.fadingValueTabuSize = (Integer)ConfigUtils.inheritOverwritableProperty(this.fadingValueTabuSize, inheritedConfig.getFadingValueTabuSize());
        this.fadingValueTabuRatio = (Double)ConfigUtils.inheritOverwritableProperty(this.fadingValueTabuRatio, inheritedConfig.getFadingValueTabuRatio());
        this.moveTabuSize = (Integer)ConfigUtils.inheritOverwritableProperty(this.moveTabuSize, inheritedConfig.getMoveTabuSize());
        this.fadingMoveTabuSize = (Integer)ConfigUtils.inheritOverwritableProperty(this.fadingMoveTabuSize, inheritedConfig.getFadingMoveTabuSize());
        this.undoMoveTabuSize = (Integer)ConfigUtils.inheritOverwritableProperty(this.undoMoveTabuSize, inheritedConfig.getUndoMoveTabuSize());
        this.fadingUndoMoveTabuSize = (Integer)ConfigUtils.inheritOverwritableProperty(this.fadingUndoMoveTabuSize, inheritedConfig.getFadingUndoMoveTabuSize());
        this.simulatedAnnealingStartingTemperature = (String)ConfigUtils.inheritOverwritableProperty(this.simulatedAnnealingStartingTemperature, inheritedConfig.getSimulatedAnnealingStartingTemperature());
        this.lateAcceptanceSize = (Integer)ConfigUtils.inheritOverwritableProperty(this.lateAcceptanceSize, inheritedConfig.getLateAcceptanceSize());
        this.greatDelugeWaterLevelIncrementScore = (String)ConfigUtils.inheritOverwritableProperty(this.greatDelugeWaterLevelIncrementScore, inheritedConfig.getGreatDelugeWaterLevelIncrementScore());
        this.greatDelugeWaterLevelIncrementRatio = (Double)ConfigUtils.inheritOverwritableProperty(this.greatDelugeWaterLevelIncrementRatio, inheritedConfig.getGreatDelugeWaterLevelIncrementRatio());
        this.stepCountingHillClimbingSize = (Integer)ConfigUtils.inheritOverwritableProperty(this.stepCountingHillClimbingSize, inheritedConfig.getStepCountingHillClimbingSize());
        this.stepCountingHillClimbingType = (StepCountingHillClimbingType)ConfigUtils.inheritOverwritableProperty(this.stepCountingHillClimbingType, inheritedConfig.getStepCountingHillClimbingType());
        return this;
    }


    public AcceptorConfig copyConfig() {
        return (new AcceptorConfig()).inherit(this);
    }

    public void visitReferencedClasses(Consumer<Class<?>> classVisitor) {
    }

}
