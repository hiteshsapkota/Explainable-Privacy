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
    private String description;

    private int id;
    private String button_type;
    private JdbcTemplate jdbcTemplate;
    private String username;
    private int trainingInstances;
    private Index index;
    private boolean update;
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

    public Index getIndex() {
        Index index = new Index();
        ConfigurationService configurationService = new ConfigurationService();
        configurationService.setParams();
        index.setTrainBatchSize(configurationService.getTrain_batch_size());
        index.setUsername(this.username);
        index.setJdbcTemplate(jdbcTemplate);
        index.setProgress();
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
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

    public boolean isUpdate() {
        return update;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUpdate(boolean update) {
        this.update = update;
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
                this.image_id="na";
            }
            else {

                Record record=records.get(0);
                this.id = record.getId();
                this.image_path = record.getImage_path();
                this.description=record.getDescription();
                this.image_id=record.getImage_id();


            }

        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }


    public void getPrevious()
    {
        try
        {
            String username=this.username;
            ConfigurationService configurationService=new ConfigurationService();
            configurationService.setParams();
            String sql = "select * from training where id=?";
            List<Record> records=jdbcTemplate.query(sql, new Object[] { (this.id-1) },new RecordRowMapper());

            if (!records.isEmpty()) {
                System.out.println("The record is not empty");
                Record record=records.get(0);
                if (record.getUser_name().equals(this.username))
                {
                    System.out.println("I am here");
                    this.id = record.getId();
                    this.image_path = record.getImage_path();
                    this.image_id=record.getImage_id();
                    this.description=record.getDescription();
                }

            }

        }
        catch (Exception e)
        {
            System.out.println(e);
        }

    }

    public void getNext()
    {
        try
        {
        String username=this.username;
        ConfigurationService configurationService=new ConfigurationService();
        configurationService.setParams();
        String sql = "select * from training where id=?";
        List<Record> records=jdbcTemplate.query(sql, new Object[] { (this.id+1) },new RecordRowMapper());
        this.id =0;
        this.image_path="na";
        if (!records.isEmpty()) {
            Record record=records.get(0);
            if (record.getUser_name().equals(this.username))
            {

                this.id = record.getId();
                this.image_path = record.getImage_path();
                this.description=record.getDescription();
            }

        }

    }
        catch (Exception e)
    {
        System.out.println(e);
    }
    }


    public String getButton_type() {
        return button_type;
    }

    public void setButton_type(String button_type) {
        this.button_type = button_type;
    }

    public void updateDisplayStatus()
    {
        System.out.println("Updating Display Status");
        System.out.println(this.id);
        String update_query="update training set display_status = 1  where id ="+Integer.toString(this.id);
        jdbcTemplate.update(update_query);
    }

    public void storeSharing_type()
    {
        System.out.println("Storing Sharing type");
        System.out.println(jdbcTemplate);
        try
        {


            if (button_type.equals("Share"))
            {
                System.out.println("Updating sharing decision");
                System.out.println(this.id);
                jdbcTemplate.update("update training set sharing_decision = 1  where id ="+Integer.toString(this.id)+";");
            }
            else if (button_type.equals("Do Not Share"))
            {
                System.out.println("Updating sharing decision");
                jdbcTemplate.update("update training set sharing_decision = 0  where id ="+Integer.toString(this.id)+";");
            }
            String update_query="update training set display_status = 1  where id ="+Integer.toString(this.id);
            jdbcTemplate.update(update_query);



        } catch (Exception e)
        {
            System.out.println(e);
        }

    }

}
