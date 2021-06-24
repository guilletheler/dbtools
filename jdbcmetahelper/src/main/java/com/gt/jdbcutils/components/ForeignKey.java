package com.gt.jdbcutils.components;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
public class ForeignKey {
    String name;
    Table table;
    Table refTable;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Column> columns = new ArrayList<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Column> refColumns = new ArrayList<>();

}
