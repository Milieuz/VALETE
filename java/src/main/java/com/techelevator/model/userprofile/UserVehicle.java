package com.techelevator.model.userprofile;

public class UserVehicle {
    private Integer id;
    private Integer userId;
    private String makeName;
    private String modelName;
    private String color;
    private String licensePlate;
    private String vin;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getMakeName() { return makeName; }
    public void setMakeName(String makeName) { this.makeName = makeName; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
}
