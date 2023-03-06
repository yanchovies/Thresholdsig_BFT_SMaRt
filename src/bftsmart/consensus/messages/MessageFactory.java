/**
Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and the authors indicated in the @author tags

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package bftsmart.consensus.messages;

import java.math.BigInteger;

/**
 * This class work as a factory of messages used in the paxos protocol.
 */
public class MessageFactory {

    // constants for messages types
    public static final int PREPARE = 44781;
    public static final int COMMIT = 44788;
//    public static final int WRITE    = 44782;
//    public static final int ACCEPT  = 44783;

    private int from; // Replica ID of the process which sent this message

    /**
     * Creates a message factory
     * @param from Replica ID of the process which sent this message
     */
    public MessageFactory(int from) {
        this.from = from;
    }
    public ConsensusMessage createPrepare(int id, int epoch, byte[] value){
        return new ConsensusMessage(PREPARE, id, epoch, from, value);
    }
    public ConsensusMessage createCommit(int id, int epoch, byte[] value){
        return new ConsensusMessage(COMMIT, id, epoch, from, value);
    }

}

