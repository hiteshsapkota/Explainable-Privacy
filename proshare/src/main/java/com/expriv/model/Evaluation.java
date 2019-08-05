package com.expriv.model;

import com.expriv.service.ConfigurationService;
import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Evaluation {

    private int id;
    private boolean attributeValid;
    private String image_id;
    private String image_path;
    private int recommendation;
    private String username;
    private String feedback;
    private String disagree_type;
    private String message;
    private FeedbackDto feedbackDto;
    private JdbcTemplate jdbcTemplate;
    private String feedbackRecomm;
    private String feedbackExp;
    private Index index;
    private boolean update;
    private String addAttr;
    private String description;
    private String expType;
    private ExplanationDto explanationDto;
    private List<String> explanation = new ArrayList<String>();
    private List<String> additionalAttributes;
    public List<String> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(List<String> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

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

    public List<String> getExplanation() {
        return explanation;
    }

    public void setExplanation(List<String> explanation) {
        this.explanation = explanation;
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

    public String getImage_path() {
        return image_path;
    }

    public int getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(int recommendation) {
        this.recommendation = recommendation;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FeedbackDto getFeedbackDto() {
        return feedbackDto;
    }

    public void setFeedbackDto(FeedbackDto feedbackDto) {
        this.feedbackDto = feedbackDto;
    }


    public String getAddAttr() {
        return addAttr;
    }

    public void setAddAttr(String addAttr) {
        this.addAttr = addAttr;
    }

    public String getFeedbackRecomm() {
        return feedbackRecomm;
    }

    public void setFeedbackRecomm(String feedbackRecomm) {
        this.feedbackRecomm = feedbackRecomm;
    }

    public boolean isUpdate() {
        return update;
    }

    public String getFeedbackExp() {
        return feedbackExp;
    }

    public void setFeedbackExp(String feedbackExp) {
        this.feedbackExp = feedbackExp;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public String getExpType() {
        return expType;
    }

    public void setExpType(String expType) {
        this.expType = expType;
    }

    public ExplanationDto getExplanationDto() {
        return explanationDto;
    }

    public void setExplanationDto(ExplanationDto explanationDto) {
        this.explanationDto = explanationDto;
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
                    this.recommendation=record.getRecommendation();
                    this.explanationDto = convertExplanation(record.getExplanation());
                    this.expType = record.getExpType();
                    this.description = record.getDescription();




                }

            }

        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public ExplanationDto convertExplanation(String rawExplanation)
    {
        String[] lines = rawExplanation.split("\n");
        List<String> explanations =  Arrays.asList(lines);

        ExplanationDto explanationDto = new ExplanationDto();
        for (String exp:explanations) {
            Explanation explanation = new Explanation();
            explanation.setContent(exp);
            explanationDto.addExplanation(explanation);

        }
        return explanationDto;


    }

    public void storeAgree_type(boolean insufficient)
    {
        try
        {


            if (feedbackRecomm.equals("Agree"))
            {
                jdbcTemplate.update("update evaluation set agree_recommendation = 'yes'  where id ="+Integer.toString(this.id)+";");
                if (this.recommendation==1)
                jdbcTemplate.update("update evaluation set sharing_decision = 1  where id ="+Integer.toString(this.id)+";");
                else if (this.recommendation==0) {
                    jdbcTemplate.update("update evaluation set sharing_decision = 0  where id =" + Integer.toString(this.id) + ";");
                    if (!insufficient)
                    {
                        if (feedbackExp.equals("Agree"))
                            jdbcTemplate.update("update evaluation set agree_explanation = 'yes'  where id =" + Integer.toString(this.id) + ";");
                        else if (feedbackExp.equals("Disagree"))
                            jdbcTemplate.update("update evaluation set agree_explanation = 'no'  where id =" + Integer.toString(this.id) + ";");

                    }
                }


            }
            else if (feedbackRecomm.equals("Disagree"))

            {
                jdbcTemplate.update("update evaluation set agree_recommendation = 'no'  where id ="+Integer.toString(this.id)+";");
                if (this.recommendation==1)
                    jdbcTemplate.update("update evaluation set sharing_decision = 0  where id ="+Integer.toString(this.id)+";");
                else if (this.recommendation==0)
                    jdbcTemplate.update("update evaluation set sharing_decision = 1  where id ="+Integer.toString(this.id)+";");




            }



            System.out.println("Storing Record");

            String query="update evaluation set display_status = 1  where id ="+Integer.toString(this.id);
            jdbcTemplate.update(query);


            if (!(feedbackRecomm.equals("Disagree")&&feedbackExp.equals("Agree"))) {

                query = "INSERT INTO feedback (user_name, image_id, image_path, display_status, sharing_decision, description) VALUES (?, ?, ?, ?, ?, ?)";

                Object[] params;
                if (feedbackRecomm.equals("Agree"))
                {
                    if (this.recommendation==1)
                    {

                        params = new Object[] {this.username, this.image_id, this.image_path, 1, 1, this.description};
                    }
                    else
                    {
                        params = new Object[] {this.username, this.image_id, this.image_path, 1, 0, this.description};
                    }
                }
                else
                {
                    if (this.recommendation==1)
                    {
                        params = new Object[] {this.username, this.image_id, this.image_path, 1, 0, this.description};
                    }
                    else
                    {
                        params = new Object[] {this.username, this.image_id, this.image_path, 1, 1, this.description};
                    }
                }
                int[] types = new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.VARCHAR };

                jdbcTemplate.update(query, params, types);

            }

        } catch (Exception e)
        {
            System.out.println(e);
        }

    }
    public void storeFeedbackType()
    {

        try
        {
            if (feedbackRecomm.equals("Disagree") && feedbackExp.equals("Agree"))
            {
                jdbcTemplate.update("update evaluation set disagree_type = 'Recommendation'  where id ="+Integer.toString(this.id)+";");
            }
            else if (feedbackRecomm.equals("Agree") && feedbackExp.equals("Disagree"))
            {
                jdbcTemplate.update("update evaluation set disagree_type = 'Explanation'  where id ="+Integer.toString(this.id)+";");
            }
            else if (feedbackRecomm.equals("Disagree") && feedbackExp.equals("Disagree"))
            {
                jdbcTemplate.update("update evaluation set disagree_type = 'Both'  where id ="+Integer.toString(this.id)+";");
            }
            else
            {
                System.out.println("Serious Problem");
            }

        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

}










