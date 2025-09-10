package com.techelevator.model.valet;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class InviteRequest {
    @Min(1) @Max(60*24*7)
    private int requestValidPeriod;
    @Min(1) @Max(50)
    private int useLimit;

    public InviteRequest(){}

    public Integer getRequestValidPeriod(){
        return this.requestValidPeriod;
    }

    public void setRequestValidPeriod(Integer requestValidPeriod){
        this.requestValidPeriod = requestValidPeriod;
    }

    public Integer getUseLimit(){
        return this.useLimit;
    }
    public void setUseLimit(Integer useLimit){
        this.useLimit = useLimit;
    }
}
