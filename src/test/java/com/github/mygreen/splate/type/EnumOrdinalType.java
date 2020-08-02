package com.github.mygreen.splate.type;

/**
 * 列挙型の変換規則。
 * ordinalに変換する。
 *
 *
 * @author T.TSUCHIE
 *
 */
public class EnumOrdinalType<T extends Enum<T>> implements SqlTemplateValueType<T> {

    @Override
    public Object getBindVariableValue(T value) throws SqlTypeConversionException {
        Integer sqlValue = (value != null ? value.ordinal() : null);
        return sqlValue;
    }
}
