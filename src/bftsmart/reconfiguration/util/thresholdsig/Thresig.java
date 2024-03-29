package bftsmart.reconfiguration.util.thresholdsig;

import bftsmart.consensus.messages.KeyShareMessage;
import bftsmart.consensus.messages.SigShareMessage;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @Author Moonk
 * @Date 2022/6/19
 */
public class Thresig{
    private static boolean CHECKVERIFIER = true;
//    private BigInteger vk = null;
//    private GroupKey gk;
    protected final static int L1 = 128;
    private final static  SecureRandom random = new SecureRandom();
    /**
     * delta
     */
    static BigInteger factorial(final int l) {
        BigInteger x = BigInteger.valueOf(1L);
        for (int i = 1; i <= l; i++)
            x = x.multiply(BigInteger.valueOf(i));
        return x;
    }

    /**
     * lagrangianInterpolation
     * @return BigInteger
     */
    private static BigInteger lambda(final int ik, final SigShareMessage[] S,
                                     final BigInteger delta) {
        // lambda(id,l) = PI {id!=j, 0<j<=l} (i-j')/(id-j')
        BigInteger value = delta;
        for (final SigShareMessage element : S)
            if (element.getId() != ik)
                value = value.multiply(BigInteger.valueOf(element.getId()));//j' - i???00000000

        for (final SigShareMessage element : S)
            if (element.getId() != ik)
                value = value.divide(BigInteger.valueOf((element.getId() - ik)));//j' - j
        return value;
    }
    public static KeyShareMessage[] generateKeys(final int k, final int l, int from,int execId) {
        KeyShareMessage[] shares = null;
        int keysize = 32;
        BigInteger[] coeff;
        BigInteger pr, qr, p, q, d, e, m, n;
        BigInteger groupSize;
        BigInteger[] secrets = new BigInteger[l];
        BigInteger rand = null;
        final BigInteger delta = factorial(l);
        p = SafePrimeGen.generateStrongPrime(keysize, random);
        q = SafePrimeGen.generateStrongPrime(keysize, random);
        pr = (p.subtract(BigInteger.ONE)).divide(BigInteger.valueOf(2L));//  (p - 1) / 2= pr
        qr = (q.subtract(BigInteger.ONE)).divide(BigInteger.valueOf(2L));// 同理
        m = pr.multiply(qr);
        n = p.multiply(q);
        groupSize = BigInteger.valueOf(l);
        if (groupSize.compareTo(BigInteger.valueOf(0x10001L)) < 0)
            e = BigInteger.valueOf(0x10001L);//F4
        else
            e = new BigInteger(groupSize.bitLength() + 1, 80, random);

        while(m.mod(e).compareTo(BigInteger.ZERO)==0)
            e = new BigInteger(groupSize.bitLength() + 1, 80, random);

        d = e.modInverse(m); //e^-1 % m
        int randbits = n.bitLength() + L1 - m.bitLength();
        coeff = new BigInteger[k - 1];
        coeff[0] = d;
        for (int i = 1; i < coeff.length; i++)
            coeff[i] = (new BigInteger(m.bitLength(), new SecureRandom())).mod(m);

        for (int i = 0; i < l; i++) {
            BigInteger retVal = coeff[k - 2];
            final BigInteger bx = BigInteger.valueOf(i + 1);
            for (int j = k - 3; j >= 0; j--)
                retVal = (retVal.multiply(bx)).add(coeff[j]);
            secrets[i] = retVal;
            rand = (new BigInteger(randbits, random)).multiply(m);
            secrets[i] = secrets[i].add(rand);
        }
        final KeyShareMessage[] s = new KeyShareMessage[l];
        for (int i = 0; i < l; i++)
            s[i] = new KeyShareMessage(i + 1, secrets[i], from,execId);
        shares = s;

        for (final KeyShareMessage element : shares) {
            while (true) {
                rand = new BigInteger(n.bitLength(), random);
                final BigInteger dd = rand.gcd(n);
                if (dd.compareTo(BigInteger.ONE) == 0)
                    break;
            }
            rand = rand.multiply(rand).mod(n);
            element.setVerifiers(rand.modPow(element.getSecret(), n), rand);
            element.setParameter(e,n,l,true);
        }
        return shares;
    }
    public static boolean verifyKey(BigInteger secert, int id){

        return true;
    }

