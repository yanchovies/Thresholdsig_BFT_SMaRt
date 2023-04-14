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

public class EVserver extends DefaultSingleRecoverable{
    private Map<String, Vehicle> vehiclesRegistered;
    private Map<String, Integer> usersRegistered;
    //private Map<String, User> usersRegistered;
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
                    if (vehiclesRegistered.containsKey(vehicle.vehicleID)) {
                        objOut.writeObject("Not possible. Vehicle " + vehicle.vehicleID + " is already registered.");
                    } else {
                        vehiclesRegistered.put(vehicle.vehicleID, vehicle);
                        objOut.writeObject("The vehicle " + vehicle.vehicleID + " is now successfully registered.");
                    }
                    hasReply = true;
                    break;
                case REGISTERUSER:
                    User user = (User)objIn.readObject();
                    if (usersRegistered.containsKey(user.userID)) {
                        objOut.writeObject("Not possible. User with the user Id " + user.userID + " is already registered.");
                    } else {
                        usersRegistered.put(user.userID, user.userBalance);
                        objOut.writeObject("The user with the user Id " + user.userID + " is now successfully registered.");
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
                            if (!vehicle1.isAvailable) {
                                objOut.writeObject("Not possible. Vehicle with the vehicle Id " + vehicleID + " is already booked.");
                            } else {
                                if (usersRegistered.get(userID) < vehicle1.depositPrice) {
                                    objOut.writeObject("Not possible. User with the user Id " + userID + " does not have enough balance.");
                                } else {
                                    vehicle1.isAvailable = false;
                                    vehicle1.currentUserID = userID;

                                    // producing a deposit payment
                                    usersRegistered.put(userID, usersRegistered.get(userID) - vehicle1.depositPrice);
                                    vehicle1.vehicleOwnerBalance += vehicle1.depositPrice;

                                    vehiclesRegistered.put(vehicleID, vehicle1);
                                    objOut.writeObject("The vehicle with the vehicle Id " + vehicleID + " is now successfully booked.");
                                }
                            }
                        }
                    }
                    hasReply = true;
                    break;
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
            usersRegistered = (Map<String, Integer>)objIn.readObject();
            // usersRegistered = (Map<String, User>)objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error while installing snapshot", e);
        }
    }
}
