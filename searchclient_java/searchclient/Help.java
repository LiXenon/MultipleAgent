package searchclient;

import java.util.Arrays;

public class Help {

    // ID of agent who requests
    public int requesterAgent;
    // ID of the current box of requester agent
    public char requesterBox;
    // ID of agent who helps
    public int helperAgent;
    // ID of the blocker
    public char blocker;
    // The coordinate that blocker should be in to unblock the path
    public int[] blockerStartCoordinate;
    public int[] blockerGoalCoordinate;
    // The coordinate that requester agent should temporarily be to prevent from deadlock
    public int[] requesterGoalCoordinate;
    public Help(int requesterAgent, int helperAgent, char requesterBox, char blocker, int[] blockerGoalCoordinate, int[] requesterGoalCoordinate, int[] blockerStartCoordinate) {
        this.requesterAgent = requesterAgent;
        this.helperAgent = helperAgent;
        this.requesterBox = requesterBox;
        this.blocker = blocker;
        this.blockerGoalCoordinate = blockerGoalCoordinate;
        this.requesterGoalCoordinate = requesterGoalCoordinate;
        this.blockerStartCoordinate = blockerStartCoordinate;
    }

    @Override
    public String toString() {
        return "requesterAgent: " + requesterAgent + ", helperAgent: " + helperAgent + ", requesterBox: " + requesterBox + ", blocker: " + blocker + ", blockerStartCoordinate: " + Arrays.toString(blockerStartCoordinate) + ", blockerGoalCoordinate: " + Arrays.toString(blockerGoalCoordinate)  + ", requesterGoalCoordinate: " + Arrays.toString(requesterGoalCoordinate);
    }
}
