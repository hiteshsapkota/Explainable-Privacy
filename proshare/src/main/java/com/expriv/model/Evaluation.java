package com.expriv.model;

import com.expriv.service.ConfigurationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Evaluation {

    private int id;
    private boolean attributeValid;
    private String image_id;
    private String image_path;
    private String explanation;
    private String username;
    private String feedback;
    private String disagree_type;
    private String message;
    private FeedbackDto feedbackDto;
    private JdbcTemplate jdbcTemplate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public String getExplanation() {
        return explanation;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(Object principal) {

        if (principal instanceof UserDetails) {
            this.username = ((UserDetails)principal).getUsername();
        } else {
            this.username = principal.toString();
        }
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getDisagree_type() {
        return disagree_type;
    }

    public void setDisagree_type(String disagree_type) {
        this.disagree_type = disagree_type;
    }

    public boolean isAttributeValid() {
        return attributeValid;
    }

    public void setAttributeValid(boolean attributeValid) {
        this.attributeValid = attributeValid;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FeedbackDto getFeedbackDto() {
        return feedbackDto;
    }

    public void setFeedbackDto(FeedbackDto feedbackDto) {
        this.feedbackDto = feedbackDto;
    }

    public void readId() {
        try
        {
            String username=this.username;
            String sql = "select * from evaluation where  user_name=?";
            List<Record> records=jdbcTemplate.query(sql, new Object[] { username }, new RecordRowMapper());
            if (records.isEmpty())
            {
                this.id=0;
                this.image_id="undefined";

            }
            else
            {
                sql = "select * from evaluation where display_status=0 and user_name=?";
                records=jdbcTemplate.query(sql, new Object[] { username }, new RecordRowMapper());
                if (records.isEmpty())
                {
                    this.id=0;
                    this.image_id="na";
                    this.image_path="na";


                }
                else
                {
                    Record record=records.get(0);
                    this.id = record.getId();
                    this.image_id = record.getImage_id();
                    this.image_path= record.getImage_path();
                    this.username=record.getUser_name();
                    String update_query="update evaluation set display_status = 1  where id ="+Integer.toString(this.id);
                    jdbcTemplate.update(update_query);



                }

            }

        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public void generateExplanation() throws IOException {

        ConfigurationService configurationService=new ConfigurationService();
        configurationService.setParams();
        String username=this.username;
        String command="python2.7"+" "+configurationService.getPython_base_dir()+"models.py getExplanation"+" "+username+" "+this.image_id;
        Process p =Runtime.getRuntime().exec(command);
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line + "\n");
        }
        int exitVal = 0;

        try {
            exitVal = p.waitFor();
        } catch (InterruptedException e) {
            System.out.println("Unsuccess");
            e.printStackTrace();
        }
        if (exitVal == 0) {
            this.explanation=output.toString();

        } else {

            System.out.println("Abnormal");

        }

    }

    public void storeSharing_type()
    {
        try
        {

            if (feedback.equals("Agree"))
            {
                jdbcTemplate.update("update evaluation set sharing_decision = 1  where id ="+Integer.toString(this.id)+";");

            }
            else if (feedback.equals("Disagree"))

            {
                jdbcTemplate.update("update evaluation set sharing_decision = 0  where id ="+Integer.toString(this.id)+";");
                if (disagree_type.equals("recommendation"))

                    jdbcTemplate.update("update evaluation set disagree_type = 'Recommendation'  where id ="+Integer.toString(this.id)+";");
                else if (disagree_type.equals("explanation"))
                    jdbcTemplate.update("update evaluation set disagree_type = 'Explanation'  where id ="+Integer.toString(this.id)+";");

                else if (disagree_type.equals("both"))
                    jdbcTemplate.update("update evaluation set disagree_type = 'Both'  where id ="+Integer.toString(this.id)+";");

            }

        } catch (Exception e)
        {
            System.out.println(e);
        }

    }

}







