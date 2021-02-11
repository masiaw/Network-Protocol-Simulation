package transport;

public class Receiver extends NetworkHost {
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
    
    // Add any necessary class variables here. They can hold state information for the receiver.
    // Also add any necessary methods (e.g. checksum of a String)
    
    private int seqnum;     // expected sequence number from sender 
    private final int acknum = 0;     // acknowledgement number used to see if checksum is correct
    
    // This is the constructor.  Don't touch!
    public Receiver(int entityName) {
        super(entityName);
    }

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
    
    
    // This method will be called once, before any of your other receiver-side methods are called. 
    // It can be used to do any required initialisation (e.g. of member variables you add to control the state of the receiver).
    @Override
    public void init() {
        seqnum = 0;
        
    }

    // This method will be called whenever a packet sent from the sender(i.e. as a result of a udtSend() being called by the Sender ) arrives at the receiver. 
    // The argument "packet" is the (possibly corrupted) packet sent from the sender.
    @Override
    public void input(Packet packet) {
        
        //checks if the packet is not corrupted and not a duplicate and delivers the data to the application layer
        // and sends the acknowledgement to the sender
         if(!isCorrupted(packet) && packet.getSeqnum() == seqnum) {
            deliverData(packet.getPayload());
            udtSend(new Packet(seqnum, acknum, seqnum + acknum));
            seqnum++;        
         } 
         
         //checks if the packet is a duplicate and resends the acknowledgement with the previous sequence and acknowledgement numbers
         else if(!isCorrupted(packet)&& packet.getSeqnum() < seqnum) {
            seqnum = packet.getSeqnum();
            udtSend(new Packet(seqnum, acknum, seqnum + acknum));
            seqnum++;
         }
    }

}
