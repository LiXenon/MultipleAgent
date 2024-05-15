package searchclient;

import java.util.*;

import searchclient.Node;
public class Subgoals {

    class GoalCost implements Comparable<GoalCost> {

        char goalName;
        int cost;

        public GoalCost(char goalName, int cost) {
            this.goalName = goalName;
            this.cost = cost;
        }

        public int compareTo(GoalCost gc) {
            return gc.cost - this.cost;
        }

        @Override
        public String toString() {
            return "GoalCost{" +
                    "goalName=" + goalName +
                    ", cost=" + cost +
                    '}';
        }
    }

    public ArrayList<LinkedList<String>> sort_priority(int[][] grid, boolean[][] walls, char[][] goals, int[] agentRows, int[] agentCols, Map<Character, int[]> goalsAndPosition, Color[] agentColors, Color[] boxColors) {
//        int rows = walls.length, cols = walls[0].length;
        int agentAmount = agentRows.length;
        int agentRow = agentRows[0];
        int agentCol = agentCols[0];

//        for (int i = 0; i < rows; i++) {
//            for (int j = 0; j < cols; j++) {
//                System.err.print(grid[i][j] + " ");
//            }
//            System.err.println();
//        }
//
//        System.err.println(agentRow + "  " + agentCol);
        ArrayList<LinkedList<GoalCost>> goalCosts = new ArrayList<LinkedList<GoalCost>>(agentAmount);
        for (int i = 0; i < agentRows.length; i++) {
            goalCosts.add(new LinkedList<GoalCost>());
        }

//        int count = 0;

        char goalName;
        int goalIndex;

        for (Map.Entry<Character, int[]> entry : goalsAndPosition.entrySet()) {
            int[] indices = entry.getValue();
            goalName = entry.getKey();
            if (goalName >= 'A') {
                goalIndex = goalName - 'A';

                Color b = boxColors[goalIndex];
                int index = 0;
                for (int i = 0; i < agentAmount; i++) {
                    if (agentColors[i] == b) {
                        index = i;
                        break;
                    }
                }
                int thisCost = shortest_way(grid, indices[0], indices[1], agentRow, agentCol);
                goalCosts.get(index).add(new GoalCost(goalName, thisCost));
            } else {
                goalIndex = goalName - '0';
                int thisCost = -1;
                goalCosts.get(goalIndex).add(new GoalCost(goalName, thisCost));
            }


//            System.err.print(goalName);
//            System.err.println(thisCost);
//            count++;
//            System.err.print(entry.getKey() + "  " + indices[0] + " " + indices[1] + " ");
//            System.err.println(shortest_way(grid, indices[0], indices[1], agentRow, agentCol));
        }
        for (LinkedList<GoalCost> gcs : goalCosts) {
            Collections.sort(gcs);
        }

        ArrayList<LinkedList<String>> subgoals = new ArrayList<LinkedList<String>>(agentAmount);

        for (int i = 0; i < agentRows.length; i++) {
            subgoals.add(new LinkedList<String>());
            for (GoalCost gc : goalCosts.get(i)) {
                subgoals.get(i).add(gc.goalName);
            }
        }
//        System.err.print(goalCosts.toString());

//        ArrayList<LinkedList<Character>> subgoal = new ArrayList<LinkedList<Character>>(agentAmount);
//        for (int i = 0; i < agentAmount; i++) {
//            LinkedList<GoalCost> agentsubgoal = goalCosts.get(i);
//            for (GoalCost gc : agentsubgoal) {
//
//            }
////            int finalI = i;
////            PriorityQueue<Character> agentsubgoal = new PriorityQueue<>(Comparator.comparingInt(c -> -getIntValue(c, goal.get(finalI), cost.get(finalI))));
////            for (char c : goal.get(finalI)) {
////                agentsubgoal.offer(c);
////                System.err.print(agentsubgoal);
////            }
//            subgoal.add(agentsubgoal);
//        }

//        for (char goalname : subgoal) {
//            System.err.print(goalname + " ");
//        }
//        System.err.println();

//        for (int[] i : grid) {
//            for (char j : i) {
//                if () {
//                    subgoal.addLast(j);
//                }
//            }
//        }
//        System.err.println("subgoals:");
//        for (char i : subgoal) {
//            System.err.print(i + " ");
//
//        }
        return subgoals;
    }

    private static int getIntValue(char c, ArrayList<Character> chars, ArrayList<Integer> ints) {
        for (int i = 0; i < chars.size(); i++) {
            if (chars.get(i) == c) {
                System.err.print(chars.get(i));
                System.err.println(ints.get(i));
                return ints.get(i);
            }
        }
        return Integer.MIN_VALUE;
    }

    public int shortest_way(int[][] grid, int startX, int startY, int goalX, int goalY) {
        int rows = grid.length;
        int cols = grid[0].length;
        int[][] distance = new int[rows][cols];
        boolean[][] visited = new boolean[rows][cols];
        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> a.cost - b.cost);

        // Initialize distances to infinity
        for (int i = 0; i < rows; i++) {
            Arrays.fill(distance[i], Integer.MAX_VALUE);
        }

        // Initialize start node
        distance[startX][startY] = grid[startX][startY];
        pq.offer(new Node(startX, startY, grid[startX][startY]));

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!pq.isEmpty()) {
            Node curr = pq.poll();
            int x = curr.x;
            int y = curr.y;
            int cost = curr.cost;

            if (visited[x][y]) continue;
            visited[x][y] = true;

            if (x == goalX && y == goalY) {
                return distance[x][y] - 1000;
            }

            // Explore neighbors
            for (int[] dir : directions) {
                int newX = x + dir[0];
                int newY = y + dir[1];

                if (newX >= 0 && newX < rows && newY >= 0 && newY < cols && !visited[newX][newY]) {
                    int newCost = cost + grid[newX][newY];
                    if (newCost < distance[newX][newY]) {
                        distance[newX][newY] = newCost;
                        pq.offer(new Node(newX, newY, newCost));
                    }
                }
            }
        }

        // No path found
        return -1;
    }

    public int freeze_cell(Map<String, int[]> completedGoals, State s) {
        int inPosition = 0;
        char[][] boxes = s.boxes;
        int[] agentRows = s.agentRows;
        int[] agentCols = s.agentCols;
        for (Map.Entry<String, int[]> entry : completedGoals.entrySet()) {
            char firstCharOfBoxName = entry.getKey().charAt(0);
//            System.err.println("Compeleted Goals:" + boxName);
            int[] indices = entry.getValue();
            if ((firstCharOfBoxName >= 'A' && boxes[indices[0]][indices[1]] != firstCharOfBoxName) || (firstCharOfBoxName < 'A' && (agentRows[firstCharOfBoxName - '0'] != indices[0] || agentCols[firstCharOfBoxName - '0'] != indices[1]))) {
                inPosition += 1000;
            }
        }
        return inPosition;
    }
}
