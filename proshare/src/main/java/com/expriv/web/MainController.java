package com.expriv.web;

import com.expriv.model.*;
import com.expriv.service.ConfigurationService;
import com.expriv.service.EvaluationService;
import com.expriv.service.ImageAttributeService;
import com.expriv.service.TrainingService;
import org.apache.catalina.security.SecurityUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/")
    public String root(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {

            Index index = new Index();
            ConfigurationService configurationService = new ConfigurationService();
            configurationService.setParams();
            index.setTrainBatchSize(configurationService.getTrain_batch_size());
            index.setEvalBatchSize(configurationService.getEval_batch_size());
            index.setUsername(principal);
            index.setJdbcTemplate(jdbcTemplate);
            index.setProgress();
            model.addAttribute("index", index);
            return "index";
        }


        else {

            return "login";

        }
    }



    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/user")
    public String userIndex() {
        return "index";
    }

   /* @PostMapping("/feedback")
    public String feedback(@ModelAttribute Evaluation evaluation, Model model)
    {
        evaluation.setJdbcTemplate(jdbcTemplate);

        if ((evaluation.getDisagreeRecomm()==null && evaluation.getDisagreeExp()==null))
        {


            model.addAttribute("feedback", evaluation);
            evaluation.setDisagree_type("invalid");
            evaluation.setAttributeValid(true);
            return "feedback";

        }
        else
        {
            evaluation.setDisagree_type("valid");
            if (evaluation.isAttributeValid()!=true)
            {
                evaluation.setAttributeValid(true);
                FeedbackDto feedbackDto = evaluation.getFeedbackDto();
                for (Feedback feedback: feedbackDto.getFeedbacks())
                {
                    if (feedback.getAttributeValue()==0)
                    {
                        evaluation.setAttributeValid(false);
                        break;
                    }
                }
            }
            if (evaluation.isAttributeValid()==false)
            {
                model.addAttribute("feedback", evaluation);
                return "feedback";
            }
            ImageAttributeService imageAttributeService = new ImageAttributeService();
            try
            {
                System.out.println(evaluation.getImage_id());
                imageAttributeService.transferValues(evaluation.getFeedbackDto());
                evaluation.storeFeedbackType();

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            EvaluationService evaluationService = new EvaluationService();
            return evaluationService.getTemplate(jdbcTemplate, model, true);





        }



    }*/

    @GetMapping("/index")
    public String index(Model model)
    {
        Index index = new Index();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ConfigurationService configurationService = new ConfigurationService();
        configurationService.setParams();
        index.setTrainBatchSize(configurationService.getTrain_batch_size());
        index.setEvalBatchSize(configurationService.getEval_batch_size());
        index.setUsername(principal);
        index.setJdbcTemplate(jdbcTemplate);
        index.setProgress();
        model.addAttribute("index", index);
        return "index";
    }

    @GetMapping("/training")
    public String trainingForm(Model model) {

        Training training= new Training();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        training.setUsername(principal);
        training.setJdbcTemplate(jdbcTemplate);
        training.readId();
        training.setUpdate(false);
        Index index = training.getIndex();
        model.addAttribute("training", training);

        return "training";
    }

    @PostMapping("/training")
    public String trainingSubmit(@ModelAttribute Training training, Model model) {

        training.setJdbcTemplate(jdbcTemplate);
        training.updateDisplayStatus();
        if (training.getButton_type().equals("Prev"))
        {

            int prev_id = training.getId();
            training.getPrevious();
            if (prev_id==training.getId())
            {
                return "training";
            }
            model.addAttribute("training", training);
            return "training";


        }

        else if (training.getButton_type().equals("Next"))
        {

            training.getNext();
        }

        else
        {
            training.storeSharing_type();
            training = new Training();
            training.setUpdate(false);
            training.setJdbcTemplate(jdbcTemplate);
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            training.setUsername(principal);
            training.readId();

        }
        TrainingService trainingService = new TrainingService();
        return trainingService.getTemplate(jdbcTemplate, model, training);

    }


    @GetMapping("/evaluation")
    public String evaluationForm(Model model)
    {
        Evaluation evaluation= new Evaluation();
        evaluation.setJdbcTemplate(jdbcTemplate);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        evaluation.setUsername(principal);
        evaluation.readId();

        ConfigurationService configurationService = new ConfigurationService();
        configurationService.setParams();

        if (evaluation.getImage_id().equals("undefined")||evaluation.getImage_id().equals("na"))
        {

            Training training = new Training();
            training.setUsername(evaluation.getUsername());
            training.setJdbcTemplate(jdbcTemplate);
            training.setTrainingInstances();
            if (training.getTrainingInstances()<configurationService.getTrainingThreshold())
            {
                evaluation.setMessage("train_first");
                evaluation.setAttributeValid(true);
                model.addAttribute("evaluation", evaluation);
                return "evaluation";
            }
            int eval_batch_size = configurationService.getEval_batch_size();

            try {
                String python_root_dir=configurationService.getPython_base_dir();


                String command=configurationService.getPythonCommand()+" "+python_root_dir+"utils.py generateImageID"+" "+evaluation.getUsername()+" "+"evaluation"+" "+eval_batch_size;
                Process p = Runtime.getRuntime().exec(command);
                ImageAttributeService imageAttributeService=new ImageAttributeService();
                imageAttributeService.printUpdate(p, "imageread");

                command=configurationService.getPythonCommand()+" "+python_root_dir+"models.py generateExplanation"+" "+evaluation.getUsername();
                p = Runtime.getRuntime().exec(command);
                imageAttributeService.printUpdate(p, " ");

            }

            catch (IOException e) {

                e.printStackTrace();
            }




        }


        evaluation.readId();
        evaluation.setUpdate(false);
        Index index = evaluation.getIndex();
        if ((Integer.parseInt(index.getEvalCompleted())+1)%configurationService.getUpdate_epoch()==0)
            evaluation.setUpdate(true);


        try {

            FeedbackDto feedbackDto=new FeedbackDto();

            ImageAttributeService imageAttributeService=new ImageAttributeService();
            imageAttributeService.setImage_id(evaluation.getImage_id());
            imageAttributeService.setAttributes(jdbcTemplate);
            List<String> imageAttributes = imageAttributeService.getAttributes();
            List<Attribute> records;

            for (String imageAttribute:imageAttributes) {
                  imageAttribute = imageAttribute.replace("\n", "");
                  Feedback feedback = new Feedback();
                  feedback.setAttributeName(imageAttribute);

                String command = "select * from attribute where name=?";
                records= jdbcTemplate.query(command, new Object[] { imageAttribute }, new AttributeRowMapper());
                Attribute record=records.get(0);
                feedback.setAttributeId(imageAttribute.replace(" ", "-").replace("(", "").replace(")", "").replace("/", ""));
                feedback.setAttributeDescription(record.getDescription());
                  feedbackDto.addFeedback(feedback);
            }

            evaluation.setFeedbackDto(feedbackDto);
            evaluation.setAttributeValid(true);
            model.addAttribute("evaluation", evaluation);


        } catch (IOException e) {
            e.printStackTrace();
        }
        return "evaluation";


    }

        @PostMapping("/evaluation")
        public String evaluationSubmit(@ModelAttribute Evaluation evaluation, Model model)
        {

            System.out.println("Additional Attributes"+evaluation.getAdditionalAttributes().get(0));
            System.out.println("Additional Attributes"+evaluation.getAdditionalAttributes().get(1));



            evaluation.setJdbcTemplate(jdbcTemplate);
            Index index=evaluation.getIndex();
            boolean insufficient = evaluation.getExpType().equals("NotShare_Insufficient")||  evaluation.getExpType().equals("Share_Insufficient") ||  evaluation.getExpType().equals("Share_OwnSensPresent");
            if (evaluation.getFeedbackRecomm()==null) {

                evaluation.setFeedbackRecomm("invalid");
                evaluation.setFeedbackExp("valid");
                evaluation.setAttributeValid(true);
                model.addAttribute("evaluation", evaluation);

                return "evaluation";
            }


            else if (evaluation.getFeedbackExp()==null && evaluation.getRecommendation()==0 && evaluation.getFeedbackRecomm().equals("Agree") && !insufficient)
            {


                    evaluation.setFeedbackRecomm("valid");
                    evaluation.setFeedbackExp("invalid");
                    evaluation.setAttributeValid(true);
                    model.addAttribute("evaluation", evaluation);
                    return "evaluation";

            }

            else
            {

                evaluation.storeAgree_type(insufficient);
                ImageAttributeService imageAttributeService = new ImageAttributeService();

                try
                {

                       imageAttributeService.transferValues(evaluation.getFeedbackDto(), jdbcTemplate, evaluation.getAddAttr(), evaluation.getId(), evaluation, insufficient);

                    evaluation.storeFeedbackType();

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }








                EvaluationService evaluationService = new EvaluationService();

                return evaluationService.getTemplate(jdbcTemplate, model);





            }







        }







}
