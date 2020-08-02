package com.github.mygreen.splate.type;

/**
 * 列挙型の変換規則。
 *  nameに変換する。
 *
 *
 * @author T.TSUCHIE
 *
 */
public class EnumNameType<T extends Enum<T>> implements SqlTemplateValueType<T> {

    @Override
    public Object getBindVariableValue(T value) throws SqlTypeConversionException {
        String sqlValue = (value != null ? value.name() : null);
        return sqlValue;
    }
}
