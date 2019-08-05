package com.expriv.model;

import java.util.ArrayList;
import java.util.List;

public class ExplanationDto {

        private List<Explanation> explanations =new ArrayList<>();

        // default and parameterized constructor

        public void addExplanation(Explanation explanation) {
            this.explanations.add(explanation);
        }

        // getter and setter

        public List<Explanation> getExplanations() {
            return explanations;
        }

        public void setExplanations(List<Explanation> explanations) {
            this.explanations = explanations;
        }


}
