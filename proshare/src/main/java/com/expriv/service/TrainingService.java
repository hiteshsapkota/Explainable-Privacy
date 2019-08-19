package com.expriv.service;

import com.expriv.model.Evaluation;
import com.expriv.model.Index;
import com.expriv.model.Training;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import java.io.IOException;

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
        if ((Integer.parseInt(index.getTrainCompleted()))%50==0)
        {
            return "training_complete";
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
