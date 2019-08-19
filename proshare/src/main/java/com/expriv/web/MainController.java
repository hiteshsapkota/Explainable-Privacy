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

import javax.jws.soap.SOAPBinding;
import java.io.IOException;
import java.util.ArrayList;
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
           Login login = new Login();
           login.setSuccess(false);
           model.addAttribute("login", login);
            return "login";

        }
    }



    @GetMapping("/login")
    public String login(Model model) {
        Login login = new Login();
        login.setSuccess(false);
        model.addAttribute("login", login);
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

    @GetMapping("/payment")
    public String paymentForm(@ModelAttribute Payment payment, Model model)
    {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        payment.setUsername(principal);
        payment.setJdbcTemplate(jdbcTemplate);

        String sql = "select * from training where user_name=? and sharing_decision is not null";
        List<Record> records=jdbcTemplate.query(sql, new Object[] { payment.getUsername() },new RecordRowMapper());

        if (records.size()>=30)
        {

            sql = "select * from payment where user_name=?";
            String code = jdbcTemplate.query(sql, new Object[] {payment.getUsername()}, new PaymentRowMapper()).get(0).getCode();
            if (code.equals("NA"))
            {
                payment.generateCode(5);
                sql = "update payment set code= "+"'"+payment.getCode()+"'"+" where user_name= "+"'"+payment.getUsername()+"'";
                jdbcTemplate.execute(sql);
                payment.setGensuccess(true);
            }
            else {
                payment.setCode(code);
                payment.setGensuccess(true);
            }

        }
        else if (records.size()<30)
        {
            payment.setGensuccess(false);
            payment.setMessage("Could not generate payment code for less than 30 pictures");

        }


        model.addAttribute("payment", payment);

        return "payment";
    }





    @GetMapping("/training")
    public String trainingForm(Model model) {

        Training training= new Training();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        training.setUsername(principal);
        training.setJdbcTemplate(jdbcTemplate);
        training.readId();
        training.setUpdate(false);
        training.addUserPayment();
        training.setDonotshare("");
        training.setShare("");
        training.setSkip("");
        Index index = training.getIndex();
        model.addAttribute("training", training);

        return "training";
    }

    @PostMapping("/training")
    public String trainingSubmit(@ModelAttribute Training training, Model model) {

        training.setJdbcTemplate(jdbcTemplate);
        if (training.getOptions()==null) {
            if (training.getSubmittype().equals("Next")) {
                if (training.getShare().equals("") && training.getDonotshare().equals("") && training.getSkip().equals("")) {
                    training.setInvalidInput(true);
                    training.setDonotshare("");
                    training.setShare("");
                    training.setSkip("");
                    model.addAttribute("training", training);
                    return "training";
                }
            }
        }




        if (training.getSubmittype().equals("Prev"))
        {

            int prev_id = training.getId();


            training.getPrevious();
            if (prev_id==training.getId())
            {
                if (training.getOptions()!=null) {


                    if (training.getOptions().equals("share"))
                        training.setShare("active focus");
                    else if (training.getOptions().equals("donotshare"))
                        training.setDonotshare("active focus");
                    else if (training.getOptions().equals("skip"))
                        training.setSkip("active focus");
                }



                model.addAttribute("training", training);
                return "training";
            }


            model.addAttribute("training", training);

            return "training";


        }




           if (training.getOptions()!=null) {
               if (training.getOptions().equals("skip")) {
                   if (training.getDonotshare()=="" && training.getShare()=="" && training.getSkip()=="")
                   {
                       training.updateDisplayStatus();

                   }
                   else
                   {
                       training.updateDisplayStatus();
                       training.storeSharing_type();
                   }

               }
               else {

                   training.updateDisplayStatus();
                   training.storeSharing_type();
               }
           }
            training = new Training();
            training.setUpdate(false);
            training.setJdbcTemplate(jdbcTemplate);
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            training.setUsername(principal);
            training.readId();






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
            evaluation.readUndersatandability();
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
            Index index=evaluation.getIndex();

            if (evaluation.getFeedbackRecomm()==null) {

                evaluation.setFeedbackRecomm("invalid");
                evaluation.setRecommChange("valid");
                evaluation.setRecommReason("valid");
                model.addAttribute("evaluation", evaluation);

                return "evaluation";
            }

            else
            {
                ImageAttributeService imageAttributeService=new ImageAttributeService();

                if (evaluation.getRecommendation()==0)
                {
                    if (evaluation.getExpType().equals("NotShare_OwnSensPresent"))
                    {
                        if (evaluation.getFeedbackRecomm().equals("Agree"))
                        {
                            evaluation.storeRecommendation("yes", "NA");
                            evaluation.storeFeedback(0);
                            evaluation.storeSensitivity("selected");
                            evaluation.storeAdditionalAttributes();


                            evaluation.transferSensitivity(imageAttributeService, "selected");
                            evaluation.storeAdditionalRemark();



                        }
                        else if (evaluation.getFeedbackRecomm().equals("Disagree"))
                        {
                            if (evaluation.getRecommChange()==null)
                            {
                                evaluation.setFeedbackRecomm("valid");
                                evaluation.setRecommChange("invalid");
                                evaluation.setRecommReason("valid");
                                model.addAttribute("evaluation", evaluation);
                                return "evaluation";
                            }

                            if (evaluation.getRecommChange().equals("Agree"))
                            {
                                evaluation.storeRecommendation("yes", "yes");
                                evaluation.storeFeedback(0);
                                evaluation.storeSensitivity("selected");
                                evaluation.storeAdditionalAttributes();
                                evaluation.transferSensitivity(imageAttributeService, "selected");
                                evaluation.storeAdditionalRemark();

                            }
                            else if (evaluation.getRecommChange().equals("Disagree"))
                            {
                                evaluation.storeRecommendation("no", "no");
                                evaluation.storeFeedback(1);
                                evaluation.transferSensitivity(imageAttributeService, "other");
                                evaluation.storeAdditionalRemark();


                            }
                        }
                    }
                    else if (evaluation.getExpType().equals("NotShare_Insufficient"))
                    {
                        if (evaluation.getFeedbackRecomm().equals("Agree"))
                        {
                            evaluation.storeRecommendation("yes", "NA");
                            evaluation.storeFeedback(0);
                            evaluation.storeSensitivity("all");
                            evaluation.storeAdditionalAttributes();
                            evaluation.transferSensitivity(imageAttributeService, "all");
                            evaluation.storeAdditionalRemark();

                        }
                        else if (evaluation.getFeedbackRecomm().equals("Disagree"))
                        {
                            evaluation.storeRecommendation("no", "NA");
                            evaluation.storeFeedback(1);
                            evaluation.transferSensitivity(imageAttributeService, "other");
                            evaluation.storeAdditionalRemark();


                        }


                    }


                }

                else if (evaluation.getRecommendation()==1)
                {
                    if (evaluation.getExpType().equals("Share_OwnSensAbsent"))
                    {
                        if (evaluation.getFeedbackRecomm().equals("Agree"))
                        {
                            evaluation.storeRecommendation("yes", "NA");
                            evaluation.storeFeedback(1);
                            evaluation.transferSensitivity(imageAttributeService, "other");
                            evaluation.storeAdditionalRemark();


                        }
                        else if (evaluation.getFeedbackRecomm().equals("Disagree"))
                        {
                            evaluation.storeRecommendation("no", "NA");
                            evaluation.storeFeedback(0);
                            evaluation.storeSensitivity("all");
                            evaluation.storeAdditionalAttributes();
                            evaluation.transferSensitivity(imageAttributeService, "all");
                            evaluation.storeAdditionalRemark();

                        }
                    }
                    else if (evaluation.getExpType().equals("Share_OwnSensPresent"))
                    {
                        if (evaluation.getFeedbackRecomm().equals("Agree"))
                        {
                            if (evaluation.getRecommChange()==null)
                            {
                                evaluation.setFeedbackRecomm("valid");
                                evaluation.setRecommChange("invalid");
                                evaluation.setRecommReason("valid");
                                model.addAttribute("evaluation", evaluation);
                                return "evaluation";
                            }
                            if (evaluation.getRecommChange().equals("Agree"))
                            {
                                evaluation.storeRecommendation("no", "yes");
                                evaluation.storeFeedback(0);
                                evaluation.storeSensitivity("selected");
                                evaluation.storeAdditionalAttributes();
                                evaluation.transferSensitivity(imageAttributeService, "selected");
                                evaluation.storeAdditionalRemark();

                            }
                            else if (evaluation.getRecommChange().equals("Disagree"))
                            {
                                evaluation.storeRecommendation("yes", "no");
                                evaluation.storeFeedback(1);
                                evaluation.transferSensitivity(imageAttributeService, "other");
                                evaluation.storeAdditionalRemark();


                            }
                        }
                        if (evaluation.getFeedbackRecomm().equals("Disagree"))
                        {
                            if (evaluation.getRecommReason()==null)
                            {
                                evaluation.setFeedbackRecomm("valid");
                                evaluation.setRecommChange("valid");
                                evaluation.setRecommReason("invalid");
                                model.addAttribute("evaluation", evaluation);
                                return "evaluation";
                            }
                            if (evaluation.getRecommReason().equals("Agree"))
                            {
                                evaluation.storeRecommendation("no", "yes");
                                evaluation.storeFeedback(0);
                                evaluation.storeSensitivity("selected");
                                evaluation.storeAdditionalAttributes();
                                evaluation.transferSensitivity(imageAttributeService, "selected");
                                evaluation.storeAdditionalRemark();

                            }
                            else if (evaluation.getRecommReason().equals("Disagree"))
                            {
                                evaluation.storeRecommendation("no", "no");
                                evaluation.storeFeedback(0);
                                evaluation.storeSensitivity("all");
                                evaluation.storeAdditionalAttributes();
                                evaluation.transferSensitivity(imageAttributeService, "all");
                                evaluation.storeAdditionalRemark();

                            }
                        }
                    }



                }
                evaluation.storeAdditionalRemark();
                EvaluationService evaluationService = new EvaluationService();

                return evaluationService.getTemplate(jdbcTemplate, model);

            }










            }









        }








