package com.expriv.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentRowMapper implements RowMapper<Payment> {

    @Override
    public Payment mapRow(ResultSet rs, int rowNum) throws SQLException
    {


       Payment payment = new Payment();
       payment.setId(rs.getInt("ID"));
       payment.setUsername(rs.getString("user_name"));
       payment.setGenerate(rs.getInt("generate"));
       payment.setCode(rs.getString("code"));
       return payment;
    }
}
