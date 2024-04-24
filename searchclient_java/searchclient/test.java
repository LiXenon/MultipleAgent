package searchclient;

import java.util.*;

class Node1 {
    int x, y, cost;

    public Node1(int x, int y, int cost) {
        this.x = x;
        this.y = y;
        this.cost = cost;
    }
}

public class test {
    public static int dijkstra(int[][] grid, int startX, int startY, int goalX, int goalY) {
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
                return distance[x][y];
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

    public static void main(String[] args) {
        int[][] grid = {
                {1, 2, 3, 4},
                {2, 3, 5, 5},
                {3, 5, 1000, 5},
                {4, 5, 6, 7}
        };

        int startX = 0;
        int startY = 0;
        int goalX = 2;
        int goalY = 2;

        int shortestPathCost = dijkstra(grid, startX, startY, goalX, goalY);
        System.out.println("Shortest path cost: " + shortestPathCost);
    }
}

