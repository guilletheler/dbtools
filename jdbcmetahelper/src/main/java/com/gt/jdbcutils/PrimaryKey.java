package com.gt.jdbcutils;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
public class PrimaryKey {
    Table table;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Column> columns = new ArrayList<>();
}
