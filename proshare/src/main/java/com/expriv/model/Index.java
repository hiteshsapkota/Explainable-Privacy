package com.expriv.model;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public class Index {
    private int id;
    private String username;
    private JdbcTemplate jdbcTemplate;
    private float trainCompleted;
    private float evalCompleted;
    private int trainBatchSize;
    private int evalBatchSize;


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
        if (principal instanceof UserDetails)

            this.username = ((UserDetails) principal).getUsername();


        else
            this.username = principal.toString();


    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public float getTrainCompleted() {
        return trainCompleted;
    }

    public void setTrainCompleted(float trainCompleted) {
        this.trainCompleted = trainCompleted;
    }

    public float getEvalCompleted() {
        return evalCompleted;
    }

    public void setEvalCompleted(float evalCompleted) {
        this.evalCompleted = evalCompleted;
    }

    public int getTrainBatchSize() {
        return trainBatchSize;
    }

    public void setTrainBatchSize(int trainBatchSize) {
        this.trainBatchSize = trainBatchSize;
    }

    public int getEvalBatchSize() {
        return evalBatchSize;
    }

    public void setEvalBatchSize(int evalBatchSize) {
        this.evalBatchSize = evalBatchSize;
    }



    public void setProgress()
    {
        try
        {

            String sql = "select * from training where user_name=?";
            List<Record> records = jdbcTemplate.query(sql, new Object[] {username}, new RecordRowMapper());
            if (records.isEmpty())
            {

                this.trainCompleted = 0;

            }
            else
            {
                int trainRecord = records.size();
                sql = "select * from training where display_status=0 and user_name=?";
                records = jdbcTemplate.query(sql, new Object[] {username}, new RecordRowMapper());
                if (records.isEmpty())
                {

                    this.trainCompleted = 100;

                }
                else
                {

                    this.trainCompleted = ((float) (records.size()%this.trainBatchSize)/this.trainBatchSize)*100;

                }

            }

            sql = "select * from evaluation where user_name=?";
            records = jdbcTemplate.query(sql, new Object[] {username}, new RecordRowMapper());
            if (records.isEmpty())
            {
                this.evalCompleted = 0;

            }
            else
            {
                int evalRecord = records.size();
                sql = "select * from evaluation where display_status=0 and user_name=?";
                records = jdbcTemplate.query(sql, new Object[] {username}, new RecordRowMapper());
                if (records.isEmpty())
                {
                    this.evalCompleted = 100;

                }
                else
                {
                    this.evalCompleted = ((float)(records.size()%this.evalBatchSize)/this.evalBatchSize)*100;
                }

            }

        } catch (Exception e)
        {
            System.out.println(e);
        }

    }
}