    /**
     * Signature implementation The secret share and shared parameters are required here, and the message b to be signed
     * @return SigShare
     */
    public static SigShareMessage sign(final byte[] b, int id, BigInteger n, BigInteger groupVerifier,
                                       BigInteger verifier, BigInteger secret, int l,int cid,int epoch,int type,int from) {
        BigInteger delta = factorial(l);
        final BigInteger x = (new BigInteger(b)).mod(n);
        MessageDigest md;
        final int randbits = n.bitLength() + 2 * L1;//3

        // r \elt (0, 2^L(n)+3*l1)
        final BigInteger r = (new BigInteger(randbits, random));
        final BigInteger vprime = groupVerifier.modPow(r, n);
        final BigInteger xtilde = x.modPow(BigInteger.valueOf(4L).multiply(delta), n);
        final BigInteger xprime = xtilde.modPow(r, n);
        final BigInteger signVal = BigInteger.valueOf(4L).multiply(delta).multiply(secret);

        BigInteger c = null;
        BigInteger z = null;
        // 收到签名验证      C Z生成
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.reset();
            md.update(groupVerifier.mod(n).toByteArray());
            md.update(xtilde.toByteArray());
            md.update(verifier.mod(n).toByteArray());
            md.update(x.modPow(signVal, n).modPow(BigInteger.valueOf(2L), n).toByteArray());
            md.update(vprime.toByteArray());
            md.update(xprime.toByteArray());
            c = new BigInteger(md.digest()).mod(n);
            z = (c.multiply(secret)).add(r);
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new SigShareMessage(from,cid,epoch,type,id, x.modPow(signVal, n), z, c, verifier, groupVerifier);
    }

    public static boolean verify(final byte[] data, final SigShareMessage[] sigs,
                                 final int k, final int l, final BigInteger n, final BigInteger e) {
        final BigInteger x = (new BigInteger(data)).mod(n);
        final BigInteger delta = factorial(l);


        BigInteger w = BigInteger.valueOf(1L);
        for (int i = 0; i < k; i++)
            w = w.multiply(sigs[i].getSig().modPow(
                    lambda(sigs[i].getId(), sigs, delta), n));
        final BigInteger eprime = delta.multiply(delta).shiftLeft(2);

        w = w.mod(n);
        final BigInteger xeprime = x.modPow(eprime, n);
        final BigInteger we = w.modPow(e, n);
        return (xeprime.compareTo(we) == 0);
    }


    public static boolean checkVerifier(final byte[] data, final SigShareMessage sig,
                                        final int l, final BigInteger n) {
        final BigInteger x = (new BigInteger(data)).mod(n);
        final BigInteger delta = factorial(l);
        if (CHECKVERIFIER) {
            final BigInteger FOUR = BigInteger.valueOf(4l);
            final BigInteger TWO = BigInteger.valueOf(2l);
            final BigInteger xtilde = x.modPow(FOUR.multiply(delta), n);
            try {
                final MessageDigest md = MessageDigest.getInstance("SHA-256");
                final BigInteger v = sig.getGroupVerifier();
                final BigInteger vi = sig.getVerifier();
                md.update(v.toByteArray());
                md.update(xtilde.toByteArray());
                md.update(vi.toByteArray());
                final BigInteger xi = sig.getSig();
                md.update(xi.modPow(TWO, n).toByteArray());
                final BigInteger vz = v.modPow(sig.getZ(), n);
                final BigInteger vinegc = vi.modPow(sig.getC(), n).modInverse(n);
                md.update(vz.multiply(vinegc).mod(n).toByteArray());
                final BigInteger xtildez = xtilde.modPow(sig.getZ(), n);
                final BigInteger xineg2c = xi.modPow(sig.getC(), n).modInverse(n);
                md.update(xineg2c.multiply(xtildez).mod(n).toByteArray());
                final BigInteger result = new BigInteger(md.digest()).mod(n);
                if (!result.equals(sig.getC())) {
                    return false;
                }
            } catch (final java.security.NoSuchAlgorithmException ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }

}
