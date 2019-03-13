package com.expriv.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.expriv.model.Evaluation;
import com.expriv.model.Training;
import com.expriv.service.ConfigurationService;

import java.io.IOException;

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

  @GetMapping("/training")
  public String trainingForm(Model model) {

    Training training = new Training();

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
    if (training.getSharing_type().equals("invalid")) {

      model.addAttribute("training", training);

    } else {

      training.storeSharing_type();
      training = new Training();
      training.setJdbcTemplate(jdbcTemplate);
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      training.setUsername(principal);
      training.readId();

      if (training.getImage_path().equals("na")) {
        ConfigurationService configurationService = new ConfigurationService();
        configurationService.setParams();
        String python_root_dir = configurationService.getPython_base_dir();
        int train_batch_size = configurationService.getTrain_batch_size();
        int eval_batch_size = configurationService.getEval_batch_size();

        try {
          String command = "python2.7" + " " + python_root_dir + "utils.py generateImageID" + " "
              + training.getUsername() + " " + "training" + " " + train_batch_size;
          Runtime.getRuntime().exec(command);
          command = "python2.7" + " " + python_root_dir + "configuration.py update";
          Runtime.getRuntime().exec(command);
          command = "python2.7" + " " + python_root_dir + "utils.py generateImageID" + " "
              + training.getUsername() + " " + "evaluation" + " " + eval_batch_size;
          Runtime.getRuntime().exec(command);
        } catch (IOException e) {
          e.printStackTrace();
        }
        return "training_complete";
      } else {

        model.addAttribute("training", training);
        return "training";
      }
    }
    return "training";
  }

  @GetMapping("/evaluation")
  public String evaluationForm(Model model) {
    Evaluation evaluation = new Evaluation();
    evaluation.setJdbcTemplate(jdbcTemplate);
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    evaluation.setUsername(principal);

    evaluation.readId();
    if (evaluation.getImage_id().equals("unidentified") || evaluation.getImage_id().equals("na")) {
      evaluation.setMessage("train_first");
      model.addAttribute("evaluation", evaluation);
      return "evaluation";

    }

    try {
      evaluation.generateExplanation();
    } catch (IOException e) {
      e.printStackTrace();
    }

    model.addAttribute("evaluation", evaluation);

    return "evaluation";
  }

  @PostMapping("/evaluation")
  public String evaluationSubmit(@ModelAttribute Evaluation evaluation, Model model) {

    evaluation.setJdbcTemplate(jdbcTemplate);

    if (evaluation.getFeedback() == null) {
      System.out.println(evaluation.getImage_path());

      evaluation.setFeedback("invalid");
      evaluation.setDisagree_type("na");
      model.addAttribute("evaluation", evaluation);
      return "evaluation";

    }

    if (evaluation.getFeedback().equals("Agree")) {
      evaluation.setDisagree_type("na");
      evaluation.setAtt1(100);
      evaluation.setAtt2(100);

    }

    if (evaluation.getDisagree_type().equals("invalid")) {
      System.out.println(evaluation.getImage_path());
      model.addAttribute("evaluation", evaluation);
      return "evaluation";
    }

    if (evaluation.getAtt1() == 0 || evaluation.getAtt2() == 0) {

      System.out.println(evaluation.getImage_path());
      evaluation.setAtt_status(10);
      model.addAttribute("evaluation", evaluation);
      return "evaluation";
    }

    evaluation.storeSharing_type();

    evaluation = new Evaluation();
    evaluation.setJdbcTemplate(jdbcTemplate);
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    evaluation.setUsername(principal);

    evaluation.readId();

    if (evaluation.getImage_id().equals("na")) {

      System.out.println(evaluation.getImage_path());
      ConfigurationService configurationService = new ConfigurationService();
      configurationService.setParams();
      String python_root_dir = configurationService.getPython_base_dir();
      int eval_batch_size = configurationService.getEval_batch_size();

      try {

        String command = "python2.7" + " " + python_root_dir + "configuration.py update";
        Runtime.getRuntime().exec(command);
        command = "python2.7" + " " + python_root_dir + "utils.py generateImageID" + " "
            + evaluation.getUsername() + " " + "evaluation" + " " + eval_batch_size;
        Runtime.getRuntime().exec(command);
      } catch (IOException e) {
        e.printStackTrace();
      }
      return "evaluation_complete";

    }

    try {

      evaluation.generateExplanation();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println(evaluation.getImage_path());
    model.addAttribute("evaluation", evaluation);
    return "evaluation";

  }

  @PostMapping("/greeting")
  public String greetingSubmit(Model model) {
    return "result";
  }

}
