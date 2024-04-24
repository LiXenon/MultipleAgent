package searchclient;

import java.util.*;

import static java.lang.System.exit;

public class GraphSearch {

    public static Action[][] search(State initialState, Frontier frontier)
    {
        boolean outputFixedSolution = false;

        if (outputFixedSolution) {
            //Part 1:
            //The agents will perform the sequence of actions returned by this method.
            //Try to solve a few levels by hand, enter the found solutions below, and run them:

            return new Action[][] {
                {Action.MoveS},
                {Action.MoveE},
                {Action.MoveE},
                {Action.MoveS},
            };
        } else {
            //Part 2:
            //Now try to implement the Graph-Search algorithm from R&N figure 3.7
            //In the case of "failure to find a solution" you should return null.
            //Some useful methods on the state class which you will need to use are:
            //state.isGoalState() - Returns true if the state is a goal state.
            //state.extractPlan() - Returns the Array of actions used to reach this state.
            //state.getExpandedStates() - Returns an ArrayList<State> containing the states reachable from the current state.
            //You should also take a look at Frontier.java to see which methods the Frontier interface exposes
            //
            //printSearchStatus(expanded, frontier): As you can see below, the code will print out status
            //(#expanded states, size of the frontier, #generated states, total time used) for every 10000th node generated.
            //You should also make sure to print out these stats when a solution has been found, so you can keep 
            //track of the exact total number of states generated!!


            int iterations = 0;

            frontier.add(initialState);
            HashSet<State> expanded = new HashSet<>();
            State s;

            while (true) {

                //Print a status message every 10000 iteration
                if (++iterations % 100000 == 0) {
                    printSearchStatus(expanded, frontier);
                }
//                if (iterations == 40) {
//                    exit(0);
//                }

                //Your code here... Don't forget to print out the stats when a solution has been found (see above)
                if (frontier.isEmpty()) {
                    return null;
                }

//                System.err.println("Round:" + iterations);
//                System.err.println(frontier.toString());
//
                s = frontier.pop();
//
//                System.err.print("Completed: ");
//                for (Map.Entry<Character, int[]> entry : s.completedGoals.entrySet()) {
//                    System.err.print(entry.getKey());
//                }

                isChangeGoal(s);

//                System.err.println(s.toString());

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
    }

    private static void isChangeGoal(State s) {
        Map<Character, int[]> goalsAndPositon = s.goalsAndPositon;
        Map<Character, int[]> boxesAndPositon = s.boxesAndPositon;
        Map<Character, int[]> completedGoals = s.completedGoals;
        PriorityQueue<Character> subgoal = s.subgoal;

        char currentGoal = subgoal.peek();
        int[] targetPosition = boxesAndPositon.get(currentGoal);
        int[] goalPosition = goalsAndPositon.get(currentGoal);

        if (goalPosition[0] == targetPosition[0] && goalPosition[1] == targetPosition[1]) {
            completedGoals.put(currentGoal, targetPosition);
            subgoal.poll();
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
