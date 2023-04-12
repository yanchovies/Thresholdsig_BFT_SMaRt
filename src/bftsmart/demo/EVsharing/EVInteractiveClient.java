package bftsmart.demo.EVsharing;


import java.io.Console;
import java.util.HashSet;
import java.util.Set;

public class EVInteractiveClient {
    public static void main(String[] args) {
        if(args.length < 1) {
            System.out.println("Usage: demo.map.MapInteractiveClient <client id>");
        }

        int clientId = Integer.parseInt(args[0]);
        EVclient client = new EVclient(clientId);
        Console console = System.console();

        boolean exit = false;
        String result, user, vehicle;
        Set<String> vehiclesRegistered = new HashSet<>();
        Set<String> usersRegistered = new HashSet<>();
        while(!exit) {
            System.out.println("Select an option:");
            System.out.println("0 - Terminate this client");
            System.out.println("1 - Register EV");
            System.out.println("2 - Register User");
//            System.out.println("3 - Removes value from the map");
//            System.out.println("4 - Retrieve the size of the map");
//            System.out.println("5 - List all keys available in the table");

            int cmd = Integer.parseInt(console.readLine("Option:"));

            switch (cmd) {
                case 0:
                    client.close();
                    exit = true;
                    break;
                case 1:
                    user = console.readLine("Enter the user ID:");
                    result = client.registerUser(user);
                    System.out.println("The outcome of the operation is: " + result + ".");
//                    if (!usersRegistered.contains(user)) {
//                        usersRegistered.add(user);
//                        result = "Success";
//                        System.out.println("User " + user + " is now successfully registered. The outcome of the operation is: " + result + ".");
//                        break;
//                    }
//                    else {
//                        result = "Failure";
//                        System.out.println("User " + user + " is already registered. The outcome of the operation is: " + result + ".");
//                        break;
//                    }
                case 2:
                    vehicle = console.readLine("Enter the vehicle ID:");
                    result = client.registerVehicle(vehicle);
                    System.out.println("The outcome of the operation is: " + result + ".");
//                    if (!vehiclesRegistered.contains(vehicle)) {
//                        vehiclesRegistered.add(vehicle);
//                        result = "Success";
//                        System.out.println("User " + vehicle + " is now successfully registered. The outcome of the operation is: " + result + ".");
//                        break;
//                    }
//                    else {
//                        result = "Failure";
//                        System.out.println("User " + vehicle + " is already registered. The outcome of the operation is: " + result + ".");
//                        break;
//                    }
//                case 3:
//                    System.out.println("Removing value in the map");
//                    key = console.readLine("Enter the key:");
//                    result =  map.remove(key);
//                    System.out.println("Value removed: " + result);
//                    break;
//                case 4:
//                    System.out.println("Getting the map size");
//                    int size = map.size();
//                    System.out.println("Map size: " + size);
//                    break;
//                case 5:
//                    System.out.println("Getting all keys");
//                    Set<String> keys = map.keySet();
//                    System.out.println("Total number of keys found: " + keys.size());
//                    for (String k : keys)
//                        System.out.println("---> " + k);
//                    break;
                default:
                    break;
            }
        }
    }
}
