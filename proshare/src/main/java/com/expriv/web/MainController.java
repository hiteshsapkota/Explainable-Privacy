package com.expriv.web;

import com.expriv.model.*;
import com.expriv.service.ConfigurationService;
import com.expriv.service.ImageAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public String root() {
        return "home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/user")
    public String userIndex() {
        return "index";
    }

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
        System.out.println(training.getImage_path());
        model.addAttribute("training", training);
        return "training";
    }

    @PostMapping("/training")
    public String trainingSubmit(@ModelAttribute Training training, Model model) {

        training.setJdbcTemplate(jdbcTemplate);
        training.updateDisplayStatus();
        if (training.getButton_type().equals("Prev")) {

            int prev_id = training.getId();
            training.getPrevious();
            System.out.println(training.getId());
            System.out.println(prev_id);
            if (prev_id==training.getId())
            {
                return "training";
            }
            model.addAttribute("training", training);




        }

        else
        {




        if (training.getButton_type().equals("Share") || training.getButton_type().equals("No Share")) {
                training.storeSharing_type();
            }



            training = new Training();
            training.setJdbcTemplate(jdbcTemplate);
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            training.setUsername(principal);

           training.readId();

            if (training.getImage_path().equals("na")) {
                ConfigurationService configurationService=new ConfigurationService();
                configurationService.setParams();
                String python_root_dir=configurationService.getPython_base_dir();
                int train_batch_size =configurationService.getTrain_batch_size();
                int eval_batch_size = configurationService.getEval_batch_size();

                 try {
                     String command=configurationService.getPythonCommand()+" "+python_root_dir+"utils.py generateImageID"+" "+training.getUsername()+" "+"training"+" "+train_batch_size;
                     Process p = Runtime.getRuntime().exec(command);
                     command = configurationService.getPythonCommand()+" "+python_root_dir+"configuration.py update";
                     p=Runtime.getRuntime().exec(command);
                     training.setTrainingInstances();
                     System.out.println(training.getTrainingInstances());
                     System.out.println(configurationService.getTrainingThreshold());
                     if (training.getTrainingInstances()>configurationService.getTrainingThreshold()) {
                         command = configurationService.getPythonCommand() + " " + python_root_dir + "utils.py generateImageID" + " " + training.getUsername() + " " + "evaluation" + " " + eval_batch_size;
                         p = Runtime.getRuntime().exec(command);
                     }
                 }
                 catch (IOException e) {
                     e.printStackTrace();
                 }
                return "training_complete";
            }
            else {


                model.addAttribute("training", training);
                return "training";
            }
        }
        return "training";
    }


    @GetMapping("/evaluation")
    public String evaluationForm(Model model)
    {
        Evaluation evaluation= new Evaluation();
        evaluation.setJdbcTemplate(jdbcTemplate);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        evaluation.setUsername(principal);
        evaluation.readId();
        System.out.println("The image id for the evaluation");
        System.out.println(evaluation.getImage_id());
        if (evaluation.getImage_id().equals("undefined")||evaluation.getImage_id().equals("na"))
        {

            evaluation.setMessage("train_first");
            evaluation.setAttributeValid(true);
            model.addAttribute("evaluation", evaluation);
            return "evaluation";

        }

        try {

            FeedbackDto feedbackDto=new FeedbackDto();

            ImageAttributeService imageAttributeService=new ImageAttributeService();
            imageAttributeService.setImage_id(evaluation.getImage_id());
            imageAttributeService.setAttributes();
            List<String> imageAttributes = imageAttributeService.getAttributes();

            for (String imageAttribute:imageAttributes) {
                  Feedback feedback = new Feedback();
                  feedback.setAttributeName(imageAttribute);
                  feedbackDto.addFeedback(feedback);
            }

            evaluation.setFeedbackDto(feedbackDto);
            evaluation.generateExplanation();
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

            evaluation.setJdbcTemplate(jdbcTemplate);

            if (evaluation.getFeedback()==null)
            {
                evaluation.setFeedback("invalid");
                evaluation.setDisagree_type("valid");
                evaluation.setAttributeValid(true);
                model.addAttribute( "evaluation", evaluation);
                return "evaluation";
            }

            if (evaluation.getFeedback().equals("Agree"))
            {
                evaluation.setDisagree_type("valid");
                evaluation.setAttributeValid(true);
            }

            if (evaluation.getDisagree_type().equals("invalid"))
            {

                model.addAttribute("evaluation", evaluation);
                evaluation.setAttributeValid(true);
                return "evaluation";
            }

            if (evaluation.isAttributeValid()!=true) {

              evaluation.setAttributeValid(true);
              FeedbackDto feedbackDto = evaluation.getFeedbackDto();
              for (Feedback feedback : feedbackDto.getFeedbacks()) {
                if (feedback.getAttributeValue() == 0) {

                  evaluation.setAttributeValid(false);
                  break;

                    }

                }
            }

            if (evaluation.isAttributeValid()==false)
            {

                model.addAttribute("evaluation", evaluation);
                return "evaluation";
            }

          evaluation.storeSharing_type();
          ImageAttributeService imageAttributeService;
          if (!evaluation.getFeedback().equals("Agree")) {

            imageAttributeService = new ImageAttributeService();
            try {

                imageAttributeService.transferValues(evaluation.getFeedbackDto());
              }
            catch (IOException e) {
                e.printStackTrace();
              }
            }
            evaluation= new Evaluation();
            evaluation.setJdbcTemplate(jdbcTemplate);
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            evaluation.setUsername(principal);
            evaluation.readId();
            if (evaluation.getImage_id().equals("na"))

            {

              ConfigurationService configurationService=new ConfigurationService();
              configurationService.setParams();
              String python_root_dir=configurationService.getPython_base_dir();
              int eval_batch_size = configurationService.getEval_batch_size();
              try {

                   String command = configurationService.getPythonCommand()+" "+python_root_dir+"configuration.py update";
                   Runtime.getRuntime().exec(command);
                   command=configurationService.getPythonCommand()+" "+python_root_dir+"utils.py generateImageID"+" "+evaluation.getUsername()+" "+"evaluation"+" "+eval_batch_size;
                   Runtime.getRuntime().exec(command);

              }

              catch (IOException e) {

                        e.printStackTrace();
                    }

              model.addAttribute("evaluation_complete", evaluation);
              return "evaluation_complete";

                }

            try
            {

                FeedbackDto feedbackDto=new FeedbackDto();
                imageAttributeService=new ImageAttributeService();
                imageAttributeService.setImage_id(evaluation.getImage_id());
                imageAttributeService.setAttributes();
                List<String> imageAttributes = imageAttributeService.getAttributes();

                for (String imageAttribute:imageAttributes) {
                    Feedback feedback = new Feedback();
                    feedback.setAttributeName(imageAttribute);
                    feedbackDto.addFeedback(feedback);

                }

                evaluation.setFeedbackDto(feedbackDto);
                evaluation.generateExplanation();
                evaluation.setAttributeValid(true);
                model.addAttribute("evaluation", evaluation);

            }
            catch (IOException e) {
                e.printStackTrace();
            }

          model.addAttribute("evaluation", evaluation);
          return "evaluation";
        }


}
