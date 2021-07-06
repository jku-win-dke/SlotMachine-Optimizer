package at.jku.dke.slotmachine.optimizer.optimization;

public class InvalidParameterTypeException extends Exception {
    private String parameter;
    private Class expectedType;
    private Class actualType;

    public InvalidParameterTypeException(String  parameter, Class expectedType, Class actualType) {
        this.parameter = parameter;
        this.expectedType = expectedType;
        this.actualType = actualType;
    }
}
