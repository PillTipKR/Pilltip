package com.oauth2.mlInference.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PredictionResponse {
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public static class Result {
        @JsonProperty("class")
        private String className;

        private double confidence;
        private List<List<Float>> bbox;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        // public List<List<Float>> getBbox() {
        //     return bbox;
        // }

        public void setBbox(List<List<Float>> bbox) {
            this.bbox = bbox;
        }
    }
}
