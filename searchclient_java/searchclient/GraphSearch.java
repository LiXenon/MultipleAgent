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
            char requestBox;

            while (true) {
                //Print a status message every 10000 iteration
                if (++iterations % 100000 == 0) {
                    printSearchStatus(expanded, frontier);
                }

                //Your code here... Don't forget to print out the stats when a solution has been found (see above)
                if (frontier.isEmpty()) {
                    return null;
                }



//                if (iterations >= 30) {
//                    exit(0);
//                }

//                System.err.println("Round:" + iterations);
//                System.err.println(frontier.toString());
//
                s = frontier.pop();

//                if (iterations >= 55 && iterations <= 70) {
//                    System.out.println(s.helps);
//                }
//                int agentAmount = s.agentRows.length;
//                for (int i = 0; i < agentAmount; i++) {
//                    if(s.addHelp(i, s.subgoal.get(i).peek())) {
//                        s.subgoal.get(i).offer(s.helps.get(s.helps.size() - 1).blocker);
//                    }
//                }

//                System.err.println(s.subgoal);
//
//                System.err.print("Completed: ");
//                for (Map.Entry<Character, int[]> entry : s.completedGoals.entrySet()) {
//                    System.err.print(entry.getKey());
//                }

//                Timer timer = new Timer();
//                State finalS = s;
//                TimerTask task = new TimerTask() {
//                    public void run() {
//                        System.err.println(finalS.toString());
//                    }
//                };
//
//                timer.scheduleAtFixedRate(task, 1000, 10000);

//                try {
//                    System.err.println(s.toString());
//                    System.err.println(s.helps.toString());
//                    if (s.jointAction != null) {
//                        for (Action action : s.jointAction) {
//                            System.err.println(action.toString());
//                        }
//                    }
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                List<Help> newHelp = addNewHelp(s, iterations);
                if (newHelp != null) s.helps = newHelp;

                LinkedList<int[]> deadLockedAgents = isAgentDeadlocked(s);
                unlockDeadLockAgents(s, deadLockedAgents);

                isChangeGoal(s,iterations);

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

    private static void unlockDeadLockAgents(State s, LinkedList<int[]> deadLockedAgents) {
        if (deadLockedAgents != null) {
            for (int[] i : deadLockedAgents) {
                int[] unlockPosition = s.blockerAgentGoalCoordinate(i[0], i[1]);
                if (unlockPosition == null) {
                    unlockPosition = s.blockerAgentGoalCoordinate(i[1], i[0]);
                    if (unlockPosition == null) {
                        break;
                    }
                    s.agentConflicts.put(i[1], unlockPosition);
                } else {
                    s.agentConflicts.put(i[0], unlockPosition);
                }
            }
        }
    }

    private static LinkedList<int[]> isAgentDeadlocked(State s) {
        int[] agentRows = s.agentRows;
        int[] agentCols = s.agentCols;
        int length = agentRows.length;
        LinkedList<int[]> deadLockedAgents = new LinkedList<int[]>();


        for (int i = 0; i < length - 1; i++) {
            int x1 = agentRows[i];
            int y1 = agentCols[i];
            for (int j = i + 1; j < length; j++) {
                int x2 = agentRows[j];
                int y2 = agentCols[j];
                int xdiff = Math.abs(x1 - x2);
                int ydiff = Math.abs(y1 - y2);
                if (xdiff + ydiff == 1) {
                    deadLockedAgents.add(new int[]{i, j});
                }
            }
        }

        return deadLockedAgents;
    }

    private static List<Help> addNewHelp(State s, int iterations) {
//        Map<Character, int[]> goalsAndPositon = s.goalsAndPositon;
//        Map<Character, int[]> boxesAndPositon = s.boxesAndPositon;
//        Map<Character, int[]> completedGoals = s.completedGoals;
        ArrayList<LinkedList<Character>> subgoal = s.subgoal;

//        Subgoals subgoals = new Subgoals();
        int[][] grid = s.grid;

        for (int i = 0; i < s.subgoal.size(); i++) {
            LinkedList<Character> agentsubgoal = subgoal.get(i);
//            System.err.println("agentsubgoal: " + agentsubgoal);
            if (!agentsubgoal.isEmpty()) {
                char currentGoal = agentsubgoal.peek();
//                int agentRow = s.agentRows[i];
//                int agentCol = s.agentCols[i];
//                int[] targetPosition = boxesAndPositon.get(currentGoal);
//                int agenttoboxdiff = subgoals.shortest_way(grid, agentRow, agentCol, targetPosition[0], targetPosition[1]) + 1000;
//                System.err.println("Agent " + i + " distance " + agenttoboxdiff);
//                if (agenttoboxdiff == 2) {
                if (s.getHelp(i) == null && currentGoal >= 'A') {
//                    System.err.println(iterations + " New help");
                    Help help = s.addHelp(i, currentGoal);
                    if (help != null) {
                        System.err.println(s.toString());
                    }
                }
//                System.err.println("Current agent: " + i);
//                for (Help h : s.helps) {
//                    System.err.println(h.toString());
//                }
            }
        }
        return s.helps;
    }

    private static void isChangeGoal(State s, int iterations) {
        Map<Character, int[]> goalsAndPositon = s.goalsAndPositon;
        Map<Character, int[]> boxesAndPositon = s.boxesAndPositon;
        Map<Character, int[]> completedGoals = s.completedGoals;
        ArrayList<LinkedList<Character>> subgoal = s.subgoal;

        for (int i = 0; i < s.subgoal.size(); i++) {
            LinkedList<Character> agentsubgoal = subgoal.get(i);
            if (s.agentConflicts.get(i) != null) {
                int[] targetPosition = {s.agentRows[i], s.agentCols[i]};
                int[] goalPosition = s.agentConflicts.get(i);

                if (goalPosition[0] == targetPosition[0] && goalPosition[1] == targetPosition[1]) {
                    s.agentConflicts.remove(i);
                    s.completedAgentConflicts++;
                }
            }
            if (s.getHelp(i) != null) {
                Help help = s.getHelperHelp(i);
                if (help == null) continue;
                char currentGoal = help.blocker;
                int[] targetPosition = boxesAndPositon.get(currentGoal);
                int[] goalPosition = help.blockerGoalCoordinate;

                if (goalPosition[0] == targetPosition[0] && goalPosition[1] == targetPosition[1]) {
                    System.err.println(iterations + " Blocker moved");
                    s.removeHelp(i);

                    s.completedHelps++;
                }
            } else if (!agentsubgoal.isEmpty()) {
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
                    System.err.println(iterations);
                    System.err.println(s.subgoal);
//                    System.err.println(s.toString());
//                    for (LinkedList<Character> as : subgoal) {
//                        System.err.print(as.peek() + " ");
//                    }
                }
            }
        }
//        System.err.println(s.helps.toString());
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