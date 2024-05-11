package searchclient;

import java.util.*;

import static java.lang.System.exit;

public abstract class Heuristic
        implements Comparator<State>
{
    int[][] grid;

    private static final int EMPTY_COST = 1;
    private static final int BLOCK_COST = 1000;

    public Heuristic(State initialState)
    {
        // Here's a chance to pre-process the static parts of the level.
        boolean[][] walls = initialState.walls;
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

        ArrayList<PriorityQueue<Character>> subgoal = s.subgoal;
//        System.err.println("subgoals:");
//        for (char goalname : subgoal) {
//            System.err.print(goalname + " ");
//        }
//        System.err.println();
        Map<Character, int[]> completedGoals = s.completedGoals;

        List<Help> helps = s.helps;
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


        int sumHue = 0;
//        System.err.println("All goals:" + subgoal.toString());
        for (int i = 0; i < s.subgoal.size(); i++) {

//            System.err.println("This is agent: " + i);
//            System.err.println("Agent" + "i goal: "+ subgoal.toString());
            PriorityQueue<Character> agentsubgoal = subgoal.get(i);

            Help help = s.getHelp(i);

            if (help != null) {
                char currentGoal = help.blocker;
                int[] blockerGoalCoordinate = help.blockerGoalCoordinate;
                int[] targetPosition = boxesAndPositon.get(currentGoal);

                int agentRow = s.agentRows[i];
                int agentCol = s.agentCols[i];

                int boxtogoaldiff = subgoals.shortest_way(grid, targetPosition[0], targetPosition[1], blockerGoalCoordinate[0], blockerGoalCoordinate[1]) + 1000;
                int agenttoboxdiff = subgoals.shortest_way(grid, agentRow, agentCol, targetPosition[0], targetPosition[1]) + 1000;

                if (boxtogoaldiff != 1) {
                    int thisHue = boxtogoaldiff + agenttoboxdiff;
//                    System.err.println("Number " + i + " cost: " + thisHue);
                    sumHue += thisHue;
                } else {
                    int thisHue = boxtogoaldiff;
//                    System.err.println("Number " + i + " cost: " + thisHue);
                    sumHue += thisHue;
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

                    if (boxtogoaldiff != 1) {
                        int thisHue = boxtogoaldiff + agenttoboxdiff;
//                    System.err.println("Number " + i + " cost: " + thisHue);
                        sumHue += thisHue;
                    } else {
                        int thisHue = boxtogoaldiff;
//                    System.err.println("Number " + i + " cost: " + thisHue);
                        sumHue += thisHue;
                    }
                } else {
                    int index = currentGoal - '0';
                    targetPosition = new int[]{s.agentRows[index], s.agentCols[index]};
                    int xdiff = Math.abs(targetPosition[0] - goalPosition[0]);
                    int ydiff = Math.abs(targetPosition[1] - goalPosition[1]);

                    while (xdiff == 0 && ydiff == 0) {
                        agentsubgoal.poll();
                        currentGoal = agentsubgoal.peek();
                        index = currentGoal - '0';
                        targetPosition = new int[]{s.agentRows[index], s.agentCols[index]};
                        goalPosition = goalsAndPositon.get(currentGoal);

                        xdiff = Math.abs(targetPosition[0] - goalPosition[0]);
                        ydiff = Math.abs(targetPosition[1] - goalPosition[1]);
                    }
//                    if (goalPosition[0] == targetPosition[0] && goalPosition[1] == targetPosition[1]) {
//                        completedGoals.put(currentGoal, targetPosition);
//                    }
                    sumHue += xdiff + ydiff;
                }
            }
        }
        int punishment = 0;

        if (s.parent != null) {
            State parent = s.parent;
            int[] parentAgentCols = parent.agentCols;
            int[] parentAgentRows = parent.agentRows;
            int[] agentCols = s.agentCols;
            int[] agentRows = s.agentRows;
            for (int i = 0; i < parentAgentCols.length; i++) {
                if (agentRows[i] == parentAgentRows[i] && agentCols[i] == parentAgentCols[i]) {
                    punishment += 100;
                }
            }
        }

        int notInPosition = subgoals.freeze_cell(completedGoals, s);
        int completed = completedGoals.size() * -1000;
        return sumHue + notInPosition + completed + punishment;
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
