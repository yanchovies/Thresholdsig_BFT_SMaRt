package bftsmart.demo.EVsharing;

import bftsmart.tom.ServiceProxy;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EVclient {

    ServiceProxy serviceProxy;

    public EVclient(int clientId) {
        serviceProxy = new ServiceProxy(clientId);
    }

    public String registerUser(String userID) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(EVRequestType.REGISTERUSER);
            objOut.writeObject(userID);

            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            if (reply.length == 0)
                return null;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                return (String)objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception registering a user: " + e.getMessage());
        }
        return null;
    }

    public String registerVehicle(String vehicleID) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(EVRequestType.REGISTERVEHICLE);
            objOut.writeObject(vehicleID);

            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            if (reply.length == 0)
                return null;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                return (String)objIn.readObject();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception registering a vehicle: " + e.getMessage());
        }
        return null;
    }

    public void close() {
        serviceProxy.close();
    }

}
