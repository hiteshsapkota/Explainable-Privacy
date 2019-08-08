package com.expriv.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UnderstandabilityRowMapper implements RowMapper<Understandability> {
    @Override
    public Understandability mapRow(ResultSet rs, int rowNum) throws SQLException
    {

       Understandability understandability = new Understandability();
       understandability.setId(rs.getInt("ID"));
       understandability.setUsername(rs.getString("user_name"));
       understandability.setAttribute(rs.getString("attribute"));
       understandability.setUsed(rs.getInt("used"));
       understandability.setScore(rs.getInt("score"));
       return understandability;
    }


}
