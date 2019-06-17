package com.expriv.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationService {
    private String python_base_dir;
    private int train_batch_size;
    private int eval_batch_size;
    private int trainingThreshold;
    private String pythonCommand;
    public String getPython_base_dir() {
        return python_base_dir;
    }

    public void setParams() {
        Properties prop = new Properties();

        try
        {
            prop.load(new FileInputStream("src/main/resources/application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.python_base_dir =(String)prop.get("python_base_path");
        this.train_batch_size=Integer.parseInt((String)prop.get("train_batch_size"));
        this.eval_batch_size=Integer.parseInt((String)prop.get("eval_batch_size"));
        this.trainingThreshold=Integer.parseInt((String)prop.get("training_threshold"));
        this.pythonCommand = (String)prop.get("pythonCommand");

    }

    public int getTrain_batch_size() {
        return train_batch_size;
    }

    public void setTrain_batch_size(int train_batch_size) {
        this.train_batch_size = train_batch_size;
    }

    public int getEval_batch_size() {
        return eval_batch_size;
    }

    public void setEval_batch_size(int eval_batch_size) {
        this.eval_batch_size = eval_batch_size;
    }

    public int getTrainingThreshold() {
    return trainingThreshold;
  }

    public void setTrainingThreshold(int trainingThreshold) {
    this.trainingThreshold = trainingThreshold;
  }

    public String getPythonCommand() {
        return pythonCommand;
    }

    public void setPythonCommand(String pythonCommand) {
        this.pythonCommand = pythonCommand;
    }
}
