package bftsmart.consensus.messages;

import bftsmart.communication.SystemMessage;

import java.io.*;
import java.math.BigInteger;

/**
 * @Author Moonk
 * @Date 2022/6/19
 */
public class KeyShareMessage extends SystemMessage{
    private BigInteger secret;
    private int id;
    private BigInteger verifier;
    private BigInteger groupVerifier;
    private BigInteger e;
    private BigInteger n;
    private int l;
    private boolean isLeaderMessage = false;
    public KeyShareMessage(final int id, final BigInteger secret) {
        this.id = id;
        this.secret = secret;
    }

    public int getL() {
        return l;
    }

    public boolean isLeaderMessage() {
        return isLeaderMessage;
    }

    public KeyShareMessage() {
    }

    public void setVerifiers(final BigInteger verifier,
                             final BigInteger groupVerifier) {
        this.verifier = verifier;
        this.groupVerifier = groupVerifier;
    }
    public void setParameter(final BigInteger e, final BigInteger n,int l,boolean isLeaderMessage) {
        this.e = e;
        this.n = n;
        this.l = l;
        this.isLeaderMessage = isLeaderMessage;
    }

    public BigInteger getE() {
        return e;
    }

    public BigInteger getN() {
        return n;
    }

    public BigInteger getGroupVerifier() {
        return groupVerifier;
    }

    public BigInteger getVerifier() {
        return verifier;
    }

    public int getId() {
        return id;
    }

    public BigInteger getSecret() {
        return secret;
    }

    @Override
    public String toString() {
        return "KeyShareMessage{" +
                "id=" + id +
                '}';
    }
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(secret);
        out.writeInt(id);
        out.writeObject(verifier);
        out.writeObject(groupVerifier);
        out.writeObject(e);
        out.writeObject(n);
        out.writeBoolean(isLeaderMessage);
        out.writeInt(l);

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        secret = (BigInteger) in.readObject();
        id = in.readInt();
        verifier = (BigInteger) in.readObject();
        groupVerifier = (BigInteger) in.readObject();
        e = (BigInteger) in.readObject();
        n = (BigInteger) in.readObject();
        isLeaderMessage = in.readBoolean();
        l = in.readInt();
    }
}
