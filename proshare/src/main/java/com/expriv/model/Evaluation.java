package com.expriv.model;

import com.expriv.service.ConfigurationService;
import com.expriv.service.ImageAttributeService;
import com.opencsv.CSVWriter;
import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private String addRemark;
    private String description;
    private String expType;
    private ExplanationDto explanationDto;
    private String recommChange;
    private String recommReason;
    private List<String> explanation = new ArrayList<String>();
    private List<String> additionalAttributes;

    private List<String> expUnderstandability = new ArrayList<String>();
    private List<String> attrUnderstandability = new ArrayList<String>();
    private List<String> understandValue = new ArrayList<String>();

    public List<String> getAdditionalAttributes() {
        return additionalAttributes;

    }


    public void setAdditionalAttributes(List<String> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public List<String> getExpUnderstandability() {
        return expUnderstandability;
    }

    public void setExpUnderstandability(List<String> expUnderstandability) {
        this.expUnderstandability = expUnderstandability;
    }

    public List<String> getAttrUnderstandability() {
        return attrUnderstandability;
    }

    public void setAttrUnderstandability(List<String> attrUnderstandability) {
        this.attrUnderstandability = attrUnderstandability;
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


    public String getAddRemark() {
        return addRemark;
    }

    public void setAddRemark(String addRemark) {
        this.addRemark = addRemark;
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

    public String getRecommChange() {
        return recommChange;
    }

    public void setRecommChange(String recommChange) {
        this.recommChange = recommChange;
    }

    public String getRecommReason() {
        return recommReason;
    }

    public void setRecommReason(String recommReason) {
        this.recommReason = recommReason;
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



    public void storeRecommendation(String agree_recommendation,String change_recommendation)
    {
        jdbcTemplate.update("update evaluation set agree_recommendation="+"'"+agree_recommendation+"'"+" where id="+Integer.toString(this.id)+";");
        jdbcTemplate.update("update evaluation set change_recommendation="+"'"+change_recommendation+"'"+" where id="+Integer.toString(this.id)+";");
        jdbcTemplate.update("update evaluation set display_status = 1  where id ="+Integer.toString(this.id)+";");

    }


    public void storeFeedback(int sharing_decision)
    {
        Object[] params = new Object[] {this.username, this.image_id, this.image_path, 1, sharing_decision, this.description};
        int[] types = new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.VARCHAR };
        String query = "INSERT INTO feedback (user_name, image_id, image_path, display_status, sharing_decision, description) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(query, params, types);


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

    public String getAttribute(String description)
    {
        List<Attribute> records;
        String command = "select * from attribute where description=?";
        records= jdbcTemplate.query(command, new Object[] { description }, new AttributeRowMapper());
        Attribute record=records.get(0);
        return record.getName();

    }

    public void storeSensitivity(String type)

    {
        List<String> sensitive = new ArrayList<>();
        List<String> insensitive = new ArrayList<>();
        if (type.equals("selected"))
        {

            for (Explanation explanation:this.getExplanationDto().getExplanations())
            {
                if (explanation.getContent()==null)
                    continue;
                String content = explanation.getContent();

                if (explanation.getValue()==null)
                    sensitive.add(this.getAttribute(content));
                else
                    insensitive.add(this.getAttribute(content));


            }


        }

        else if (type.equals("all"))
        {
            for (Feedback feedback: this.getFeedbackDto().getFeedbacks())
            {
                if (feedback.getAttributeName()==null)
                    continue;
                String content = feedback.getAttributeName();
                if (feedback.getAttributeSensitivity()==null)
                    sensitive.add(content);
                else
                    insensitive.add(content);
            }
        }

        jdbcTemplate.update("update evaluation set sensitive_attributes="+"'"+String.join(",", sensitive)+"'"+"where id ="+Integer.toString(this.getId())+";");
        jdbcTemplate.update("update evaluation set insensitive_attributes="+"'"+String.join(",", insensitive)+"'"+"where id ="+Integer.toString(this.getId())+";");

    }
    public void storeAdditionalAttributes()
    {
        jdbcTemplate.update("update evaluation set other_sensitive_attributes="+"'"+String.join(",", this.additionalAttributes)+"'"+"where id ="+Integer.toString(this.getId())+";");
    }
    public void transferSensitivity(ImageAttributeService imageAttributeService, String type)
    {
        ConfigurationService configurationService=new ConfigurationService();
        configurationService.setParams();
        String pythonBasePath = configurationService.getPython_base_dir();
        CSVWriter writer= imageAttributeService.createFile(pythonBasePath);
        List<String> sensitive = new ArrayList<>();
        List<String> insensitive = new ArrayList<>();
        String[] data;
        if (type.equals("selected"))
        {
            for (Explanation explanation:this.getExplanationDto().getExplanations())
            {
                if (explanation.getContent()==null)
                    continue;
                String content = explanation.getContent();

                if (explanation.getValue()==null)
                    data = new String[]{this.getAttribute(content), Integer.toString(1)};

                else
                    data = new String[]{this.getAttribute(content), Integer.toString(0)};
                writer.writeNext(data);


            }
        }
        else if (type.equals("all"))
        {
            for (Feedback feedback: this.getFeedbackDto().getFeedbacks())
            {
                if (feedback.getAttributeName()==null)
                    continue;
                String content = feedback.getAttributeName();
                if (feedback.getAttributeSensitivity()==null)
                    data = new String[]{content, Integer.toString(1)};
                else
                    data = new String[]{content, Integer.toString(0)};
                writer.writeNext(data);
            }
        }
        else if (type.equals("other"))
        {
            for (Feedback feedback: this.getFeedbackDto().getFeedbacks())
            {
                if (feedback.getAttributeName()==null)
                    continue;
                String content = feedback.getAttributeName();
                data = new String[]{content, Integer.toString(0)};
                writer.writeNext(data);
            }

        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        this.setUsername(principal);
        String command=configurationService.getPythonCommand()+" "+pythonBasePath+"utils.py storeFeedback"+" "+this.getUsername();
        try {

            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void storeAdditionalRemark()
    {
        jdbcTemplate.update("update evaluation set remark="+"'"+this.addRemark+"'"+"where id ="+Integer.toString(this.getId())+";");
    }

    public void readUndersatandability()
    {

        String command = "select * from understandability where user_name=?";
        List<Understandability> records= jdbcTemplate.query(command, new Object[] { this.username }, new UnderstandabilityRowMapper());
        List<String> usedAttributes = new ArrayList<String>();
        for (Understandability record: records)
        {
            if (record.getUsed()==1)
                usedAttributes.add(record.getAttribute());

        }
        for (Feedback feedback: this.feedbackDto.getFeedbacks())
        {
            String attributeName = feedback.getAttributeName();
            if (usedAttributes.contains(attributeName))
              continue;
            this.attrUnderstandability.add(feedback.getAttributeDescription());
        }
        int i=0;

        for (Explanation explanation: this.explanationDto.getExplanations())
        {

            if (i==0) {
                i += 1;
                continue;
            }
            i+=1;
            System.out.println("Content is"+explanation.getContent());
            String attributeName = this.getAttribute(explanation.getContent());
            if (usedAttributes.contains(attributeName))
                continue;
            this.expUnderstandability.add(explanation.getContent());
        }
        }

    }













