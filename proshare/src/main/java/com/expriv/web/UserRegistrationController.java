package com.expriv.web;

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
                                      BindingResult result){

        User existing = userService.findByEmail(userDto.getEmail());
        if (existing != null){
            result.rejectValue("email", null, "There is already an account registered with that email");
        }

        if (result.hasErrors()){
            return "registration";
        }

        userService.save(userDto);


        ConfigurationService configurationService=new ConfigurationService();
        configurationService.setParams();

        String python_root_dir = configurationService.getPython_base_dir();

        String command1="python2.7"+" "+python_root_dir+"utils.py generateImageID"+" "+userDto.getEmail()+" "+"training 1";
        String command2 = "python2.7"+" "+python_root_dir+"configuration.py initialization"+" "+userDto.getEmail();

        try {
            Runtime.getRuntime().exec(command1);
            Runtime.getRuntime().exec(command2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/registration?success";
    }

}
