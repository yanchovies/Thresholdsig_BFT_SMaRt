package bftsmart.consensus.roles;

import bftsmart.communication.ServerCommunicationSystem;
import bftsmart.consensus.Consensus;
import bftsmart.consensus.Epoch;
import bftsmart.consensus.messages.ConsensusMessage;
import bftsmart.consensus.messages.KeyShareMessage;
import bftsmart.consensus.messages.MessageFactory;
import bftsmart.reconfiguration.ServerViewController;
import bftsmart.consensus.messages.SigShareMessage;
import bftsmart.reconfiguration.util.thresholdsig.Thresig;
import bftsmart.tom.core.ExecutionManager;
import bftsmart.tom.core.TOMLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @Author Moonk
 * @Date 2022/4/21
 */
public class Backup {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private int me;
    private ExecutionManager executionManager;
    private MessageFactory factory; // Factory for PaW messages
    private ServerCommunicationSystem communication; // Replicas comunication system
    private TOMLayer tomLayer; // TOM layer
    private ServerViewController controller;
    private BigInteger secret;
    private int keyId;
    private BigInteger verifier;
    private BigInteger groupVerifier;
    private BigInteger e;
    private BigInteger n;
    private int l;
    private int cid;
    public ReentrantLock lock = new ReentrantLock();

    public Backup(ServerCommunicationSystem communication, MessageFactory factory, ServerViewController controller) {
        this.communication = communication;
        this.factory = factory;
        this.controller = controller;
    }
    public void setExecutionManager(ExecutionManager executionManager) {
        this.executionManager = executionManager;
    }
    public void setTOMLayer(TOMLayer tomLayer) {
        this.tomLayer = tomLayer;
    }
    public void deliver(ConsensusMessage msg){
        processMessage(msg);
    }
    public void processMessage(ConsensusMessage msg){
        Consensus consensus = executionManager.getConsensus(msg.getNumber());
        Epoch epoch = consensus.getEpoch(msg.getEpoch(), controller);
        lock.lock();
        switch (msg.getType()){
            case MessageFactory.PREPARE:{
                PrepareReceived(epoch,msg);
            }break;
            case MessageFactory.COMMIT:{
                CommitReceived(epoch,msg);
            }break;
        }
        lock.unlock();
    }

    private void PrepareReceived(Epoch epoch, ConsensusMessage msg) {
        if(epoch.propValue == null) {
            epoch.propValue = msg.getValue();
            epoch.propValueHash = tomLayer.computeHash(msg.getValue());
            epoch.getConsensus().addWritten(msg.getValue());
            epoch.deserializedPropValue = tomLayer.checkProposedValue(msg.getValue(), true);
            epoch.getConsensus().getDecision().firstMessageProposed = epoch.deserializedPropValue[0];
        }
        cid = msg.getNumber();

        SigShareMessage sign = Thresig.sign(msg.getValue(), keyId, n, groupVerifier, verifier, secret, l,cid);
        communication.send(new int[]{executionManager.getCurrentLeader()}, sign);
    }
    private void CommitReceived(Epoch epoch, ConsensusMessage msg) {
        decide(epoch);
    }
    private void decide(Epoch epoch) {

//        dec.setDecisionEpoch(epoch);
        epoch.getConsensus().decided(epoch, true);
    }

    public void setKeyShare(KeyShareMessage keyShare){
        this.secret = keyShare.getSecret();
        this.keyId = keyShare.getId();
        this.e = keyShare.getE();
        this.groupVerifier = keyShare.getGroupVerifier();
        this.n = keyShare.getN();
        this.verifier = keyShare.getVerifier();
        this.l = keyShare.getL();
//        this.sendReceiveKey();
    }
    public void sendReceiveKey(){
        communication.send(new int[]{executionManager.getCurrentLeader()},new KeyShareMessage(keyId,secret));
    }

    public MessageFactory getFactory() {
        return factory;
    }
}
