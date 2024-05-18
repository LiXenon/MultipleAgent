package searchclient;

import java.util.*;

import static java.lang.System.exit;

public class GraphSearch {

    public static Action[][] search(State initialState, Frontier frontier)
    {

        int iterations = 0;

        frontier.add(initialState);
        HashSet<State> expanded = new HashSet<>();
        State s;

        while (true) {
            //Print a status message every 10000 iteration
            if (++iterations % 100000 == 0) {
                printSearchStatus(expanded, frontier);
            }

            //Your code here... Don't forget to print out the stats when a solution has been found (see above)
            if (frontier.isEmpty()) {
                return null;
            }

            s = frontier.pop();

//                try {
//                    System.err.println(s.toString());
//                    System.err.println(s.helps.toString());
////                    for (Map.Entry<Integer, int[]> entry : s.agentConflicts.entrySet()) {
////                        System.err.println(entry.getKey()+ " " + Arrays.toString(entry.getValue()));
////                    }
////                    if (s.jointAction != null) {
////                        for (Action action : s.jointAction) {
////                            System.err.println(action.toString());
////                        }
////                    }
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

            isChangeGoal(s,iterations);

            if (s.isGoalState()) {
                printSearchStatus(expanded, frontier);
                return s.extractPlan();
            }

            expanded.add(s);
            ArrayList<State> childState = s.getExpandedStates();
            for (State t : childState) {
                if ((!frontier.contains(t)) && (!expanded.contains(t))) {
                    frontier.add(t);
                }
            }

        }

    }

    private static void isChangeGoal(State s, int iterations) {
        Map<Character, int[]> goalsAndPositon = s.goalsAndPositon;
        Map<Character, int[]> boxesAndPositon = s.boxesAndPositon;
        Map<Character, int[]> completedGoals = s.completedGoals;
        ArrayList<LinkedList<Character>> subgoal = s.subgoal;

        for (int i = 0; i < s.subgoal.size(); i++) {
            LinkedList<Character> agentsubgoal = subgoal.get(i);

            if (!agentsubgoal.isEmpty()) {
                char currentGoal = agentsubgoal.peek();
                int[] targetPosition;
                if (currentGoal >= 'A') {
                    targetPosition = boxesAndPositon.get(currentGoal);
                } else {
                    targetPosition = new int[]{s.agentRows[i], s.agentCols[i]};
                }
                int[] goalPosition = goalsAndPositon.get(currentGoal);

                if (goalPosition[0] == targetPosition[0] && goalPosition[1] == targetPosition[1]) {
                    completedGoals.put(currentGoal, targetPosition);
                    agentsubgoal.poll();
                }
            }
        }
    }

    private static long startTime = System.nanoTime();

    private static void printSearchStatus(HashSet<State> expanded, Frontier frontier)
    {
        String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
        double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
        System.err.format(statusTemplate, expanded.size(), frontier.size(), expanded.size() + frontier.size(),
                elapsedTime, Memory.stringRep());
    }
}