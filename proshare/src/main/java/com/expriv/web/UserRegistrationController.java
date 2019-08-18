package com.expriv.web;

import com.expriv.model.Login;
import com.expriv.service.ImageAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.expriv.model.User;
import com.expriv.service.ConfigurationService;
import com.expriv.service.UserService;
import com.expriv.web.dto.UserRegistrationDto;

import javax.validation.Valid;
import java.io.IOException;

@Controller
@RequestMapping("/registration")
public class UserRegistrationController {

    @Autowired
    private UserService userService;

    @ModelAttribute("user")
    public UserRegistrationDto userRegistrationDto() {
        return new UserRegistrationDto();
    }

    @GetMapping
    public String showRegistrationForm(Model model) {
        return "registration";
    }

    @PostMapping
    public String registerUserAccount(@ModelAttribute("user") @Valid UserRegistrationDto userDto,
                                      BindingResult result, Model model){

        User existing = userService.findByEmail(userDto.getEmail());
        if (existing != null){
            result.rejectValue("email", null, "There is already an account registered with that email");
        }

        if (userDto.getAge()==null || userDto.getGender()==null || userDto.getSharingFrequency()==null || userDto.getSocialmediaFrequency()==null || userDto.getEducation()==null)
            result.rejectValue("gender", null, "At least one demographic field is empty");

        if (result.hasErrors()){
            return "registration";
        }

        userService.save(userDto);

        ImageAttributeService imageAttributeService = new ImageAttributeService();
        ConfigurationService configurationService=new ConfigurationService();
        configurationService.setParams();
        String python_root_dir = configurationService.getPython_base_dir();
        String command1=configurationService.getPythonCommand()+" "+python_root_dir+"utils.py generateImageID"+" "+userDto.getEmail()+" "+"training"+" "+configurationService.getTrain_batch_size();
        String command2 = configurationService.getPythonCommand()+" "+python_root_dir+"configuration.py initialization"+" "+userDto.getEmail();

        try {
            Runtime.getRuntime().exec(command2);


        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("Executing command here");
            Process p = Runtime.getRuntime().exec(command1);
            imageAttributeService.printUpdate(p, "imageread");

        } catch (IOException e) {
            e.printStackTrace();
        }

        Login login = new Login();
        login.setSuccess(true);
        model.addAttribute("login", login);
        return "login";
    }

}
