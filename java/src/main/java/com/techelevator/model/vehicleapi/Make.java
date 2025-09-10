package com.techelevator.model.vehicleapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Make {
    @JsonProperty("Make_ID")
    private int makeId;

    @JsonProperty("Make_Name")
    private String makeName;

    public int getMakeId() {
        return makeId;
    }

    public void setMakeId(int makeId) {
        this.makeId = makeId;
    }

    public String getMakeName() {
        return makeName;
    }

    public void setMakeName(String makeName) {
        this.makeName = makeName;
    }

    @Override
    public String toString() {
        return "Make{" +
                "makeId=" + makeId +
                ", makeName='" + makeName + '\'' +
                '}';
    }
}
