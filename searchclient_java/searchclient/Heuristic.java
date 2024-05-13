package searchclient;

import java.util.*;

import static java.lang.System.exit;

public abstract class Heuristic
        implements Comparator<State>
{
    int[][] grid;

    private static final int EMPTY_COST = 1;
    private static final int BLOCK_COST = 10000;
    private static final int GOAL_COST = 1000;
    private static final int BOX_COST = 100;

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
//                } else if (boxes[i][j] != 0) {
//                    this.grid[i][j] = BOX_COST;
//                }
            }
        }
/**
        System.err.println("walls:");
        for (boolean[] i : walls) {
            for (boolean j : i) {
                System.err.print(j + " ");
            }
            System.err.println();
        }

        System.err.println("boxes:");
        for (char[] i : boxes) {
            for (char j : i) {
                System.err.print(j + " ");
            }
            System.err.println();
        }

        System.err.println("goals:");
        for (char[] i : goals) {
            for (char j : i) {
                System.err.print(j + " ");
            }
            System.err.println();
        }
        **/
    }

    public int h(State s) {

        Subgoals subgoals = new Subgoals();

        Map<Character, int[]> goalsAndPositon = s.goalsAndPositon;
        Map<Character, int[]> boxesAndPositon = s.boxesAndPositon;

        ArrayList<LinkedList<Character>> subgoal = s.subgoal;
//        System.err.println("subgoals:");
//        for (char goalname : subgoal) {
//            System.err.print(goalname + " ");
//        }
//        System.err.println();
        Map<Character, int[]> completedGoals = s.completedGoals;
        int completedHelps = s.completedHelps;
        int completedAgentConflicts = s.completedAgentConflicts;

        List<Help> helps = s.helps;

        int[] agentRows = s.agentRows;
        int[] agentCols = s.agentCols;

        boolean[][] walls = s.walls;
        char[][] boxes = s.boxes;
        char[][] goals = s.goals;
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
//                } else if (goals[i][j] != 0) {
//                    this.grid[i][j] = GOAL_COST;
//                }
            }
        }

//        for (int i = 0; i < agentRows.length; i++) {
//            this.grid[agentRows[i]][agentCols[i]] = 5;
//        }
//
        for (Map.Entry<Character, int[]> entry : boxesAndPositon.entrySet()) {
            int[] indices = entry.getValue();
            grid[indices[0]][indices[1]] = BOX_COST;
        }
//        ArrayList<ArrayList<Character>> helpBoxes = new ArrayList<>(subgoal.size());
//        ArrayList<ArrayList<int[]>> helpGoalPositions = new ArrayList<>(subgoal.size());
//
//        for (int i = 0; i < subgoal.size(); i++) {
//            helpBoxes.add(new ArrayList<>());
//            helpGoalPositions.add(new ArrayList<>());
//            Iterator<Help> it = helps.iterator();
//            while (it.hasNext()) {
//                Help item = it.next();
//                if (item.helperAgent == i) {
//                    helpBoxes.get(i).add(item.blocker);
//                    helpGoalPositions.get(i).add(item.blockerGoalCoordinate);
//                }
//            }
//        }


        int completedAgents = 0;
        int sumHue = 0;
        boolean[] holdBox = new boolean[agentRows.length];
