package searchclient;

import java.util.*;

import static java.lang.System.exit;

public abstract class Heuristic
        implements Comparator<State>
{

    public Heuristic(State initialState)
    {
        // Here's a chance to pre-process the static parts of the level.
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

        PriorityQueue<Character> subgoal = s.subgoal;
//        System.err.println("subgoals:");
//        for (char goalname : subgoal) {
//            System.err.print(goalname + " ");
//        }
//        System.err.println();
        Map<Character, int[]> completedGoals = s.completedGoals;

        char currentGoal = subgoal.peek();
        int[] targetPosition;
        int[] goalPosition = goalsAndPositon.get(currentGoal);
        int[][] grid = s.grid;

        if (currentGoal >= 'A' && currentGoal <= 'Z') {
            targetPosition = boxesAndPositon.get(currentGoal);
            Color b = s.boxColors[currentGoal - 'A'];
            int index = 0;
            for (int i = 0; i < s.agentColors.length; i++) {
                if (s.agentColors[i] == b) {
                    index = i;
                    break;
                }
            }
            int agentRow = s.agentRows[index];
            int agentCol = s.agentCols[index];
//            System.err.println("targetPosition[0]" + targetPosition[0]);
//            System.err.println("agentRow" + agentRow);
//            System.err.println("targetPosition[1]" + targetPosition[1]);
//            System.err.println("agentCol" + agentCol);

            int boxtogoaldiff = subgoals.shortest_way(grid, targetPosition[0], targetPosition[1], goalPosition[0], goalPosition[1]);
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
            int notInPosition = subgoals.freeze_cell(completedGoals, s);
            if (goalPosition[0] == targetPosition[0] && goalPosition[1] == targetPosition[1]) {
                completedGoals.put(currentGoal, targetPosition);
            }
            int completed = completedGoals.size() * -1000;
            if (boxtogoaldiff != 1) {
                return boxtogoaldiff + agenttoboxdiff + notInPosition + completed;
            } else {
                return boxtogoaldiff + notInPosition + completed;
            }
        } else {
            int index = currentGoal - '0';
            targetPosition = new int[]{s.agentRows[index], s.agentCols[index]};
            int xdiff = Math.abs(targetPosition[0] - goalPosition[0]);
            int ydiff = Math.abs(targetPosition[1] - goalPosition[1]);

            while (xdiff == 0 && ydiff == 0) {
                subgoal.poll();
                currentGoal = subgoal.peek();
                index = currentGoal - '0';
                targetPosition = new int[]{s.agentRows[index], s.agentCols[index]};
                goalPosition = goalsAndPositon.get(currentGoal);

                xdiff = Math.abs(targetPosition[0] - goalPosition[0]);
                ydiff = Math.abs(targetPosition[1] - goalPosition[1]);
            }
            int notInPosition = subgoals.freeze_cell(completedGoals, s);
            if (goalPosition[0] == targetPosition[0] && goalPosition[1] == targetPosition[1]) {
                completedGoals.put(currentGoal, targetPosition);
            }
            int completed = completedGoals.size() * -1000;
            return xdiff + ydiff + notInPosition + completed;
        }

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
