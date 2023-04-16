package bftsmart.demo.EVsharing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;

import java.lang.Math;

public class EVserver extends DefaultSingleRecoverable{
    private Map<String, Vehicle> vehiclesRegistered;
    // private Map<String, Integer> usersRegistered;
    private Map<String, User> usersRegistered;
    private Logger logger;

    public EVserver(int id) {
        vehiclesRegistered = new HashMap<>();

        usersRegistered = new HashMap<>();

        logger = Logger.getLogger(EVserver.class.getName());
        new ServiceReplica(id, this, this);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: demo.EVsharing.EVserver <server id>");
            System.exit(-1);
        }
        new EVserver(Integer.parseInt(args[0]));
    }

    @SuppressWarnings("unchecked")
    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        byte[] reply = null;
        boolean hasReply = false;
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
            EVRequestType reqType = (EVRequestType) objIn.readObject();
            switch (reqType) {
                case REGISTERVEHICLE:
                    Vehicle vehicle = (Vehicle)objIn.readObject();
                    if (vehiclesRegistered.containsKey(vehicle.getVehicleID())) {
                        objOut.writeObject("Not possible. Vehicle " + vehicle.getVehicleID() + " is already registered.");
                    } else {
                        vehiclesRegistered.put(vehicle.getVehicleID(), vehicle);
                        objOut.writeObject("The vehicle " + vehicle.getVehicleID() + " is now successfully registered.");
                    }
                    hasReply = true;
                    break;
                case REGISTERUSER:
                    User user = (User)objIn.readObject();
                    if (usersRegistered.containsKey(user.getUserID())) {
                        objOut.writeObject("Not possible. User with the user Id " + user.getUserID() + " is already registered.");
                    } else {
                        usersRegistered.put(user.getUserID(), user);
                        objOut.writeObject("The user with the user Id " + user.getUserID() + " is now successfully registered.");
                    }
                    hasReply = true;
                    break;
                case BOOKVEHICLE:
                    AbstractMap.SimpleEntry<String, String> pair = (AbstractMap.SimpleEntry<String, String>)objIn.readObject();
                    String userID = pair.getKey();
                    if (!usersRegistered.containsKey(userID)) {
                        objOut.writeObject("Not possible. User with the user Id " + userID + " is not registered.");
                    } else {
                        String vehicleID = pair.getValue();
                        if (!vehiclesRegistered.containsKey(vehicleID)) {
                            objOut.writeObject("Not possible. Vehicle with the vehicle Id " + vehicleID + " is not registered.");
                        } else {
                            Vehicle vehicle1 = vehiclesRegistered.get(vehicleID);
                            if (!vehicle1.getIsAvailable()) {
                                objOut.writeObject("Not possible. Vehicle with the vehicle Id " + vehicleID + " is already booked.");
                            } else {
                                if (usersRegistered.get(userID).getUserBalance() < vehicle1.getDepositPrice()) {
                                    objOut.writeObject("Not possible. User with the user Id " + userID + " does not have enough balance.");
                                } else {
                                    vehicle1.setIsAvailable(false);
                                    vehicle1.setCurrentUserID(userID);

                                    // producing a deposit payment and giving an access code for the vehicle
                                    User user1 = usersRegistered.get(userID);
                                    user1.setCurrentVehicleAccessCode(vehicle1.getVehicleAccessCode());
                                    user1.setUserBalance(user1.getUserBalance() - vehicle1.getDepositPrice());
                                    usersRegistered.put(userID, user1);
                                    vehicle1.setVehicleOwnerBalance(vehicle1.getVehicleOwnerBalance() + vehicle1.getDepositPrice());

                                    vehiclesRegistered.put(vehicleID, vehicle1);
                                    objOut.writeObject("The vehicle with the vehicle Id " + vehicleID + " is now successfully booked. The current user of the vehicle is " + vehiclesRegistered.get(vehicleID).getCurrentUserID());
                                }
                            }
                        }
                    }
                    hasReply = true;
                    break;
                case RETURNVEHICLE:
                    AbstractMap.SimpleEntry<String, String> quartet = (AbstractMap.SimpleEntry<String, String>)objIn.readObject();
                    String vehicleID1 = quartet.getValue();
//                    Quartet<String, String, Float, Float> quartet = (Quartet<String, String, Float, Float>)objIn.readObject();
//                    String vehicleID1 = quartet.getSecond();
                    if (!vehiclesRegistered.containsKey(vehicleID1)) {
                        objOut.writeObject("Not possible. Vehicle with the vehicle Id " + vehicleID1 + " is not registered.");
                    } else {
//                        String userID1 = quartet.getFirst();
                        String userID1 = quartet.getKey();
                        if (!Objects.equals(vehiclesRegistered.get(vehicleID1).getCurrentUserID(), userID1)) {
                            objOut.writeObject("Not possible. User " + userID1 + " is not using the vehicle " + vehicleID1 + ". The current user of the vehicle " + vehicleID1 + " is " + vehiclesRegistered.get(vehicleID1).getCurrentUserID());
                        } else {

//                            float price = vehiclesRegistered.get(vehicleID1).getVehiclePricePerHour() * quartet.getThird() + vehiclesRegistered.get(vehicleID1).getVehiclePricePerKm() * quartet.getFourth() - vehiclesRegistered.get(vehicleID1).getDepositPrice();
                            float price = vehiclesRegistered.get(vehicleID1).getVehiclePricePerHour() * 20 + vehiclesRegistered.get(vehicleID1).getVehiclePricePerKm() * 15 - vehiclesRegistered.get(vehicleID1).getDepositPrice();
                            float finalPrice = price + price * vehiclesRegistered.get(vehicleID1).getVehicleRepairPercentageOfFee() / 100;

                            // producing a random number between 0 and 1 to simulate if the vehicle needs repair
                            int needsRepair = (int)(Math.random() * 2);
                            int maintenanceRepairCost = 0;
                            if (needsRepair == 1) {
                                // producing a random number between 1 and the deposit price to simulate the repair cost
                                // for the sake of simplicity, we assume that the repair cost is always less than the deposit price
                                maintenanceRepairCost = (int)(Math.random() * vehiclesRegistered.get(vehicleID1).getDepositPrice() + 1);
                            }

                            finalPrice += maintenanceRepairCost;

                            Vehicle vehicle2 = vehiclesRegistered.get(vehicleID1);
                            User user2 = usersRegistered.get(userID1);

                            // simulating transaction
                            vehicle2.setVehicleOwnerBalance(vehicle2.getVehicleOwnerBalance() + finalPrice);
                            user2.setUserBalance(user2.getUserBalance() - finalPrice);

                            // updating other fields of current vehicle and user
                            user2.setCurrentVehicleAccessCode(null);
                            vehicle2.setIsAvailable(true);
                            vehicle2.setCurrentUserID(null);
                            // new access code is generated for the vehicle
                            vehicle2.setVehicleAccessCode();

                            List<String> helperListForUsers = user2.getIDsOfVehiclesUsed();
                            helperListForUsers.add(vehicleID1);
                            vehicle2.setIDsOfUsersThatUsedVehicle(helperListForUsers);

                            List<String> helperListForVehicles = vehicle2.getIDsOfUsersThatUsedVehicle();
                            helperListForVehicles.add(userID1);
                            vehicle2.setIDsOfUsersThatUsedVehicle(helperListForVehicles);

                            vehiclesRegistered.put(vehicleID1, vehicle2);
                            usersRegistered.put(userID1, user2);
                        }
                    }
                    hasReply = true;
                    break;
                case DISPUTE:
                    AbstractMap.SimpleEntry<String, String> pair1 = (AbstractMap.SimpleEntry<String, String>)objIn.readObject();
                    String userID2 = pair1.getKey();
                    if (!usersRegistered.containsKey(userID2)) {
                        objOut.writeObject("Not possible. User with the user Id " + userID2 + " is not registered.");
                    } else {
                        String vehicleID2 = pair1.getValue();
                        if (!vehiclesRegistered.containsKey(vehicleID2)) {
                            objOut.writeObject("Not possible. Vehicle with the vehicle Id " + vehicleID2 + " is not registered.");
                        } else {
                            if ((!vehiclesRegistered.get(vehicleID2).getIDsOfUsersThatUsedVehicle().contains(userID2)) || (!usersRegistered.get(userID2).getIDsOfVehiclesUsed().contains(vehicleID2))) {
                                objOut.writeObject("Not possible. The ID history of the user and the vehicle do not coincide. User " + userID2 + " did not use the vehicle " + vehicleID2);
                            } else {
                                // simulating the outcome of the dispute. If the outcome is 1, the user wins the dispute and gets some compensation.
                                int disputeOutcome = (int)(Math.random() * 2);
                                int compensation = 0;
                                if (disputeOutcome == 1) {
                                    // producing a random number to simulate the compensation
                                    // for the sake of simplicity, we assume that the compensation is always some refund,
                                    // and it lies in the range between the deposit price and the deposite price x2
                                    compensation = (int)(Math.random() * (vehiclesRegistered.get(vehicleID2).getDepositPrice() + 1) + vehiclesRegistered.get(vehicleID2).getDepositPrice());

                                // simulating transaction
                                // if the user wins the dispute, the vehicle owner pays the compensation to user
                                // if the user loses the dispute, nothing happens
                                Vehicle vehicle3 = vehiclesRegistered.get(vehicleID2);
                                User user3 = usersRegistered.get(userID2);
                                vehicle3.setVehicleOwnerBalance(vehicle3.getVehicleOwnerBalance() - compensation);
                                user3.setUserBalance(user3.getUserBalance() + compensation);

                                // storing the results
                                vehiclesRegistered.put(vehicleID2, vehicle3);
                                usersRegistered.put(userID2, user3);
                                }
                            }
                        }
                    }
                    hasReply = true;
                    break;
//                case MAINTENANCEANDREPAIR:
//                    Triple<String, String, Float> triple = (Triple<String, String, Float>)objIn.readObject();
//                    String vehicleID2 = triple.getFirst();
//                    if (!vehiclesRegistered.containsKey(vehicleID2)) {
//                        objOut.writeObject("Not possible. Vehicle with the vehicle Id " + vehicleID2 + " is not registered.");
//                    } else {
//                        String userID2 = triple.getSecond();
//                        if ((!vehiclesRegistered.get(vehicleID2).getIDsOfUsersThatUsedVehicle().contains(userID2)) || (!usersRegistered.get(userID2).getIDsOfVehiclesUsed().contains(vehicleID2))) {
//                            objOut.writeObject("Not possible. User " + userID2 + " did not use the vehicle " + vehicleID2);
//                        } else {
//
//                        }
//                    }
//                    hasReply = true;
//                    break;
            }
            if (hasReply) {
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            } else {
                reply = new byte[0];
            }

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Ocurred during EV operation execution", e);
        }
        return reply;
    }

    @SuppressWarnings("unchecked")
    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        byte[] reply = null;

        return reply;
    }

    @Override
    public byte[] getSnapshot() {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
            objOut.writeObject(vehiclesRegistered);
            objOut.writeObject(usersRegistered);
            return byteOut.toByteArray();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while taking snapshot", e);
        }
        return new byte[0];
    }

    @SuppressWarnings("unchecked")
    @Override
    public void installSnapshot(byte[] state) {
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(state);
             ObjectInput objIn = new ObjectInputStream(byteIn)) {
            vehiclesRegistered = (Map<String, Vehicle>)objIn.readObject();
            // usersRegistered = (Map<String, Integer>)objIn.readObject();
            usersRegistered = (Map<String, User>)objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error while installing snapshot", e);
        }
    }
}
