package com.github.mygreen.splate;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomReflectivePropertyAccessor extends ReflectivePropertyAccessor {

    /**
     * 存在しないプロパティが指定された場合、無視するかどうか。
     */
    private final boolean ignoreNotFoundProperty;

    private static final String NOT_FOUNE_MESSAGE = "Neither getter method nor field found for property";

    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
        if(ignoreNotFoundProperty) {
            return true;
        } else {
            return super.canRead(context, target, name);
        }
    }

    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {

        try {
            return super.read(context, target, name);
        } catch(AccessException e) {
            if(e.getMessage().contains(NOT_FOUNE_MESSAGE) && ignoreNotFoundProperty) {
                return new TypedValue(null);
            } else {
                throw e;
            }
        }

    }


}
