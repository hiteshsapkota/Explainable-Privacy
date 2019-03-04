package net.javaguides.springboot.springsecurity.web;

import net.javaguides.springboot.springsecurity.model.Training;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MainController {


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
        Training training= new Training();
        training.readId();
        model.addAttribute("training", training);
        return "training";
    }

    @PostMapping("/training")
    public String trainingSubmit(@ModelAttribute Training training, Model model) {


        if (training.getSharing_type().equals("invalid")) {

            model.addAttribute("training", training);

        }
        else {
            training.storeSharing_type();
            training = new Training();
            training.readId();

            if (training.getImage_id().equals("na")) {
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
    public String evaluation(Model model) {return "evaluation";}

    @PostMapping("/greeting")
    public String greetingSubmit(Model model) {
        return "result";
    }


}
