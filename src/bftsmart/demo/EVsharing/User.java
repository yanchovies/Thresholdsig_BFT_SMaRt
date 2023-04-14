package bftsmart.demo.EVsharing;

import java.io.Serial;
import java.io.Serializable;

public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;
    String userID;
    int userBalance;

    public  User(String userID, int userBalance) {
        this.userID = userID;
        this.userBalance = userBalance;
    }


}
