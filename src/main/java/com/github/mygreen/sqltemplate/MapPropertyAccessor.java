package com.github.mygreen.sqltemplate;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.AbstractNestablePropertyAccessor;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;

/**
 * ルートオブジェクトがマップの場合に直接キー名でアクセスするためのアクセッサです。
 * <p>ネストした要素へのアクセスは {@link DirectFieldAccessor} を使用します。</p>
 *
 * @author T.TSUCHIE
 *
 */
public class MapPropertyAccessor extends AbstractNestablePropertyAccessor {

    private final Map<String, Object> rootObject;

    private final Map<String, MapHandler> handlerMap = new HashMap<>();

    /**
     * 参照対象のオブジェクトを指定してインスタンスを作成する。
     * @param object 参照対象のオブジェクト
     */
    public MapPropertyAccessor(final Map<String, Object> object) {
        super(object);
        this.rootObject = object;
    }

    @Override
    protected PropertyHandler getLocalPropertyHandler(String propertyName) {

        // キーを指定してマップの要素を取得する。
        final Object targetValue = rootObject.get(propertyName);
        if(targetValue == null) {
            return null;
        }

        // マップの要素をプロパティとして扱う。
        MapHandler handler = handlerMap.computeIfAbsent(propertyName,
                key -> new MapHandler(rootObject, propertyName, targetValue.getClass()));
        return handler;
    }

    @Override
    protected AbstractNestablePropertyAccessor newNestedPropertyAccessor(Object object, String nestedPath) {
        // マップの要素へのアクセスは DirectFieldAccessor に委譲する。
        return new DirectFieldAccessor(object);
    }

    @Override
    protected NotWritablePropertyException createNotWritablePropertyException(String propertyName) {
        return new NotWritablePropertyException(rootObject.getClass(), propertyName);
    }

    private class MapHandler extends PropertyHandler {

        private final Map<String, Object> map;

        private final String key;

        private final Class<?> type;

        public MapHandler(Map<String, Object> map, String key, Class<?> type) {
            // 参照のみサポート
            super(type, true, false);
            this.map = map;
            this.key = key;
            this.type = type;
        }

        @Override
        public TypeDescriptor toTypeDescriptor() {
            return TypeDescriptor.valueOf(type);
        }

        @Override
        public ResolvableType getResolvableType() {
            return ResolvableType.forClass(type);
        }

        @Override
        public TypeDescriptor nested(int level) {
            return TypeDescriptor.valueOf(type);
        }

        @Override
        public Object getValue() throws Exception {
            return map.get(key);
        }

        @Override
        public void setValue(Object value) throws Exception {
            map.put(key, value);
        }

    }

}
