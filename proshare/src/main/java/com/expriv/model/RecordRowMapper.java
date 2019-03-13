package com.expriv.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class RecordRowMapper implements RowMapper<Record>
{
    @Override
    public Record mapRow(ResultSet rs, int rowNum) throws SQLException
    {

        Record record=new Record();

        record.setId(rs.getInt("ID"));
        record.setUser_name(rs.getString("user_name"));
        record.setImage_id(rs.getString("image_id"));
        record.setImage_path(rs.getString("image_path"));
        record.setSharing_decision(rs.getInt("sharing_decision"));
        record.setDisplay_status(rs.getInt("display_status"));

        return record;


    }

}