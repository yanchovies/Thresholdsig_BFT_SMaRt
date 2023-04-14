package bftsmart.demo.EVsharing;

import java.io.Serial;
import java.io.Serializable;

public class Vehicle implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    String vehicleID;

    int vehicleOwnerBalance;

    boolean isAvailable;

    String currentUserID;

    //int bookingPeriod;

    int depositPrice;

    int vehiclePricePerDay;

    boolean needsRepair;

    int vehicleRepairPercentageOfFee;

    int vehicleRepairAdditionalCost;

    public Vehicle(String vehicleID, int vehicleOwnerBalance, boolean isAvailable, int depositPrice, int vehiclePricePerDay, int vehicleRepairPercentageOfFee) {
        this.vehicleID = vehicleID;
        this.vehicleOwnerBalance = vehicleOwnerBalance;
        this.isAvailable = isAvailable;
        this.currentUserID = "";
        //this.bookingPeriod = 0;
        this.depositPrice = depositPrice;
        this.vehiclePricePerDay = vehiclePricePerDay;
        this.needsRepair = false;
        this.vehicleRepairPercentageOfFee = vehicleRepairPercentageOfFee;
        this.vehicleRepairAdditionalCost = 0;
    }
}
