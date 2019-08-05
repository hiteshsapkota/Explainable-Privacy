package com.expriv.model;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public class Index {
    private int id;
    private String username;
    private JdbcTemplate jdbcTemplate;
    private String trainCompleted;
    private String evalCompleted;
    private String trainSkipped;
    private String evalSkipped;
    private String trainRemaining;
    private String evalRemaining;
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

    public String getTrainCompleted() {
        return trainCompleted;
    }

    public void setTrainCompleted(String trainCompleted) {
        this.trainCompleted = trainCompleted;
    }

    public String getEvalCompleted() {
        return evalCompleted;
    }

    public void setEvalCompleted(String evalCompleted) {
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

    public String getTrainSkipped() {
        return trainSkipped;
    }

    public void setTrainSkipped(String trainSkipped) {
        this.trainSkipped = trainSkipped;
    }
    public String getEvalSkipped() {
        return evalSkipped;
    }

    public void setEvalSkipped(String evalSkipped) {
        this.evalSkipped = evalSkipped;
    }

    public String getTrainRemaining() {
        return trainRemaining;
    }

    public void setTrainRemaining(String trainRemaining) {
        this.trainRemaining = trainRemaining;
    }

    public String getEvalRemaining() {
        return evalRemaining;
    }

    public void setEvalRemaining(String  evalRemaining) {
        this.evalRemaining = evalRemaining;
    }

    public int getInstances(String sql)
    {
        List <Record> records = jdbcTemplate.query(sql, new Object[] {username}, new RecordRowMapper());
        if (records.isEmpty())
            return 0;
        else
        return (int)records.size();
    }

    public void setProgress()
    {
        try {

            String sql = "select * from training where user_name=?";
            int noInstances =50;
            //int noInstances = getInstances(sql);
            if (noInstances == 0) {

                this.trainCompleted = Integer.toString(0);
                this.trainSkipped = Integer.toString(0);
                this.trainRemaining = Integer.toString(0);

            } else {
                sql = "select * from training where display_status=1 and sharing_decision is not NULL and user_name=?";
                this.trainCompleted = Integer.toString(getInstances(sql));
                sql = "select * from training where display_status=1 and sharing_decision is NULL and user_name=?";
                this.trainSkipped = Integer.toString(getInstances(sql));
                this.trainRemaining = Integer.toString(((Integer.parseInt(this.trainCompleted)+Integer.parseInt(this.trainSkipped))/noInstances+1)*noInstances-Integer.parseInt(this.trainCompleted)-Integer.parseInt(this.trainSkipped));


            }

            sql = "select * from evaluation where user_name=?";
            noInstances = getInstances(sql);
            if (noInstances == 0) {
                this.evalCompleted = Integer.toString(0);
                this.evalSkipped = Integer.toString(0);
                this.evalRemaining = Integer.toString(0);

            } else {
                sql = "select * from evaluation where display_status=1 and sharing_decision is not NULL and user_name=?";
                this.evalCompleted = Integer.toString(getInstances(sql));
                sql = "select * from evaluation where display_status=1 and sharing_decision is NULL and user_name=?";
                this.evalSkipped = Integer.toString(getInstances(sql));
                this.evalRemaining = Integer.toString(noInstances-Integer.parseInt(this.evalCompleted)-Integer.parseInt(this.evalSkipped));

            }

        }
        catch (Exception e)
        {
            System.out.println(e);
        }

    }
}
