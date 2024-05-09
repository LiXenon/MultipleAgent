package searchclient;

public class Help {

    // ID of agent who requests
    public int requesterAgent;
    // ID of the current box of requester agent
    public int requesterBox;
    // ID of agent who helps
    public int helperAgent;
    // ID of the blocker
    public char blocker;
    // The coordinate that blocker should be in to unblock the path
    public int[] blockerGoalCoordinate;

    public Help(int requesterAgent, int helperAgent, int requesterBox, char blocker, int[] blockerGoalCoordinate) {
        this.requesterAgent = requesterAgent;
        this.helperAgent = helperAgent;
        this.requesterBox = requesterBox;
        this.blocker = blocker;
        this.blockerGoalCoordinate = blockerGoalCoordinate;
    }
}
