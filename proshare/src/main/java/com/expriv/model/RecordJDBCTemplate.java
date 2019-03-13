package com.expriv.model;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

public class RecordJDBCTemplate implements RecordDAO {

  private JdbcTemplate jdbcTemplateObject;

  public void setDataSource(DataSource dataSource) {
    this.jdbcTemplateObject = new JdbcTemplate(dataSource);
  }

  public void create(String user_name, Integer display_status) {
    String insertQuery = "insert into users_image_record (user_name, display_status) values (?, ?)";
    jdbcTemplateObject.update(insertQuery, user_name, display_status);
    System.out
        .println("Created Record User name=" + user_name + "Display Status=" + display_status);
    return;
  }

  public List<Record> listRecords() {
    String SQL = "select * from users_image_record";
    List<Record> records = jdbcTemplateObject.query(SQL, new RecordRowMapper());
    return records;
  }
}
