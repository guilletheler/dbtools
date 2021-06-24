package com.gt.jdbcutils;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
public class Index {
    Table table;
    boolean unique;
    String name;
    
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Column> columns = new ArrayList<>();

}
