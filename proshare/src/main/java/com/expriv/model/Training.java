package com.expriv.model;

import com.expriv.service.ConfigurationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public class Training{

    private String image_id;
    private String image_path;
    private int id;
    private String sharing_type;
    private JdbcTemplate jdbcTemplate;
    private String username;
    private int trainingInstances;
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(Object principal) {

        if (principal instanceof UserDetails) {
            this.username = ((UserDetails)principal).getUsername();
        }
        else {
            this.username = principal.toString();
        }
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }


    public int getTrainingInstances() {
        return trainingInstances;
    }

    public void setTrainingInstances() {

        String username=this.username;
        String sql = "select * from training where display_status=1 and user_name=?";
        List<Record> records=jdbcTemplate.query(sql, new Object[] { username },new RecordRowMapper());
        this.trainingInstances=records.size();

    }

    public void readId() {
        try
        {

            String username=this.username;
            ConfigurationService configurationService=new ConfigurationService();
            configurationService.setParams();
            String sql = "select * from training where display_status=0 and user_name=?";
            List<Record> records=jdbcTemplate.query(sql, new Object[] { username },new RecordRowMapper());

            if (records.isEmpty())
            {
                this.id=0;
                this.image_path="na";
            }
            else {

                Record record=records.get(0);
                this.id = record.getId();
                this.image_path = record.getImage_path();
                String update_query="update training set display_status = 1  where id ="+Integer.toString(this.id);
                jdbcTemplate.update(update_query);
            }

        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public String getSharing_type() {
        return sharing_type;
    }

    public void setSharing_type(String sharing_type) {
        this.sharing_type = sharing_type;

    }
    public void storeSharing_type()
    {
        try
        {
            if (sharing_type.equals("share"))
            {
                jdbcTemplate.update("update training set sharing_decision = 1  where id ="+Integer.toString(this.id)+";");
            }
            else if (sharing_type.equals("not_share"))
            {
                jdbcTemplate.update("update training set sharing_decision = 0  where id ="+Integer.toString(this.id)+";");
            }

        } catch (Exception e)
        {
            System.out.println(e);
        }

    }

}
