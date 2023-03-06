package bftsmart.consensus.messages;

import bftsmart.communication.SystemMessage;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigInteger;

/**
 * @Author Moonk
 * @Date 2022/6/19
 */
public class SigShareMessage extends SystemMessage {
    private int number;
    private int id;
    private BigInteger sig;
    public SigShareMessage() {
    }
    public SigShareMessage(final int cid, final int id, final BigInteger sig) {
        this.number = cid;
        this.id = id;
        this.sig = sig;
    }

    public int getNumber() {
        return number;
    }

    public int getId() {
        return this.id;
    }
    public BigInteger getSig() {
        return this.sig;
    }
    @Override
    public String toString() {
        return "SigShareMessage{" +
                "number=" + number +
                ", id=" + id +
                ", sig=" + sig +
                '}';
    }
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(number);
        out.writeInt(id);
        out.writeObject(sig);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        number = in.readInt();
        id = in.readInt();
        sig = (BigInteger) in.readObject();
    }

}
