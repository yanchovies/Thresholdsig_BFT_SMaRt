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
    public void deliver(ConsensusMessage msg){
        processMessage(msg);
    }
    public void processMessage(ConsensusMessage msg){
        Consensus consensus = tomLayer.execManager.getConsensus(msg.getNumber());
        Epoch epoch = consensus.getEpoch(msg.getEpoch(), controller);
        switch (msg.getType()){
            case MessageFactory.PREPAREVOTE:{
                prepareVoteReceived(msg,epoch);
            }break;
            case MessageFactory.PRECOMMITVOTE:{
                preCommitVoteReceived(msg,epoch);
            }break;
            case MessageFactory.COMMITVOTE:{
                commitVoteReceived(msg,epoch);
            }break;
        }

    }



    /**
     *  handle prepareVote
     */
    public void prepareVoteReceived(ConsensusMessage consensusMessage,Epoch epoch){
        SigShareMessage msg = (SigShareMessage) consensusMessage;
        epoch.setPrepareVotesigs(msg);
        int count = epoch.countPrepareSignatures(value, controller.getCurrentViewN(), this.n);
        if (count >controller.getQuorum()  && !epoch.PreCommit) {//controller.getQuorum()
            SigShareMessage[] sigsArray = epoch.getPrepareVotesigs(count);
            boolean verify = Thresig.verify(value, sigsArray, count, controller.getCurrentViewN(), this.n, this.e);
            if ((verify) ) {
                epoch.setThresholdSigPK(this.n, this.e);
                logger.debug("Validation succeeded!");
                communication.send(this.controller.getCurrentViewAcceptors(), factory.createPreCommit(epoch.getConsensus().getId(),0,value));
                epoch.PreCommit = true;
            } else if (!tomLayer.isChangingLeader()) {
                logger.debug("verification failed!   need to view change...");
            } else {
                logger.debug("enough signatures have been received!");
            }
        }
    }

    /**
     *  handle preCommitVote
     */
    public void preCommitVoteReceived(ConsensusMessage consensusMessage,Epoch epoch){
        SigShareMessage msg = (SigShareMessage) consensusMessage;
        epoch.setPreCommitSigs(msg);
        int count = epoch.countPreCommitSignatures(value, controller.getCurrentViewN(), this.n);
        if (count >controller.getQuorum()&& !epoch.Commit) {
            SigShareMessage[] sigsArray = epoch.getPreCommitSigs(count);
            boolean verify = Thresig.verify(value, sigsArray, count, controller.getCurrentViewN(), this.n, this.e);
            if ((verify)) {
                epoch.setThresholdSigPK(this.n, this.e);
                logger.debug("Validation succeeded!");
                communication.send(this.controller.getCurrentViewAcceptors(), factory.createCommit(epoch.getConsensus().getId(),0,value));
                epoch.Commit = true;
            } else if (!tomLayer.isChangingLeader()) {
                logger.debug("verification failed!   need to view change...");
            } else {
                logger.debug("enough signatures have been received!");
            }
        }
    }

    /**
     * handle commitVote
     */
    public void commitVoteReceived(ConsensusMessage consensusMessage,Epoch epoch){
        SigShareMessage msg = (SigShareMessage) consensusMessage;
        epoch.setCommitSigs(msg);
        int count = epoch.countCommitSignatures(value, controller.getCurrentViewN(), this.n);
        if (count >controller.getQuorum() && !epoch.Decide) {
            SigShareMessage[] sigsArray = epoch.getCommitSigs(count);
            boolean verify = Thresig.verify(value, sigsArray, count, controller.getCurrentViewN(), this.n, this.e);
            if ((verify)) {
                epoch.setThresholdSigPK(this.n, this.e);
                logger.debug("Validation succeeded!");
                communication.send(this.controller.getCurrentViewAcceptors(), factory.createDecide(epoch.getConsensus().getId(),0,value));
                epoch.Decide = true;
            } else if (!tomLayer.isChangingLeader()) {
                logger.debug("verification failed!   need to view change...");
            } else {
                logger.debug("enough signatures have been received!");
            }
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
    public void sendThresholdSigKeys(int execId){
        KeyShareMessage[] keyShareMessages = Thresig.generateKeys(controller.getCurrentViewN() - controller.getCurrentViewF(), controller.getCurrentViewN(), me,execId);
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
