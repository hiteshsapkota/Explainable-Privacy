package com.expriv.model;

import com.expriv.model.Feedback;

import java.util.ArrayList;
import java.util.List;

public class FeedbackDto {

  private List<Feedback> feedbacks =new ArrayList<>();

  // default and parameterized constructor

  public void addFeedback(Feedback feedback) {
    this.feedbacks.add(feedback);
  }

  // getter and setter

  public List<Feedback> getFeedbacks() {
    return feedbacks;
  }

  public void setFeedbacks(List<Feedback> feedbacks) {
    this.feedbacks = feedbacks;
  }
}

