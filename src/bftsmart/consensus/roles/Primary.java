package bftsmart.consensus.roles;

import bftsmart.communication.ServerCommunicationSystem;
import bftsmart.communication.SystemMessage;
import bftsmart.consensus.Consensus;
import bftsmart.consensus.Decision;
import bftsmart.consensus.Epoch;
import bftsmart.consensus.messages.ConsensusMessage;
import bftsmart.consensus.messages.KeyShareMessage;
import bftsmart.consensus.messages.MessageFactory;
import bftsmart.consensus.messages.SigShareMessage;
import bftsmart.reconfiguration.ServerViewController;
import bftsmart.reconfiguration.util.thresholdsig.Thresig;
import bftsmart.tom.core.ExecutionManager;
import bftsmart.tom.core.TOMLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Moonk
 * @Date 2022/4/21
 */
public class Primary {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private int me; // This replica ID
    private ExecutionManager executionManager; // Execution manager of consensus's executions
    private MessageFactory factory; // Factory for PaW messages
    private ServerCommunicationSystem communication; // Replicas comunication system
    private TOMLayer tomLayer; // TOM layer
    private ServerViewController controller;
    //private Cipher cipher;
    private byte[] value = null;
    private BigInteger n;
    private BigInteger e;
    private int cid;
    private boolean alwaysCommit = false;
    private List<SigShareMessage> sigs = new ArrayList<>();
    private Epoch epoch;
    public Primary(ServerCommunicationSystem communication,MessageFactory factory,ServerViewController controller) {
        this.communication = communication;
        this.factory = factory;
        this.controller = controller;
    }
    public void deliver(SigShareMessage msg){
        processMessage(msg);
    }
    public void processMessage(SigShareMessage msg){
        if(cid == msg.getNumber()){
            sigs.add(msg);
            if(sigs.size()>controller.getQuorum()){//controller.getCurrentViewN()){//
                SigShareMessage[] sigsArray = sigs.toArray(new SigShareMessage[0]);
                boolean verify = Thresig.verify(value, sigsArray, sigs.size(), controller.getCurrentViewN(), this.n, this.e);
                if(verify&&!alwaysCommit){
                    logger.debug("Validation succeeded!");
                    communication.send(this.controller.getCurrentViewAcceptors(), factory.createCommit(epoch.getConsensus().getId(),0,value));
                    alwaysCommit = true;
                }else if(!verify){
                    logger.debug("verification failed!!!    view change...");
                } else {
                    logger.debug("enough signatures have been received!!!");
                }
            }
        }else {
            logger.debug("out of date message!!!");
        }

    }
    public void setTOMLayer(TOMLayer tomLayer){
        this.tomLayer = tomLayer;
    }

    /**
     * start Consensus
     * @param cid
     * @param value
     */
    public void startConsensus(int cid, byte[] value, Consensus consensus) {
        this.cid = cid;
        this.value = value;
        ConsensusMessage msg = factory.createPrepare(cid, 0, value);
        epoch = consensus.getEpoch(msg.getEpoch(), controller);
        reset();
        communication.send(this.controller.getCurrentViewAcceptors(), msg);
    }
    public void sendThresholdSigKeys(){
        KeyShareMessage[] keyShareMessages = Thresig.generateKeys(controller.getCurrentViewN() - controller.getCurrentViewF(), controller.getCurrentViewN());
        this.n = keyShareMessages[0].getN();
        this.e = keyShareMessages[0].getE();
        for(int i=0;i<controller.getCurrentViewN();i++){
            tomLayer.getCommunication().send(new int[]{i},keyShareMessages[i]);
        }
    }
    public void setEandN(BigInteger e,BigInteger n){
        this.e = e;
        this.n = n;
    }
    public void verifyKey(KeyShareMessage keyShare){
        BigInteger secert = keyShare.getSecret();
        int id = keyShare.getId();
        boolean verifyKey = Thresig.verifyKey(secert, id);
    }

    public void reset(){
        sigs.clear();
        alwaysCommit = false;
    }
}
