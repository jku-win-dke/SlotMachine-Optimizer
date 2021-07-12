package at.jku.dke.slotmachine.optimizer.optimization;

public class InvalidOptimizationParameterTypeException extends Exception {
    private String parameter;
    private Class expectedType;
    private Class actualType;

    public InvalidOptimizationParameterTypeException(String  parameter, Class expectedType) {
        this.parameter = parameter;
        this.expectedType = expectedType;
    }

    public InvalidOptimizationParameterTypeException(String  parameter, Class expectedType, Class actualType) {
        this.parameter = parameter;
        this.expectedType = expectedType;
        this.actualType = actualType;
    }
}
