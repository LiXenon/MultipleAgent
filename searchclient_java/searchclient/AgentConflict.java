package searchclient;

import java.util.Arrays;

public class AgentConflict {

    // ID of agent who requests
    public int requesterAgent;
    // ID of agent who helps
    public int blockerAgent;
    // The coordinate that requester agent should temporarily be to prevent from deadlock
    public int[] requesterGoalCoordinate;
    // The coordinate that blocker should be in to unblock the path
    public int[] blockerGoalCoordinate;

    public AgentConflict(int requesterAgent, int blockerAgent, int[] requesterGoalCoordinate, int[] blockerGoalCoordinate) {
        this.requesterAgent = requesterAgent;
        this.blockerAgent = blockerAgent;
        this.requesterGoalCoordinate = requesterGoalCoordinate;
        this.blockerGoalCoordinate = blockerGoalCoordinate;
    }

    @Override
    public String toString() {
        return "AgentConflict{" +
                "requesterAgent=" + requesterAgent +
                ", blockerAgent=" + blockerAgent +
                ", requesterGoalCoordinate=" + Arrays.toString(requesterGoalCoordinate) +
                ", blockerGoalCoordinate=" + Arrays.toString(blockerGoalCoordinate) +
                '}';
    }
}
