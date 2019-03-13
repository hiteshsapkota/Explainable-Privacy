package net.javaguides.springboot.springsecurity.model;

import javax.sql.DataSource;
import java.util.List;

public interface RecordDAO {
    public void setDataSource(DataSource ds);

    public void create(String user_name, Integer display_status);
    public List<Record> listRecords();
}
