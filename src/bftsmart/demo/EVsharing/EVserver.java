package bftsmart.demo.EVsharing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;


import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;

public class EVserver extends DefaultSingleRecoverable{
    private Set<String> vehiclesRegistered;
    private Set<String> usersRegistered;
    private Logger logger;

    public EVserver(int id) {
        //TODO: add relevant variables to the constructor
        vehiclesRegistered = new HashSet<String>();
        usersRegistered = new HashSet<String>();

        logger = Logger.getLogger(EVserver.class.getName());
        new ServiceReplica(id, this, this);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: demo.map.MapServer <server id>");
            System.exit(-1);
        }
        new EVserver(Integer.parseInt(args[0]));
    }

    @SuppressWarnings("unchecked")
    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        byte[] reply = null;
        String vehicleID = null;
        String userID = null;

        boolean hasReply = false;
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
            EVRequestType reqType = (EVRequestType) objIn.readObject();
            switch (reqType) {
                case REGISTERVEHICLE:
                    vehicleID = (String)objIn.readObject();
                    if (usersRegistered.contains(vehicleID)) {
                        objOut.writeObject("Vehicle " + vehicleID + " is already registered.");
                    } else {
                        vehiclesRegistered.add(vehicleID);
                        objOut.writeObject("The vehicle" + vehicleID + " is now successfully registered.");
                    }
                    hasReply = true;
                    break;
                case REGISTERUSER:
                    userID = (String)objIn.readObject();
                    if (usersRegistered.contains(userID)) {
                        objOut.writeObject("Vehicle " + userID + " is already registered.");
                    } else {
                        vehiclesRegistered.add(userID);
                        objOut.writeObject("The vehicle" + userID + " is now successfully registered.");
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

//        boolean hasReply = false;

//        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
//             ObjectInput objIn = new ObjectInputStream(byteIn);
//             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
//             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
//            MapRequestType reqType = (MapRequestType)objIn.readObject();
//            switch (reqType) {
//                case GET:
//                    key = (K)objIn.readObject();
//                    value = replicaMap.get(key);
//                    if (value != null) {
//                        objOut.writeObject(value);
//                        hasReply = true;
//                    }
//                    break;
//                case SIZE:
//                    int size = replicaMap.size();
//                    objOut.writeInt(size);
//                    hasReply = true;
//                    break;
//                case KEYSET:
//                    keySet(objOut);
//                    hasReply = true;
//                    break;
//                default:
//                    logger.log(Level.WARNING, "in appExecuteUnordered only read operations are supported");
//            }
//            if (hasReply) {
//                objOut.flush();
//                byteOut.flush();
//                reply = byteOut.toByteArray();
//            } else {
//                reply = new byte[0];
//            }
//        } catch (IOException | ClassNotFoundException e) {
//            logger.log(Level.SEVERE, "Ocurred during EV operation execution", e);
//        }

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
            vehiclesRegistered = (HashSet<String>)objIn.readObject();
            usersRegistered = (HashSet<String>)objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error while installing snapshot", e);
        }
    }
}
