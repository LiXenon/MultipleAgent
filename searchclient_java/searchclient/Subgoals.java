package searchclient;

import java.util.*;

import searchclient.Node;
public class Subgoals {

    public PriorityQueue<Character> sort_priority(int[][] grid, boolean[][] walls, char[][] goals, int[] agentRows, int[] agentCols, Map<Character, int[]> goalsAndPosition) {
        int rows = walls.length, cols = walls[0].length;
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
        char[] goal = new char[goalsAndPosition.size()];
        int[] cost = new int[goalsAndPosition.size()];
        int count = 0;

        for (Map.Entry<Character, int[]> entry : goalsAndPosition.entrySet()) {
            int[] indices = entry.getValue();
            goal[count] = entry.getKey();
            cost[count] = shortest_way(grid, indices[0], indices[1], agentRow, agentCol);
//            System.err.print(goal[count]);
//            System.err.println(cost[count]);
            count++;
//            System.err.print(entry.getKey() + "  " + indices[0] + " " + indices[1] + " ");
//            System.err.println(shortest_way(grid, indices[0], indices[1], agentRow, agentCol));
        }

        PriorityQueue<Character> subgoal = new PriorityQueue<>(Comparator.comparingInt(c -> -getIntValue(c, goal, cost)));

        for (char c : goal) {
            subgoal.offer(c);
        }

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
//        System.err.println("subgoals end");
        return subgoal;
    }

    private static int getIntValue(char c, char[] chars, int[] ints) {
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == c) {
                return ints[i];
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

    public int freeze_cell(Map<Character, int[]> completedGoals, State s) {
        int inPosition = 0;
        char[][] boxes = s.boxes;
        for (Map.Entry<Character, int[]> entry : completedGoals.entrySet()) {
            char boxName = entry.getKey();
//            System.err.println("Compeleted Goals:" + boxName);
            int[] indices = entry.getValue();
            if (boxes[indices[0]][indices[1]] != boxName) {
                inPosition += 1000;
            }
        }
        return inPosition;
    }
}
