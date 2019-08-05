package com.expriv.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AttributeRowMapper implements RowMapper<Attribute> {

    @Override
    public Attribute mapRow(ResultSet rs, int rowNum) throws SQLException
    {
        System.out.println("Mapping row");

        Attribute attribute=new Attribute();
        attribute.setId(rs.getString("ID"));
        attribute.setName(rs.getString("name"));
        attribute.setDescription(rs.getString("description"));
        return attribute;

    }
}
