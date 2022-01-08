package com.github.mygreen.splate;

import java.util.Map;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypedValue;
import org.springframework.util.Assert;

import lombok.RequiredArgsConstructor;


/**
 * マップ用のアクセッサー。
 *
 * @since 0.3
 * @author T.TSUCHIE
 *
 */
@RequiredArgsConstructor
public class CustomMapAccessor extends MapAccessor {

    /**
     * 存在しないキーが指定された場合、無視するかどうか。
     */
    private final boolean ignoreNotFoundProperty;

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

        if(target == null && ignoreNotFoundProperty) {
            return new TypedValue(null);
        }

        Assert.state(target instanceof Map, "Target must be of type Map");
        Map<?, ?> map = (Map<?, ?>) target;
        Object value = map.get(name);
        if (value == null && !map.containsKey(name) && ignoreNotFoundProperty) {
            return new TypedValue(null);
        } else {
            return super.read(context, target, name);
        }
    }
}
