/**
 * Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and
 * the authors indicated in the @author tags
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package bftsmart.statemanagement.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Set;

import bftsmart.consensus.messages.ConsensusMessage;
import bftsmart.consensus.messages.SigShareMessage;
import bftsmart.reconfiguration.ServerViewController;
import bftsmart.reconfiguration.views.View;
import bftsmart.statemanagement.ApplicationState;
import bftsmart.statemanagement.SMMessage;
import bftsmart.statemanagement.StateManager;
import bftsmart.tom.core.DeliveryThread;
import bftsmart.tom.core.TOMLayer;
import bftsmart.tom.leaderchange.LCManager;
import bftsmart.tom.leaderchange.CertifiedDecision;
import bftsmart.tom.util.TOMUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marcel Santos
 *
 */
public abstract class BaseStateManager implements StateManager {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    protected TOMLayer tomLayer;
    protected ServerViewController SVController;
    protected DeliveryThread dt;

    protected HashMap<Integer, ApplicationState> senderStates = null;
    protected HashMap<Integer, View> senderViews = null;
    protected HashMap<Integer, Integer> senderRegencies = null;
    protected HashMap<Integer, Integer> senderLeaders = null;
    protected HashMap<Integer, CertifiedDecision> senderProofs = null;

    protected boolean appStateOnly;
    protected int waitingCID = -1;
    protected int lastCID;
    protected ApplicationState state;

    protected boolean isInitializing = true;
    private HashMap<Integer, Integer> senderCIDs = null;

    public BaseStateManager() {
        senderStates = new HashMap<>();
        senderViews = new HashMap<>();
        senderRegencies = new HashMap<>();
        senderLeaders = new HashMap<>();
        senderProofs = new HashMap<>();
    }

    protected int getReplies() {
        return senderStates.size();
    }

    protected boolean enoughReplies() {
        return senderStates.size() > SVController.getCurrentViewF();
    }

    protected boolean enoughRegencies(int regency) {

        Collection<Integer> regencies = senderRegencies.values();
        int counter = 0;
        for (int r : regencies) {
            if (regency == r) {
                counter++;
            }
        }
        boolean result = counter > SVController.getQuorum();//0;//SVController.getQuorum();
        return result;
    }

    protected boolean enoughLeaders(int leader) {

        Collection<Integer> leaders = senderLeaders.values();
        int counter = 0;
        for (int l : leaders) {
            if (leader == l) {
                counter++;
            }
        }
        boolean result = counter > SVController.getQuorum();//0;//SVController.getQuorum();
        return result;

    }

    protected boolean enoughViews(View view) {
        Collection<View> views = senderViews.values();
        int counter = 0;
        for (View v : views) {
            if (view.equals(v)) {
                counter++;
            }
        }
        boolean result = counter > SVController.getQuorum();//0;//SVController.getQuorum();
        return result;
    }

    // check if the consensus messages are consistent without checking the mac/signatures
    // if it is consistent, it returns the respective consensus ID; otherwise, returns -1
    private int proofIsConsistent(Set<SigShareMessage> proof) {

        int id = -1;
        for (SigShareMessage sm : proof) {
            if (id == -1) id = sm.getNumber();
            if (id != sm.getNumber()) {
                return -1; // they are not consistent, so the proof is invalid
            }
        }
        // if the values are still these, this means the proof is empty, thus is invalid
        return id;
    }

    protected boolean enoughProofs(int cid, LCManager lc) {

        int counter = 0;
        for (CertifiedDecision cDec : senderProofs.values()) {

            if (cDec != null && cid == proofIsConsistent(cDec.getSigsMsgs()) && lc.hasValidProof(cDec)) {
                counter++;
            }

        }
        boolean result = counter > SVController.getQuorum();//0;//SVController.getQuorum();
        return result;
    }

    /**
     * Clear the collections and state hold by this object. Calls clear() in the
     * States, Leaders, regencies and Views collections. Sets the state to
     * null;
     */
    protected void reset() {
        senderStates.clear();
        senderLeaders.clear();
        senderRegencies.clear();
        senderViews.clear();
        senderProofs.clear();
        state = null;
    }

