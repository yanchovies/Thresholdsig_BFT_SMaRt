package bftsmart.demo.EVsharing;


import java.io.Console;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class EVInteractiveClient {
    public static void main(String[] args) {
        if(args.length < 1) {
            System.out.println("Usage: demo.map.MapInteractiveClient <client id>");
        }

        int clientId = Integer.parseInt(args[0]);
        EVclient client = new EVclient(clientId);

        boolean exit = false;
        String result;
        Scanner sc = new Scanner(System.in);
        while(!exit) {
            System.out.println("Select an option:");
            System.out.println("0 - Terminate this client");
            System.out.println("1 - Register EV");
            System.out.println("2 - Register User");
            System.out.println("3 - Book an EV");


            int cmd = Integer.parseInt(sc.nextLine());

            switch (cmd) {
                case 0:
                    client.close();
                    exit = true;
                    break;
                case 1:
                    System.out.println("Enter the vehicle ID:");
                    String vehicleID = sc.nextLine();
                    System.out.println("Enter the vehicle owner's balance:");
                    int vehicleOwnerBalance = Integer.parseInt(sc.nextLine());
                    System.out.println("Specify if the vehicle is available for rental:");
                    boolean isAvailable = Boolean.parseBoolean(sc.nextLine());
                    System.out.println("Enter the vehicle's deposit price:");
                    int depositPrice = Integer.parseInt(sc.nextLine());
                    System.out.println("Enter the vehicle's rental price (per day):");
                    int rentalPricePerDay = Integer.parseInt(sc.nextLine());
                    System.out.println("Enter the repair&maintenance percentage of the final fee:");
                    int vehicleRepairPercentageOfFee = Integer.parseInt(sc.nextLine());
                    Vehicle vehicle = new Vehicle(vehicleID, vehicleOwnerBalance, isAvailable, depositPrice, rentalPricePerDay, vehicleRepairPercentageOfFee);
                    result = client.registerVehicle(vehicle);
                    System.out.println(result);
                    break;
                case 2:
                    System.out.println("Enter the user ID:");
                    String userID = sc.nextLine();
                    System.out.println("Enter the initial balance of the user:");
                    int balance = Integer.parseInt(sc.nextLine());
                    // creating a User class object
                    User user = new User(userID, balance);
                    result = client.registerUser(user);
                    System.out.println(result);
                    break;
                case 3:
                    System.out.println("Enter the user ID of the user that would like to book an EV:");
                    String userId = sc.nextLine();
                    System.out.println("Enter the vehicle ID of the vehicle that the user would like to book:");
                    String vehicleId = sc.nextLine();
                    result = client.bookVehicle(userId, vehicleId);
                    System.out.println(result);
                    break;
                default:
                    break;
            }
        }
    }
}
