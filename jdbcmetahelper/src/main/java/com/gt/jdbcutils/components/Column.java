package com.gt.jdbcutils.components;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
public class Column {
    @ToString.Exclude
    Table table;
    String name;
    Integer dataType;
    Integer largo;
    Integer decimalDigits;
    boolean nullable;
    boolean isAutoIncrement;
    Long nextAutoincVal;
}
