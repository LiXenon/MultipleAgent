package searchclient;

import java.util.*;

public abstract class Heuristic
        implements Comparator<State>
{
    int[][] grid;

    private static final int EMPTY_COST = 1;
    private static final int BLOCK_COST = 10000;

    public Heuristic(State initialState)
    {
        // Here's a chance to pre-process the static parts of the level.
        boolean[][] walls = initialState.walls;
        char[][] boxes = initialState.boxes;
        int rows = walls.length, cols = walls[0].length;
        this.grid = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            Arrays.fill(this.grid[i], EMPTY_COST);
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (walls[i][j] == true) {
                    this.grid[i][j] = BLOCK_COST;
                }
            }
        }
    }

    public int h(State s) {

        Subgoals subgoals = new Subgoals();

        Map<Character, int[]> goalsAndPositon = s.goalsAndPositon;
        Map<Character, int[]> boxesAndPositon = s.boxesAndPositon;

        ArrayList<LinkedList<Character>> subgoal = s.subgoal;
        Map<Character, int[]> completedGoals = s.completedGoals;
        int completedHelps = s.completedHelps;
        int completedAgentConflicts = s.completedAgentConflicts;

        int[] agentRows = s.agentRows;
        int[] agentCols = s.agentCols;
        char[][] boxes = s.boxes;
        char[][] goals = s.goals;

        int sumHue = 0;
        boolean[] holdBox = new boolean[agentRows.length];

        for (int i = 0; i < s.subgoal.size(); i++) {

            LinkedList<Character> agentsubgoal = subgoal.get(i);

            if (!agentsubgoal.isEmpty()) {
                char currentGoal = agentsubgoal.peek();
                int[] targetPosition;
                int[] goalPosition = goalsAndPositon.get(currentGoal);

                if (currentGoal >= 'A' && currentGoal <= 'Z') {
                    targetPosition = boxesAndPositon.get(currentGoal);

                    int agentRow = s.agentRows[i];
                    int agentCol = s.agentCols[i];

                    int boxtogoaldiff = subgoals.shortest_way(grid, targetPosition[0], targetPosition[1], goalPosition[0], goalPosition[1]) + 1000;
                    int agenttoboxdiff = subgoals.shortest_way(grid, agentRow, agentCol, targetPosition[0], targetPosition[1]) + 1000;

                    if (agenttoboxdiff != 2) {
                        int thisHue = boxtogoaldiff + agenttoboxdiff;
                        sumHue += thisHue;
                    } else {
                        int thisHue = boxtogoaldiff;
                        holdBox[i] = true;
                        sumHue += thisHue;
                    }
                } else {
                    int index = currentGoal - '0';
                    targetPosition = new int[]{s.agentRows[index], s.agentCols[index]};
                    goalPosition = goalsAndPositon.get(currentGoal);

                    int agenttogoaldiff = subgoals.shortest_way(grid, goalPosition[0], goalPosition[1], targetPosition[0], targetPosition[1]) + 1000;

                    sumHue += agenttogoaldiff;
                }
            }
            else if (agentsubgoal.isEmpty() && s.parent != null){
                State parent = s.parent;
                if (goals[agentRows[i]][agentCols[i]] - '0' == i) {
                    continue;
                }
                boolean isBlockinWay = true;
                LinkedList<int[]> positons = new LinkedList<int[]>();
                for (int j = agentRows[i] - 1; j <= agentRows[i] + 1; j++) {
                    for (int k = agentCols[i] - 1; k <= agentCols[i] + 1; k++) {
                        if (j != agentRows[i] || k != agentCols[i]) {
                            if (grid[j][k] == 1 && goals[j][k] == 0) {
                                positons.add(new int[] {j, k});
                            }
                        }
                    }
                }
                int count = 0;
                for (int[] j : positons) {
                    for (int[] k : positons) {
                        if (Math.abs(k[0] - j[0]) + Math.abs(k[1] - j[1]) == 1) {
                            count++;
                        }
                    }
                }
                if ((count / 2) == positons.size() - 1) {
                    isBlockinWay = false;
                }
                if (isBlockinWay) {
                    continue;
                }
                int[] parentAgentRows = parent.agentRows;
                int[] parentAgentCols = parent.agentCols;
                if ((agentRows[i] == parentAgentRows[i] && agentCols[i] == parentAgentCols[i]) && goals[agentRows[i]][agentCols[i]] == 0) {
                    sumHue -= 10000;
                }
            }
        }
        int punishment = 0;

        if (s.parent != null) {
            State parent = s.parent;
            int[] parentAgentCols = parent.agentCols;
            int[] parentAgentRows = parent.agentRows;
                for (int i = 0; i < parentAgentCols.length; i++) {
                    if ((agentRows[i] == parentAgentRows[i] && agentCols[i] == parentAgentCols[i]) && (subgoal.get(i).size() != 0)) {
                        punishment += 101;
                }
            }

        }

        int notInPosition = subgoals.freeze_cell(completedGoals, s);
        int completedgoals = completedGoals.size() * -1000;
        int completedhelps = completedHelps * -1000;
        int completedagentconflicts = completedAgentConflicts * -1000;
        return sumHue + notInPosition + completedgoals + completedhelps + completedagentconflicts + punishment;
    }

    public abstract int f(State s);

    @Override
    public int compare(State s1, State s2)
    {
        return this.f(s1) - this.f(s2);
    }
}

class HeuristicGreedy
        extends Heuristic
{
    public HeuristicGreedy(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        return this.h(s);
    }

    @Override
    public String toString()
    {
        return "greedy evaluation";
    }
}