//        System.err.println("All goals:" + subgoal.toString());
        for (int i = 0; i < s.subgoal.size(); i++) {

//            System.err.println("This is agent: " + i);
//            System.err.println("Agent" + "i goal: "+ subgoal.toString());
            LinkedList<Character> agentsubgoal = subgoal.get(i);

            Help helperhelp = s.getHelperHelp(i);
            Help requesterhelp = s.getRequesterHelp(i);
            AgentConflict blockerAgent = s.getBlockerAgentConflict(i);
            AgentConflict requesterAgent = s.getRequesterAgentConflict(i);
            if (blockerAgent != null) {
                int agentRow = s.agentRows[i];
                int agentCol = s.agentCols[i];

                int[] unlockPosition = blockerAgent.blockerGoalCoordinate;

                int agenttogoaldiff = subgoals.shortest_way(grid, agentRow, agentCol, unlockPosition[0], unlockPosition[1]) + 1000;

                int thisHue = agenttogoaldiff;
                sumHue += thisHue;
//            } else if (requesterAgent != null) {
//                int agentRow = s.agentRows[i];
//                int agentCol = s.agentCols[i];
//
//                int[] unlockPosition = requesterAgent.requesterGoalCoordinate;
//
//                int agenttogoaldiff = subgoals.shortest_way(grid, agentRow, agentCol, unlockPosition[0], unlockPosition[1]) + 1000;
//
//                int thisHue = agenttogoaldiff;
//                sumHue += thisHue;
            } else if (helperhelp != null) {
                char currentGoal = helperhelp.blocker;
                int[] blockerGoalCoordinate = helperhelp.blockerGoalCoordinate;
                int[] targetPosition = boxesAndPositon.get(currentGoal);

                int agentRow = agentRows[i];
                int agentCol = agentCols[i];

                int boxtogoaldiff = subgoals.shortest_way(grid, targetPosition[0], targetPosition[1], blockerGoalCoordinate[0], blockerGoalCoordinate[1]) + 1000;
                int agenttoboxdiff = subgoals.shortest_way(grid, agentRow, agentCol, targetPosition[0], targetPosition[1]) + 1000;

                if (agenttoboxdiff != 101) {
                    int thisHue = boxtogoaldiff + agenttoboxdiff;
//                    System.err.println("Number " + i + " cost: " + thisHue);
                    sumHue += thisHue;
                } else {
                    int thisHue = boxtogoaldiff;
//                    System.err.println("Number " + i + " cost: " + thisHue);
                    sumHue += thisHue;
                }

            } else if (requesterhelp != null) {
                boolean stage1 = false;
                char currentGoal = requesterhelp.blocker;
                int[] blockertargetPosition = boxesAndPositon.get(currentGoal);
                int[] blockergoalPosition = requesterhelp.blockerGoalCoordinate;
                if (blockergoalPosition[0] == blockertargetPosition[0] && blockergoalPosition[1] == blockertargetPosition[1]) {
                    stage1 = true;
                }

                if (!stage1) {
                    int[] requesterGoalCoordinate = requesterhelp.requesterGoalCoordinate;

                    int agentRow = s.agentRows[i];
                    int agentCol = s.agentCols[i];

                    int agenttogoaldiff = subgoals.shortest_way(grid, agentRow, agentCol, requesterGoalCoordinate[0], requesterGoalCoordinate[1]) + 1000;

                    int thisHue = agenttogoaldiff;
                    sumHue += thisHue;
                } else {

                }

            } else if (!agentsubgoal.isEmpty()) {
                char currentGoal = agentsubgoal.peek();
                int[] targetPosition;
                int[] goalPosition = goalsAndPositon.get(currentGoal);
//        int[][] grid = s.grid;

                if (currentGoal >= 'A' && currentGoal <= 'Z') {
                    targetPosition = boxesAndPositon.get(currentGoal);

                    int agentRow = s.agentRows[i];
                    int agentCol = s.agentCols[i];
//            System.err.println("targetPosition[0]" + targetPosition[0]);
//            System.err.println("agentRow" + agentRow);
//            System.err.println("targetPosition[1]" + targetPosition[1]);
//            System.err.println("agentCol" + agentCol);

                    int boxtogoaldiff = subgoals.shortest_way(grid, targetPosition[0], targetPosition[1], goalPosition[0], goalPosition[1]) + 1000;
                    int agenttoboxdiff = subgoals.shortest_way(grid, agentRow, agentCol, targetPosition[0], targetPosition[1]) + 1000;
//            System.err.println("agenttoboxdiff" + agenttoboxdiff);
//            while (xboxtogoaldiff == 0 && yboxtogoaldiff == 0) {
//                subgoal.poll();
//                currentGoal = subgoal.peek();
//                targetPosition = boxesAndPositon.get(currentGoal);
//                goalPosition = goalsAndPositon.get(currentGoal);
//
//                for (int i = 0; i < s.agentColors.length; i++) {
//                    if (s.agentColors[i] == b) {
//                        index = i;
//                    }
//                }
//                agentRow = s.agentRows[index];
//                agentCol = s.agentCols[index];
//                xboxtogoaldiff = Math.abs(targetPosition[0] - goalPosition[0]);
//                yboxtogoaldiff = Math.abs(targetPosition[1] - goalPosition[1]);
//                xagenttoboxdiff = Math.abs(targetPosition[0] - agentRow);
//                yagenttoboxdiff = Math.abs(targetPosition[1] - agentCol);
//            }
//            System.err.println("xboxtogoaldiff" + xboxtogoaldiff);
//            System.err.println("yboxtogoaldiff" + yboxtogoaldiff);
//            System.err.println("xagenttoboxdiff" + xagenttoboxdiff);
//            System.err.println("yagenttoboxdiff" + yagenttoboxdiff);

//                    if (goalPosition[0] == targetPosition[0] && goalPosition[1] == targetPosition[1]) {
//                        completedGoals.put(currentGoal, targetPosition);
//                    }

                    if (agenttoboxdiff != 101) {
                        int thisHue = boxtogoaldiff + agenttoboxdiff;
//                    System.err.println("Number " + i + " cost: " + thisHue);
                        sumHue += thisHue;
                    } else {
                        int thisHue = boxtogoaldiff;
                        holdBox[i] = true;
//                    System.err.println("Number " + i + " cost: " + thisHue);
                        sumHue += thisHue;
                    }
                } else {
                    int index = currentGoal - '0';
                    targetPosition = new int[]{s.agentRows[index], s.agentCols[index]};
                    goalPosition = goalsAndPositon.get(currentGoal);

                    int agenttogoaldiff = subgoals.shortest_way(grid, goalPosition[0], goalPosition[1], targetPosition[0], targetPosition[1]) + 1000;
//                    while (xdiff == 0 && ydiff == 0) {
//                        agentsubgoal.poll();
//                        currentGoal = agentsubgoal.peek();
//                        index = currentGoal - '0';
//                        targetPosition = new int[]{s.agentRows[index], s.agentCols[index]};
//                        goalPosition = goalsAndPositon.get(currentGoal);
//
//
//                    }
//                    if (goalPosition[0] == targetPosition[0] && goalPosition[1] == targetPosition[1]) {
//                        completedGoals.put(currentGoal, targetPosition);
//                    }
                    sumHue += agenttogoaldiff;
                }
            } else if (agentsubgoal.isEmpty()){
                State parent = s.parent;
                int[] parentAgentRows = parent.agentRows;
                int[] parentAgentCols = parent.agentCols;
                if (agentRows[i] == parentAgentRows[i] && agentCols[i] == parentAgentCols[i]) {
                    sumHue -= 10000;
                }
            }
        }
        int punishment = 0;

        if (s.parent != null) {
            State parent = s.parent;
            int[] parentAgentCols = parent.agentCols;
            int[] parentAgentRows = parent.agentRows;
//            if (parent.parent != null) {
//                State grandparent = parent.parent;
//                int[] grandparentAgentCols = grandparent.agentCols;
//                int[] grandparentAgentRows = grandparent.agentRows;
//                for (int i = 0; i < parentAgentCols.length; i++) {
//                    if (((agentRows[i] == grandparentAgentRows[i] && agentCols[i] == grandparentAgentCols[i]) || (agentRows[i] == parentAgentRows[i] && agentCols[i] == parentAgentCols[i]))
//                            && (subgoal.get(i).size() != 0)) {
//                        punishment += 100;
//                    }
//                }
//            } else {
                for (int i = 0; i < parentAgentCols.length; i++) {
                    if (s.getRequesterAgentConflict(i) == null && s.getRequesterHelp(i) == null) {
                        if ((agentRows[i] == parentAgentRows[i] && agentCols[i] == parentAgentCols[i]) && (!subgoal.get(i).isEmpty())) {
                            punishment += 10000;
                        }
                    }
                }
//            }


        }

        int notInPosition = subgoals.freeze_cell(completedGoals, s);
        int completedgoals = completedGoals.size() * -1000;
        int completedhelps = completedHelps * -1000;
        int completedagentconflicts = completedAgentConflicts * -1000;
//        System.err.print(sumHue + notInPosition + completedgoals + completedhelps + completedagentconflicts + punishment);
        return sumHue + notInPosition + completedgoals + completedhelps + completedagentconflicts + punishment;
//        System.err.println(currentGoal);
//        System.err.println(targetPosition[0] + " " + targetPosition[1]);
//        System.err.println(goalPosition[0] + " " + goalPosition[1]);
    };

    public abstract int f(State s);

    @Override
    public int compare(State s1, State s2)
    {
        return this.f(s1) - this.f(s2);
    }
}

class HeuristicAStar
        extends Heuristic
{
    public HeuristicAStar(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
//        System.err.println("g: " + s.g());
//        System.err.println("h: " + this.h(s));
        return s.g() + this.h(s);
    }

    @Override
    public String toString()
    {
        return "A* evaluation";
    }
}

class HeuristicWeightedAStar
        extends Heuristic
{
    private int w;

    public HeuristicWeightedAStar(State initialState, int w)
    {
        super(initialState);
        this.w = w;
    }

    @Override
    public int f(State s)
    {
        return 10 * s.g() + this.w * this.h(s);
    }

    @Override
    public String toString()
    {
        return String.format("WA*(%d) evaluation", this.w);
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
