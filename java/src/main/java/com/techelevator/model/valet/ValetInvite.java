package com.techelevator.model.valet;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;

// New model for ValetInvite object
public class ValetInvite {

    private int id;
    private String valetSecretHash;
    private Instant expirationTime;
    private int useLimit;
    private int useCount;
    private boolean isRevoked;
    private int creator;
    private Instant creationTime;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getValetSecretHash() {
        return valetSecretHash;
    }

    public void setValetSecretHash(String valetSecretHash) {
        this.valetSecretHash = valetSecretHash;
    }

    @JsonIgnore
    public Instant getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Instant expirationTime) {
        this.expirationTime = expirationTime;
    }

    public int getUseLimit() {
        return useLimit;
    }

    public void setUseLimit(int useLimit) {
        this.useLimit = useLimit;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    public boolean getIsRevoked() {
        return isRevoked;
    }

    public void setIsRevoked(boolean revoked) {
        isRevoked = revoked;
    }
    public int getCreator() {
        return creator;
    }

    public void setCreator(int creator) {
        this.creator = creator;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }

}