    public Collection<ApplicationState> receivedStates() {
        return senderStates.values();
    }

    @Override
    public void setLastCID(int cid) {
        lastCID = cid;
    }

    @Override
    public int getLastCID() {
        return lastCID;
    }

    @Override
    public void requestAppState(int cid) {
        lastCID = cid + 1;
        waitingCID = cid;
        logger.debug("Updated waitingcid to " + cid);
        appStateOnly = true;
        requestState();
    }

    @Override
    public void analyzeState(int cid) {
        logger.debug("The state transfer protocol is enabled");
        if (waitingCID == -1) {
            logger.debug("I'm not waiting for any state, so I will keep record of this message");
            if (tomLayer.execManager.isDecidable(cid)) {
                System.out.println("I have now more than " + SVController.getCurrentViewF() + " messages for CID " + cid + " which are beyond CID " + lastCID);
                lastCID = cid;
                waitingCID = cid - 1;
                System.out.println("I will be waiting for state messages associated to consensus " + waitingCID);
                requestState();
            }
        }
    }

    @Override
    public abstract void init(TOMLayer tomLayer, DeliveryThread dt);

    @Override
    public boolean isRetrievingState() {
        if (isInitializing) {
            return true;
        }
        return waitingCID > -1;
    }

    @Override
    public void askCurrentConsensusId() {
        int me = SVController.getStaticConf().getProcessId();
        int[] target = SVController.getCurrentViewAcceptors();

        SMMessage currentCID = new StandardSMMessage(me, -1, TOMUtil.SM_ASK_INITIAL, 0, null, null, 0, 0);
        tomLayer.getCommunication().send(target, currentCID);

        target = SVController.getCurrentViewOtherAcceptors();

        while (isInitializing) {
            tomLayer.getCommunication().send(target, currentCID);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Interruption during sleep"+e);
            }
        }
    }

    @Override
    public void currentConsensusIdAsked(int sender) {
        int me = SVController.getStaticConf().getProcessId();
        int lastConsensusId = tomLayer.getLastExec();
        SMMessage currentCIDReply = new StandardSMMessage(me, lastConsensusId, TOMUtil.SM_REPLY_INITIAL, 0, null, null, 0, 0);
        tomLayer.getCommunication().send(new int[]{sender}, currentCIDReply);
    }

    @Override
    public synchronized void currentConsensusIdReceived(SMMessage smsg) {
        if (!isInitializing || waitingCID > -1) {
            return;
        }
        if (senderCIDs == null) {
            senderCIDs = new HashMap<>();
        }
        senderCIDs.put(smsg.getSender(), smsg.getCID());
        /*****************************************************************************************************/
        if (senderCIDs.size() >= SVController.getQuorum()) {//1) {//SVController.getQuorum()) {
            /*****************************************************************************************************/
            HashMap<Integer, Integer> cids = new HashMap<>();
            for (int id : senderCIDs.keySet()) {

                int value = senderCIDs.get(id);

                Integer count = cids.get(value);
                if (count == null) {
                    cids.put(value, 0);
                } else {
                    cids.put(value, count + 1);
                }
            }
            for (int key : cids.keySet()) {
                if (cids.get(key) >= SVController.getQuorum()) {//1SVController.getQuorum()) {
                    if (key == lastCID) {
                        System.out.println("Replica state is up to date");
                        dt.deliverLock();
                        isInitializing = false;
                        tomLayer.setLastExec(key);
                        dt.canDeliver();
                        dt.deliverUnlock();
                        break;
                    } else {
                        //ask for state
                        System.out.println("Requesting state from other replicas");
                        lastCID = key + 1;
                        if (waitingCID == -1) {
                            waitingCID = key;
                            requestState();
                        }
                    }
                }
            }
        }
    }

    protected abstract void requestState();

    @Override
    public abstract void stateTimeout();

    @Override
    public abstract void SMRequestDeliver(SMMessage msg, boolean isBFT);

    @Override
    public abstract void SMReplyDeliver(SMMessage msg, boolean isBFT);

}