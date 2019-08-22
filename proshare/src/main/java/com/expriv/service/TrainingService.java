package com.expriv.service;

import com.expriv.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import java.io.IOException;
import java.util.List;

public class TrainingService {
    private Training training;

    public Training getTraining() {
        return training;
    }

    public void setTraining(Training training) {
        this.training = training;
    }

    public String getTemplate(JdbcTemplate jdbcTemplate, Model model, Training training)
    {
        Index index = training.getIndex();
        if (Integer.parseInt(index.getTrainRemaining())==1)
        {
            training.setUpdate(true);
        }
        if ((Integer.parseInt(index.getTrainCompleted()))%50==0) {



            try {
                ConfigurationService configurationService=new ConfigurationService();
                configurationService.setParams();
                String python_root_dir=configurationService.getPython_base_dir();
                int train_batch_size =configurationService.getTrain_batch_size();
                String command=configurationService.getPythonCommand()+" "+python_root_dir+"utils.py generateImageID"+" "+training.getUsername()+" "+"training"+" "+train_batch_size;
               Process p = Runtime.getRuntime().exec(command);
                ImageAttributeService imageAttributeService = new ImageAttributeService();
                imageAttributeService.printUpdate(p, " ");
            } catch (IOException e) {
                e.printStackTrace();
            }

            Payment payment = new Payment();
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


        if (training.getImage_path().equals("na")) {
            ConfigurationService configurationService=new ConfigurationService();
            configurationService.setParams();
            String python_root_dir=configurationService.getPython_base_dir();
            int train_batch_size =configurationService.getTrain_batch_size();
            int eval_batch_size = configurationService.getEval_batch_size();



            try {
                String command=configurationService.getPythonCommand()+" "+python_root_dir+"utils.py generateImageID"+" "+training.getUsername()+" "+"training"+" "+train_batch_size;

                Process  p = Runtime.getRuntime().exec(command);
                ImageAttributeService imageAttributeService = new ImageAttributeService();
                imageAttributeService.printUpdate(p, " ");


                if (Integer.parseInt(index.getTrainCompleted())>configurationService.getTrainingThreshold()) {


                    command = configurationService.getPythonCommand() + " " + python_root_dir + "configuration.py update";
                    p = Runtime.getRuntime().exec(command);

                    imageAttributeService.printUpdate(p, "update");
                }

                training.setTrainingInstances();






            }
            catch (IOException e) {
                e.printStackTrace();
            }




            training.readId();
            model.addAttribute("training", training);
            return "training";
        }
        else {


            model.addAttribute("training", training);
            return "training";
        }

    }
}
