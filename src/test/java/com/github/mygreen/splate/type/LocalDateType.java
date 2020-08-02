package com.github.mygreen.splate.type;

import java.sql.Date;
import java.time.LocalDate;

/**
 * {@link LocalDate} の変換規則。
 *
 * @since 0.2
 * @author T.TSUCHIE
 *
 */
public class LocalDateType implements SqlTemplateValueType<LocalDate> {

    @Override
    public Object getBindVariableValue(LocalDate value) throws SqlTypeConversionException {
        Date sqlValue = (value != null ? Date.valueOf(value) : null);
        return sqlValue;
    }
}
