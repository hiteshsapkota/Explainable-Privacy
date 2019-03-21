package com.expriv.service;

import com.opencsv.CSVWriter;
import com.expriv.model.Evaluation;
import com.expriv.model.Feedback;
import com.expriv.model.FeedbackDto;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ImageAttributeService{

    private String image_id;
    private List<String> attributes =new ArrayList<>();
    public String getImage_id() {
      return image_id;
    }
    public void setImage_id(String image_id) {
      this.image_id = image_id;
    }
    public List<String> getAttributes() {
      return attributes;
    }

    public void setAttributes() throws IOException {
      ConfigurationService configurationService=new ConfigurationService();
      configurationService.setParams();
      String program_path = configurationService.getPython_base_dir();
      String command="python2.7"+" "+program_path+"utils.py getImageAttributes"+" "+image_id;
      Process p = Runtime.getRuntime().exec(command);
      BufferedReader reader = new BufferedReader(
              new InputStreamReader(p.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        this.attributes.add(line + "\n");
      }

      int exitVal = 0;
      try {
        exitVal = p.waitFor();
      } catch (InterruptedException e) {
        System.out.println("Unsuccess");
        e.printStackTrace();
      }
      if (exitVal == 0) {
        System.out.println("Success!");

      }
      else {

        System.out.println("Abnormal");

      }

    }

    public void transferValues (FeedbackDto feedbackDto ) throws IOException
    {

      ConfigurationService configurationService=new ConfigurationService();
      configurationService.setParams();
      String python_base_dir = configurationService.getPython_base_dir();
      File file = new File(python_base_dir+"instance_attribute_feedback.csv");
      FileWriter outputfile = new FileWriter(file);
      CSVWriter writer=new CSVWriter(outputfile);
      String[] header = { "Attribute", "Value"};
      writer.writeNext(header);

      for (Feedback feedback:feedbackDto.getFeedbacks())
      {

        String[] data1={feedback.getAttributeName(), Integer.toString(feedback.getAttributeValue())};
        writer.writeNext(data1);

      }
      writer.close();
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      Evaluation evaluation=new Evaluation();
      evaluation.setUsername(principal);
      String command="python2.7"+" "+python_base_dir+"utils.py storeFeedback"+" "+evaluation.getUsername();
      Runtime.getRuntime().exec(command);
    }

}



