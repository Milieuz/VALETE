package com.techelevator.model.valet;

import java.time.Instant;

public class NewlyCreatedInvite {
    private int id;
    private String valetCode;
    private Instant expirationTime;
    private int useLimit;
    public NewlyCreatedInvite(int id, String valetCode, Instant expirationTime, int useLimit){
        this.id = id;
        this.valetCode = valetCode;
        this.expirationTime = expirationTime;
        this.useLimit = useLimit;
    }
    public int getId() {
        return id;
    }

    public String getValetCode() {
        return this.valetCode;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    public int getUseLimit() {
        return useLimit;
    }
}
