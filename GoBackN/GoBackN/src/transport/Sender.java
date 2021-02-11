package transport;

import java.util.ArrayList;

public class Sender extends NetworkHost {

    /*
     * Predefined Constant (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and Packet payload
     *
     *
     * Predefined Member Methods:
     *
     *  void startTimer(double increment):
     *       Starts a timer, which will expire in "increment" time units, causing the interrupt handler to be called.  You should only call this in the Sender class.
     *  void stopTimer():
     *       Stops the timer. You should only call this in the Sender class.
     *  void udtSend(Packet p)
     *       Sends the packet "p" into the network to arrive at other host
     *  void deliverData(String dataSent)
     *       Passes "dataSent" up to app layer. You should only call this in the Receiver class.
     *
     *  Predefined Classes:
     *
     *  NetworkSimulator: Implements the core functionality of the simulator
     *
     *  double getTime()
     *       Returns the current time in the simulator. Might be useful for debugging. Call it as follows: NetworkSimulator.getInstance().getTime()
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for debugging. Call it as follows: NetworkSimulator.getInstance().printEventList()
     *
     *  Message: Used to encapsulate a message coming from the application layer
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      void setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *      String getData():
     *          returns the data contained in the message
     *
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet, which is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload):
     *          creates a new Packet with a sequence field of "seq", an ack field of "ack", a checksum field of "check", and a payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an ack field of "ack", a checksum field of "check", and an empty payload
     *    Methods:
     *      void setSeqnum(int seqnum)
     *          sets the Packet's sequence field to seqnum
     *      void setAcknum(int acknum)
     *          sets the Packet's ack field to acknum
     *      void setChecksum(int checksum)
     *          sets the Packet's checksum to checksum
     *      void setPayload(String payload) 
     *          sets the Packet's payload to payload
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      String getPayload()
     *          returns the Packet's payload
     *
     */
    
    // Add any necessary class variables here. They can hold state information for the sender. 
    // Also add any necessary methods (e.g. checksum of a String)
    
    
    // This is the constructor.  Don't touch!
    public Sender(int entityName) {
        super(entityName);
    }
    
    private int seqnum;                 // current sequence number added to the packet
    private final int acknum = 0;       // expected acknowledgement number from receiver
    private final int window = 8;       // max number of sent, unpacked packets
    private int base;                   // sequence number of the earliest unacknowledged packet
    private ArrayList<Packet> buffer;   // list of sent but not yet acknowledged packets
    
    // This method will do a checksum, which returns character-by-character sum of the payload field of the packet
    // which can be achieved by converting each character to the number it is associated with by ASCII
    public int checksum(String payload){
        int checksum = 0;
        for (int i = 0; i < payload.length(); i++ ){
            int a = payload.charAt(i);
            checksum += a;
        }
        return checksum;
    }

    // This method will check whether the packet sent is corrupted
    // returns true if corrupted otherwise false
    public boolean isCorrupted(Packet p){
        int checksum = p.getSeqnum() + p.getAcknum() + checksum(p.getPayload());
        return checksum != p.getChecksum();
    }    
    
    // This method will be called once, before any of your other sender-side methods are called. 
    // It can be used to do any required initialisation (e.g. of member variables you add to control the state of the sender).
    @Override
    public void init() {
        seqnum = 0;
        base = 0;
        buffer = new ArrayList<Packet>();
    }
    
    // This method will be called whenever the app layer at the sender has a message to send.  
    // The job of your protocol is to ensure that the data in such a message is delivered in-order, and correctly, to the receiving application layer.
    @Override
    public void output(Message message) {
        
        //ensures that only the number of packets within the size of the window is transmitted
        if (seqnum < base + window) {
            
            //sets up data for packet and creates new packet
            int seq = seqnum;
            String payload = message.getData();
            int check = seq + acknum + checksum(payload);
            Packet p = new Packet(seq, acknum, check, payload);
            
            //adds a packet to the buffer in case it has to be retransmitted
            buffer.add(p);
        
            //sends the packet to the receiver
            udtSend(p);
            
            //starts the timer if none is running yet
            if (base == seqnum) {
                startTimer(40);
            }
            
            //increments the sequence number of the next packet
            seqnum++;
        }
    }
    
    
    // This method will be called whenever a packet sent from the receiver (i.e. as a result of a udtSend() being done by a receiver procedure) arrives at the sender.  
    // "packet" is the (possibly corrupted) packet sent from the receiver.
    @Override
    public void input(Packet packet) {
        //checks if the akcnowledgement was corrupted and if it is not then terminates the transmission
        if(!isCorrupted(packet)) {
            
            //increments the base
            base = packet.getSeqnum() + 1;
            
            stopTimer(); //Timer stops if the packet is the last available packet or if the time needs to be restarted
            // if the packet is not the last packet available in the window timer restarted
            if (base != seqnum) {
                startTimer(40);
            }
        }
    }
    
    
    // This method will be called when the senders' timer expires (thus generating a timer interrupt).  
    @Override
    public void timerInterrupt() {
        startTimer(40);
        //re-transmission if needed
        for (int i = base; i < seqnum; i++) {
            udtSend(buffer.get(i));
        }
    }
}
