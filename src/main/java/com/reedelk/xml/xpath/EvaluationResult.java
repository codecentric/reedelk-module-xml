package com.reedelk.xml.xpath;

public class EvaluationResult {

    private final String expression;
    private final Object result;

    public EvaluationResult(String expression, Object result) {
        this.expression = expression;
        this.result = result;
    }

    public String getExpression() {
        return expression;
    }

    public Object getResult() {
        return result;
    }
}
