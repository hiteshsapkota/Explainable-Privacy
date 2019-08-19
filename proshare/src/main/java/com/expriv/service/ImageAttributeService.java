package com.expriv.service;

import com.expriv.model.*;
import com.opencsv.CSVWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.*;
import java.sql.PreparedStatement;
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

    public void setAttributes(JdbcTemplate jdbcTemplate) throws IOException {
      ConfigurationService configurationService=new ConfigurationService();
      configurationService.setParams();
      String program_path = configurationService.getPython_base_dir();
      String command=configurationService.getPythonCommand()+" "+program_path+"utils.py getImageAttributes"+" "+image_id;
      Process p = Runtime.getRuntime().exec(command);
      printUpdate(p, "attribute");

    }
    public void printUpdate (Process p, String message) throws IOException
    {
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
            if (message.equals("attribute"))
            {
                System.out.println("Successful in getting image attributes");
            }
            else if (message.equals("update")) {
                System.out.println("Success in updating!");
            }
            else if(message.equals("imageread"))
            {
                System.out.println("Successful in reading and storing the data to the training database");
            }

        }
        else {

            System.out.println("Abnormal");

        }

    }


  public CSVWriter createFile(String python_base_dir)
  {
      File file = new File(python_base_dir+"instance_attribute_feedback.csv");
      FileWriter outputfile = null;
      try {
          outputfile = new FileWriter(file);
      } catch (IOException e) {
          e.printStackTrace();
      }
      CSVWriter writer=new CSVWriter(outputfile);
      String[] header = { "Attribute", "Value"};
      writer.writeNext(header);
      return writer;



  }

  public void storetoFile(FeedbackDto feedbackDto, CSVWriter writer, String type)
  {
      String[] data;

      for (Feedback feedback: feedbackDto.getFeedbacks())
      {
          if (type.equals("first"))
              data = new String[]{feedback.getAttributeName(), Integer.toString(0)};
           else
              data = new String[]{feedback.getAttributeName(), feedback.getAttributeSensitivity()};
          writer.writeNext(data);




      }
      try {
          writer.close();
      } catch (IOException e) {
          e.printStackTrace();
      }


  }

  public void storetoDatabase(JdbcTemplate jdbcTemplate, String otherAttributes, String type, FeedbackDto feedbackDto, int id)
  {
      int totalAttributes = feedbackDto.getFeedbacks().size();
      int i=0;
      String sensAttributes = "";
      String insensAttributes = "";
      for (Feedback feedback: feedbackDto.getFeedbacks())
      {
          if (type.equals("first"))
          {
             insensAttributes+=feedback.getAttributeName();
          }
          else
          {
              if (Integer.parseInt(feedback.getAttributeSensitivity())==1)
                  sensAttributes+=feedback.getAttributeName();
              else
                  insensAttributes+=feedback.getAttributeName();
          }


          if (i<(totalAttributes-1))
          {
              insensAttributes+=", ";
          }
      }
      jdbcTemplate.update("update evaluation set sensitive_attributes="+"'"+sensAttributes+"'"+"where id ="+Integer.toString(id)+";");
      jdbcTemplate.update("update evaluation set insensitive_attributes="+"'"+insensAttributes+"'"+"where id ="+Integer.toString(id)+";");
      if (!type.equals("first"))
      jdbcTemplate.update("update evaluation set other_attributes="+"'"+otherAttributes+"'"+"where id ="+Integer.toString(id)+";");




  }
    public void transferValues (FeedbackDto feedbackDto, JdbcTemplate jdbcTemplate, String otherAttributes, int id, Evaluation evaluation, boolean insufficient) throws IOException
    {
        ConfigurationService configurationService=new ConfigurationService();
        configurationService.setParams();
        String python_base_dir = configurationService.getPython_base_dir();


        if ((evaluation.getRecommendation()==1 && evaluation.getFeedbackRecomm().equals("Agree")) || (evaluation.getRecommendation()==0 && evaluation.getFeedbackRecomm().equals("Disagree")))
        {
            CSVWriter writer = createFile(python_base_dir);

            int i=0;
            String insensAttributes = "";
            storetoFile(feedbackDto, writer, "first");
            storetoDatabase(jdbcTemplate, otherAttributes, "first", feedbackDto, id);

            //Implicitly pass each attribute values as 0 (insensitive)
            // Set sensitive attributes =0, Set Insensitive Attributes = all present attributes
            // No other attributes taken into consideration


        }

        else if (evaluation.getRecommendation()==0 && evaluation.getFeedbackRecomm().equals("Agree"))
        {

            if (insufficient)
            {
                CSVWriter writer = createFile(python_base_dir);
                storetoFile(feedbackDto, writer, "second");
                storetoDatabase(jdbcTemplate, otherAttributes, "second", feedbackDto, id);
                //Explicitly pass each attribute values
                // Store sensitive and insensitive values
                // Other sensitive attributes are taken into consideration
            }
            else
            {
                if (evaluation.getFeedbackExp().equals("Disagree"))
                {
                    CSVWriter writer = createFile(python_base_dir);
                    storetoFile(feedbackDto, writer, "third");
                    storetoDatabase(jdbcTemplate, otherAttributes, "third", feedbackDto, id);




                    //Explicitly pass each attribute values
                    // Store sensitive and insensitive values
                    // Other sensitive attributes are taken into consideration
                }

            }
        }

      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      evaluation=new Evaluation();
      evaluation.setUsername(principal);

      String command=configurationService.getPythonCommand()+" "+python_base_dir+"utils.py storeFeedback"+" "+evaluation.getUsername();
      Runtime.getRuntime().exec(command);


    }
}





