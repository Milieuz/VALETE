package com.techelevator.model.vehicleapi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techelevator.model.vehicleapi.Make;

public class MakeResponse {
    @JsonProperty("Count")
    private int count;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("Results")
    private List<Make> results;

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

    public List<Make> getResults() {
        return results;
    }

    public void setResults(List<Make> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "MakeResponse{" +
                "count=" + count +
                ", message='" + message + '\'' +
                ", results=" + results +
                '}';
    }
}
