package bftsmart.demo.EVsharing;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Vehicle implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String vehicleID;

    private String vehicleAccessCode;

    private float vehicleOwnerBalance;

    private boolean isAvailable;

    private String currentUserID;

    //int bookingPeriod;

    private int depositPrice;

    private int vehiclePricePerHour;

    private int vehiclePricePerKm;

    private boolean needsRepair;

    private int vehicleRepairPercentageOfFee;

    private int vehicleRepairAdditionalCost;

    private List<String> IDsOfUsersThatUsedVehicle;

    public Vehicle(String vehicleID, float vehicleOwnerBalance, boolean isAvailable, int depositPrice, int vehiclePricePerHour, int vehiclePricePerKm, int vehicleRepairPercentageOfFee) {
        this.vehicleID = vehicleID;
        this.vehicleAccessCode = getRandomString(8);
        this.vehicleOwnerBalance = vehicleOwnerBalance;
        this.isAvailable = isAvailable;
        this.currentUserID = "";
        //this.bookingPeriod = 0;
        this.depositPrice = depositPrice;
        this.vehiclePricePerHour = vehiclePricePerHour;
        this.vehiclePricePerKm = vehiclePricePerKm;
        this.needsRepair = false;
        this.vehicleRepairPercentageOfFee = vehicleRepairPercentageOfFee;
        this.vehicleRepairAdditionalCost = 0;
        this.IDsOfUsersThatUsedVehicle = new ArrayList<>();
    }

    // getters

    public String getVehicleID() {
        return vehicleID;
    }

    public String getVehicleAccessCode() {
        return vehicleAccessCode;
    }

    public float getVehicleOwnerBalance() {
        return vehicleOwnerBalance;
    }

    public boolean getIsAvailable() {
        return isAvailable;
    }

    public String getCurrentUserID() {
        return currentUserID;
    }

    public int getDepositPrice() {
        return depositPrice;
    }

    public int getVehiclePricePerHour() {
        return vehiclePricePerHour;
    }

    public int getVehiclePricePerKm() {
        return vehiclePricePerKm;
    }

    public boolean getNeedsRepair() {
        return needsRepair;
    }

    public int getVehicleRepairPercentageOfFee() {
        return vehicleRepairPercentageOfFee;
    }

    public int getVehicleRepairAdditionalCost() {
        return vehicleRepairAdditionalCost;
    }

    public List<String> getIDsOfUsersThatUsedVehicle() {
        return IDsOfUsersThatUsedVehicle;
    }

    // setters

    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    public void setVehicleAccessCode() {
        this.vehicleAccessCode = getRandomString(8);
    }

    public void setVehicleOwnerBalance(float vehicleOwnerBalance) {
        this.vehicleOwnerBalance = vehicleOwnerBalance;
    }

    public void setIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public void setCurrentUserID(String currentUserID) {
        this.currentUserID = currentUserID;
    }

    public void setDepositPrice(int depositPrice) {
        this.depositPrice = depositPrice;
    }

    public void setVehiclePricePerHour(int vehiclePricePerHour) {
        this.vehiclePricePerHour = vehiclePricePerHour;
    }

    public void setVehiclePricePerKm(int vehiclePricePerKm) {
        this.vehiclePricePerKm = vehiclePricePerKm;
    }

    public void setNeedsRepair(boolean needsRepair) {
        this.needsRepair = needsRepair;
    }

    public void setVehicleRepairPercentageOfFee(int vehicleRepairPercentageOfFee) {
        this.vehicleRepairPercentageOfFee = vehicleRepairPercentageOfFee;
    }

    public void setVehicleRepairAdditionalCost(int vehicleRepairAdditionalCost) {
        this.vehicleRepairAdditionalCost = vehicleRepairAdditionalCost;
    }

    public void setIDsOfUsersThatUsedVehicle(List<String> IDsOfUsersThatUsedVehicle) {
        this.IDsOfUsersThatUsedVehicle = IDsOfUsersThatUsedVehicle;
    }

    // other methods

    public String getRandomString(int theLength)
    {

        // a string to choose a character from
        String helperString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of our helper string
        StringBuilder stringBuilder = new StringBuilder(theLength);

        for (int i = 0; i < theLength; i++) {

            // generate a random number between 0 to the length of helper string and append a character at the end of string buffer
            stringBuilder.append(helperString.charAt((int)(helperString.length() * Math.random())));
        }

        return stringBuilder.toString();
    }
}