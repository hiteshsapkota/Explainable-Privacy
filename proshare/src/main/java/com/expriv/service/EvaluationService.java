package com.expriv.service;

import com.expriv.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import java.io.IOException;
import java.util.List;

public class EvaluationService {
    private Evaluation evaluation;

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }
    public String getTemplate(JdbcTemplate jdbcTemplate, Model model)
    {
        System.out.println("I am in the evaluation template section");
        evaluation= new Evaluation();
        evaluation.setJdbcTemplate(jdbcTemplate);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        evaluation.setUsername(principal);
        evaluation.readId();

        Index index = evaluation.getIndex();
        ConfigurationService configurationService = new ConfigurationService();
        configurationService.setParams();
        String python_root_dir=configurationService.getPython_base_dir();

        if (Integer.parseInt(index.getEvalCompleted())%configurationService.getUpdate_epoch()==0)
        {
            evaluation.setUpdate(true);
            System.out.println("Updating Record");
            String command = configurationService.getPythonCommand()+" "+python_root_dir+"configuration.py update";
            ImageAttributeService imageAttributeService = new ImageAttributeService();


            try {
                Process p = Runtime.getRuntime().exec(command);
                imageAttributeService.printUpdate(p, "update");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (evaluation.getImage_id().equals("na"))

        {




            int eval_batch_size = configurationService.getEval_batch_size();
            ImageAttributeService imageAttributeService = new ImageAttributeService();
            try {


                String command=configurationService.getPythonCommand()+" "+python_root_dir+"utils.py generateImageID"+" "+evaluation.getUsername()+" "+"evaluation"+" "+eval_batch_size;
                Process p =Runtime.getRuntime().exec(command);
                imageAttributeService.printUpdate(p, " ");
                command=configurationService.getPythonCommand()+" "+python_root_dir+"models.py generateExplanation"+" "+evaluation.getUsername();
                 p=Runtime.getRuntime().exec(command);
                imageAttributeService.printUpdate(p, " ");
            }

            catch (IOException e) {

                e.printStackTrace();
            }

            model.addAttribute("evaluation", evaluation);
            return "evaluation_complete";

        }


        FeedbackDto feedbackDto=new FeedbackDto();
        ImageAttributeService imageAttributeService=new ImageAttributeService();
        imageAttributeService.setImage_id(evaluation.getImage_id());
        List<String> imageAttributes = imageAttributeService.getAttributes();
        List<Record> records;


        for (String imageAttribute:imageAttributes) {
            Feedback feedback = new Feedback();
            feedback.setAttributeName(imageAttribute);
            feedback.setAttributeId(imageAttribute.replace(" ", "-").replace("(", "").replace(")", "").replace("/", ""));
            String command = "select * from attribute where name=?";
            records= jdbcTemplate.query(command, new Object[] { imageAttribute }, new RecordRowMapper());
            Record record=records.get(0);
            feedback.setAttributeDescription(record.getDescription());
            feedbackDto.addFeedback(feedback);

        }

        evaluation.setFeedbackDto(feedbackDto);
        evaluation.setAttributeValid(true);
        model.addAttribute("evaluation", evaluation);

        model.addAttribute("evaluation", evaluation);
        return "evaluation";

    }
}
