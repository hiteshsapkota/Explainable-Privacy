package com.expriv.model;

import com.expriv.service.ConfigurationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;

public class Payment {
    private String code;
    private String username;
    private JdbcTemplate jdbcTemplate;
    private int generate;
    private int id;
    private String message;
    private boolean gensuccess;
    private Index index;
    private String comment;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int getGenerate() {
        return generate;
    }

    public void setGenerate(int generate) {
        this.generate = generate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isGensuccess() {
        return gensuccess;
    }

    public void setGensuccess(boolean gensuccess) {
        this.gensuccess = gensuccess;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public void generateCode(int n)
    {

        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(n);
        for (int i=0; i<n; i++)
        {
            int index = (int) (AlphaNumericString.length()*Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        this.code = sb.toString();

    }
}
