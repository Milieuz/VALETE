package com.techelevator.model.vehicleapi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techelevator.model.vehicleapi.Model;

public class ModelResponse {
    @JsonProperty("Count")
    private int count;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("Results")
    private List<Model> results;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Model> getResults() {
        return results;
    }

    public void setResults(List<Model> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "ModelResponse{" +
                "count=" + count +
                ", message='" + message + '\'' +
                ", results=" + results +
                '}';
    }
}
