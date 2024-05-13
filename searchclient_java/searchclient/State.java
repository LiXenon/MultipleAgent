package searchclient;

import java.util.*;

public class State
{
    private static final Random RNG = new Random(1);

    /*
        The agent rows, columns, and colors are indexed by the agent number.
        For example, this.agentRows[0] is the row location of agent '0'.
    */
    public int[] agentRows;
    public int[] agentCols;
    public static Color[] agentColors;

    /*
        The walls, boxes, and goals arrays are indexed from the top-left of the level, row-major order (row, col).
               Col 0  Col 1  Col 2  Col 3
        Row 0: (0,0)  (0,1)  (0,2)  (0,3)  ...
        Row 1: (1,0)  (1,1)  (1,2)  (1,3)  ...
        Row 2: (2,0)  (2,1)  (2,2)  (2,3)  ...
        ...

        For example, this.walls[2] is an array of booleans for the third row.
        this.walls[row][col] is true if there's a wall at (row, col).

        this.boxes and this.char are two-dimensional arrays of chars. 
        this.boxes[1][2]='A' means there is an A box at (1,2). 
        If there is no box at (1,2), we have this.boxes[1][2]=0 (null character).
        Simiarly for goals. 

    */
    public static boolean[][] walls;
    public char[][] boxes;
    public static char[][] goals;

    /*
        The box colors are indexed alphabetically. So this.boxColors[0] is the color of A boxes, 
        this.boxColor[1] is the color of B boxes, etc.
    */
    public static Color[] boxColors;

    public final State parent;
    public final Action[] jointAction;
    private final int g;

    private int hash = 0;

    public ArrayList<LinkedList<Character>> subgoal = new ArrayList<LinkedList<Character>>();;
    public Map<Character, int[]> completedGoals = new HashMap<>();
    public int completedHelps = 0;
    public int completedAgentConflicts = 0;

    public Map<Character, int[]> goalsAndPositon = new HashMap<>();
    public Map<Character, int[]> boxesAndPositon = new HashMap<>();


    public int[][] grid;
    private static final int EMPTY_COST = 1;
    private static final int GOAL_COST = 1000;
    private static final int BLOCK_COST = 10000;
    private static final int BOX_COST = 100;

    public List<Help> helps;

    public List<AgentConflict> agentConflicts;

    // Constructs an initial state.
    // Arguments are not copied, and therefore should not be modified after being passed in.
    public State(int[] agentRows, int[] agentCols, Color[] agentColors, boolean[][] walls,
                 char[][] boxes, Color[] boxColors, char[][] goals
    )
    {
        this.agentRows = agentRows;
        this.agentCols = agentCols;
        this.agentColors = agentColors;
        this.walls = walls;
        this.boxes = boxes;
        this.boxColors = boxColors;
        this.goals = goals;
        this.parent = null;
        this.jointAction = null;
        this.completedHelps = 0;
        this.completedAgentConflicts = 0;
        this.g = 0;

        int rows = walls.length, cols = walls[0].length;
        this.grid = new int[rows][cols];
        this.agentConflicts = new ArrayList<>();
        this.helps = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            Arrays.fill(this.grid[i], EMPTY_COST);
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (walls[i][j] == true) {
                    this.grid[i][j] = BLOCK_COST;
                } else if (goals[i][j] != 0) {
                    this.grid[i][j] = GOAL_COST;
                }
            }
        }

        Subgoals subgoals = new Subgoals();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = goals[i][j];
                char d = boxes[i][j];
                if (c != 0) {
                    this.goalsAndPositon.put(c, new int[]{i, j});
                }
                if (d != 0) {
                    this.boxesAndPositon.put(d, new int[]{i, j});
                }
            }
        }
        this.subgoal = subgoals.sort_priority(this.grid, this.walls, this.goals, this.agentRows, this.agentCols, this.goalsAndPositon, this.agentColors, this.boxColors);

        System.err.println(this.subgoal);

//        for (int i = 0; i < rows; i++) {
//            for (int j = 0; j < cols; j++) {
//                char c = goals[i][j];
//                char d = boxes[i][j];
//                if (c != 0) {
//                    this.goalsAndPositon.put(c, new int[]{i, j});
//                }
//                if (d != 0) {
//                    this.boxesAndPositon.put(d, new int[]{i, j});
//                }
//            }
//        }
    }


    // Constructs the state resulting from applying jointAction in parent.
    // Precondition: Joint action must be applicable and non-conflicting in parent state.
    private State(State parent, Action[] jointAction)
    {
        // Copy parent
        this.agentRows = Arrays.copyOf(parent.agentRows, parent.agentRows.length);
        this.agentCols = Arrays.copyOf(parent.agentCols, parent.agentCols.length);
        this.completedGoals = parent.completedGoals;
        this.subgoal = parent.subgoal;
        this.grid = parent.grid;
        this.agentConflicts = parent.agentConflicts;
        this.helps = parent.helps;
        this.boxes = new char[parent.boxes.length][];
        for (int i = 0; i < parent.boxes.length; i++)
        {
            this.boxes[i] = Arrays.copyOf(parent.boxes[i], parent.boxes[i].length);
        }

        // Set own parameters
        this.parent = parent;
        this.jointAction = Arrays.copyOf(jointAction, jointAction.length);
        this.completedAgentConflicts = parent.completedAgentConflicts;
        this.completedHelps = parent.completedHelps;
        this.g = parent.g + 1;

        // Apply each action
        int numAgents = this.agentRows.length;
        for (int agent = 0; agent < numAgents; ++agent)
        {
            Action action = jointAction[agent];
            char box;
            int boxrow;
            int boxcol;

            switch (action.type)
            {
                case NoOp:
                    break;

                case Move:
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    break;

                case Push:
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;

                    boxrow = this.agentRows[agent];
                    boxcol = this.agentCols[agent];

                    box = this.boxes[boxrow][boxcol];

                    this.boxes[boxrow + action.boxRowDelta][boxcol + action.boxColDelta] = box;
                    this.boxes[boxrow][boxcol] = 0;;
                    break;

                case Pull:
                    boxrow = this.agentRows[agent];
                    boxcol = this.agentCols[agent];

                    box = this.boxes[boxrow - action.boxRowDelta][boxcol - action.boxColDelta];

                    this.boxes[boxrow][boxcol] = box;
                    this.boxes[boxrow - action.boxRowDelta][boxcol - action.boxColDelta] = 0;

                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    break;

            }
        }

        int rows = walls.length, cols = walls[0].length;

        this.goalsAndPositon = new HashMap<>();
        this.boxesAndPositon = new HashMap<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = goals[i][j];
                char d = boxes[i][j];
                if (c != 0) {
                    this.goalsAndPositon.put(c, new int[]{i, j});
                }
                if (d != 0) {
                    this.boxesAndPositon.put(d, new int[]{i, j});
                }
            }
        }
    }

    public int g()
    {
        return this.g;
    }

    public boolean isGoalState()
    {
        for (int row = 1; row < this.goals.length - 1; row++)
        {
            for (int col = 1; col < this.goals[row].length - 1; col++)
            {
                char goal = this.goals[row][col];

                if ('A' <= goal && goal <= 'Z' && this.boxes[row][col] != goal)
                {
                    return false;
                }
                else if ('0' <= goal && goal <= '9' &&
                        !(this.agentRows[goal - '0'] == row && this.agentCols[goal - '0'] == col))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<State> getExpandedStates()
    {
        int numAgents = this.agentRows.length;

        // Determine list of applicable actions for each individual agent.
        Action[][] applicableActions = new Action[numAgents][];
        for (int agent = 0; agent < numAgents; ++agent)
        {
            ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
            for (Action action : Action.values())
            {
                // !!! In Multiple Agent, if you encounter a box-blocked situation or agent-blocked situation, make a help request
                // If someone can help (or give in), no operation will be performed until the help (or give in) is completed; thereafter, the helper (giver) is not allowed to act until the Agent resolves the situation just now.
                // All agents are not allowed to offer or accept any help (concession) when helping (or giving in) and solving situations.
                if (this.isApplicable(agent, action))
                {
                    agentActions.add(action);
                }
            }
            applicableActions[agent] = agentActions.toArray(new Action[0]);
        }

        // Iterate over joint actions, check conflict and generate child states.
        Action[] jointAction = new Action[numAgents];
        int[] actionsPermutation = new int[numAgents];
        ArrayList<State> expandedStates = new ArrayList<>(16);
        while (true)
        {
            for (int agent = 0; agent < numAgents; ++agent)
            {
                jointAction[agent] = applicableActions[agent][actionsPermutation[agent]];
            }

            if (!this.isConflicting(jointAction))
            {
                expandedStates.add(new State(this, jointAction));
            }

            // Advance permutation
            boolean done = false;
            for (int agent = 0; agent < numAgents; ++agent)
            {
                if (actionsPermutation[agent] < applicableActions[agent].length - 1)
                {
                    ++actionsPermutation[agent];
                    break;
                }
                else
                {
                    actionsPermutation[agent] = 0;
                    if (agent == numAgents - 1)
                    {
                        done = true;
                    }
                }
            }

            // Last permutation?
            if (done)
            {
                break;
            }
        }

        Collections.shuffle(expandedStates, State.RNG);
        return expandedStates;
    }

    private boolean isApplicable(int agent, Action action)
    {
        int agentRow = this.agentRows[agent];
        int agentCol = this.agentCols[agent];
        Color agentColor = this.agentColors[agent];
        Color[] boxColor = this.boxColors;
        char box;
        int boxColorIndex;
        int destinationRow;
        int destinationCol;

        switch (action.type)
        {
            case NoOp:
                return true;

            case Move:
                destinationRow = agentRow + action.agentRowDelta;
                destinationCol = agentCol + action.agentColDelta;
                if (!isNotOutBoundary(destinationRow,destinationCol)) {return false;}
                return this.cellIsFree(destinationRow, destinationCol);

            case Push:
                destinationRow = agentRow + action.agentRowDelta;
                destinationCol = agentCol + action.agentColDelta;
                if (!isNotOutBoundary(destinationRow,destinationCol)) {return false;}
                box = this.boxes[destinationRow][destinationCol];
                if (box == 0) {
                    return false;
                }
                boxColorIndex = box - 'A';
                if (boxColor[boxColorIndex] != agentColor) {
                    return false;
                }
                if (!isNotOutBoundary(destinationRow + action.boxRowDelta,destinationCol + action.boxColDelta)) {return false;}
                return this.cellIsFree(destinationRow + action.boxRowDelta, destinationCol + action.boxColDelta);

            case Pull:
                destinationRow = agentRow;
                destinationCol = agentCol;
                if (!isNotOutBoundary(destinationRow - action.boxRowDelta,destinationCol - action.boxColDelta)) {return false;}
                box = this.boxes[destinationRow - action.boxRowDelta][destinationCol - action.boxColDelta];

                if (box == 0) {
                    return false;
                }
                boxColorIndex = box - 'A';
                if (boxColor[boxColorIndex] != agentColor) {
                    return false;
                }
                if (!isNotOutBoundary(destinationRow + action.agentRowDelta,destinationCol + action.agentColDelta)) {return false;}
                return this.cellIsFree(destinationRow + action.agentRowDelta, destinationCol + action.agentColDelta);

        }

        // Unreachable:
        return false;
    }

    private boolean isNotOutBoundary(int x, int y) {
        int rows = this.walls.length, cols = this.walls[0].length;
        return x >= 0 && x < rows && y >= 0 && y < cols; // true not out, false out
    }
    //    !!!Next is to add new features in this method (or another new method) to implement BDI.
//    Initial thoughts are executing concession when meeting conflicting, and asking for help when encountering blocks
    private boolean isConflicting(Action[] jointAction)
    {
        int numAgents = this.agentRows.length;

        int[] destinationRows = new int[numAgents]; // row of new cell to become occupied by action
        int[] destinationCols = new int[numAgents]; // column of new cell to become occupied by action
        int[] boxRows = new int[numAgents]; // current row of box moved by action
        int[] boxCols = new int[numAgents]; // current column of box moved by action

        // Collect cells to be occupied and boxes to be moved
        for (int agent = 0; agent < numAgents; ++agent)
        {
            Action action = jointAction[agent];
            int agentRow = this.agentRows[agent];
            int agentCol = this.agentCols[agent];
            int boxRow;
            int boxCol;

            switch (action.type)
            {
                case NoOp:
                    break;

                case Move:
                    destinationRows[agent] = agentRow + action.agentRowDelta;
                    destinationCols[agent] = agentCol + action.agentColDelta;
                    boxRows[agent] = destinationRows[agent]; // Distinct dummy value
                    boxCols[agent] = destinationCols[agent]; // Distinct dummy value
                    break;

                case Push:
                    destinationRows[agent] = agentRow + action.agentRowDelta;
                    destinationCols[agent] = agentCol + action.agentColDelta;
                    boxRows[agent] = destinationRows[agent] + action.boxRowDelta;
                    boxCols[agent] = destinationCols[agent] + action.boxColDelta;
                    break;

                case Pull:
                    destinationRows[agent] = agentRow + action.agentRowDelta;
                    destinationCols[agent] = agentCol + action.agentColDelta;
                    boxRows[agent] = agentRow;
                    boxCols[agent] = agentCol;
                    break;
            }
        }

        for (int a1 = 0; a1 < numAgents; ++a1)
        {
            if (jointAction[a1] == Action.NoOp)
            {
                continue;
            }

            for (int a2 = a1 + 1; a2 < numAgents; ++a2)
            {
                if (jointAction[a2] == Action.NoOp)
                {
                    continue;
                }

                // Moving into same cell?
                if (destinationRows[a1] == destinationRows[a2] && destinationCols[a1] == destinationCols[a2])
                {
                    return true;
                } else if (boxRows[a1] == boxRows[a2] && boxCols[a1] == boxCols[a2]) {
                    return true;
                } else if((destinationRows[a1] == boxRows[a2] && destinationCols[a1] == boxCols[a2])) {
                    return true;
                } else if (boxRows[a1] == destinationRows[a2] && boxCols[a1] == destinationCols[a2]) {
                    return true;
                }
            }
        }

        return false;
    }
    // Get the shortest path from box to goal
    public List<int[]> getShortestPath(int[] start, int[] goal, int[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;

        PriorityQueue<int[]> frontier = new PriorityQueue<>(Comparator.comparingInt(cell -> grid[cell[0]][cell[1]]));
        frontier.offer(new int[]{start[0], start[1], 0});

        int[][] costSoFar = new int[rows][cols];
        for (int[] row : costSoFar) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }
        costSoFar[start[0]][start[1]] = 0;

        int[][][] cameFrom = new int[rows][cols][];
        cameFrom[start[0]][start[1]] = null;

        while (!frontier.isEmpty()) {
            int[] current = frontier.poll();
            int currentX = current[0];
            int currentY = current[1];

            if (currentX == goal[0] && currentY == goal[1]) {
                break;
            }

            int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            for (int[] dir : directions) {
                int newX = currentX + dir[0];
                int newY = currentY + dir[1];
                if (newX >= 0 && newX < rows && newY >= 0 && newY < cols) {
                    int newCost = costSoFar[currentX][currentY] + grid[newX][newY];
                    if (newCost < costSoFar[newX][newY]) {
                        costSoFar[newX][newY] = newCost;
                        frontier.add(new int[]{newX, newY, newCost});
                        cameFrom[newX][newY] = new int[]{currentX, currentY};
                    }
                }
            }
        }
//        List<int[]> path = reconstructPath(cameFrom, start, goal);
//        for (int[] step : path) {
//            System.out.println(Arrays.toString(step));
//        }
        return reconstructPath(cameFrom, start, goal);
    }

    private static List<int[]> reconstructPath(int[][][] cameFrom, int[] start, int[] goal) {
        List<int[]> path = new ArrayList<>();
        int[] current = goal;
        while (current != null && !(current[0] == start[0] && current[1] == start[1])) {
            path.add(current);
            current = cameFrom[current[0]][current[1]];
        }
        path.add(start);
        Collections.reverse(path);
        return path;
    }


    // Get the id of the first box on the path, if box not found, return '0'
    // The box cannot be agent's box
    private char firstBoxOnThePath(List<int[]> path) {
        if (!path.isEmpty()) path.remove(0);
        for (int[] coor: path) {
            Iterator<Map.Entry<Character, int[]>> it = this.boxesAndPositon.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Character, int[]> entry = it.next();
                if (coor[0] == entry.getValue()[0] && coor[1] == entry.getValue()[1]) {
                    return entry.getKey(); // box found, return its id
                }
            }
        }
        return '0'; // If return 0, means no box found on the path
    }

    // Check if the box is still on the path
    private boolean isBoxOnThePath(List<int[]> path, int[] boxCoordinate) {
        for (int[] coor: path) {
            if (boxCoordinate[0] == coor[0] && boxCoordinate[1] == coor[1]) return true;
        }
        return false;
    }

    private boolean cellIsFree(int row, int col)
    {
        return !this.walls[row][col] && this.boxes[row][col] == 0 && this.agentAt(row, col) == 0;
    }

    private char agentAt(int row, int col)
    {
        for (int i = 0; i < this.agentRows.length; i++)
        {
            if (this.agentRows[i] == row && this.agentCols[i] == col)
            {
                return (char) ('0' + i);
            }
        }
        return 0;
    }

    public Action[][] extractPlan()
    {
        Action[][] plan = new Action[this.g][];
        State state = this;
        while (state.jointAction != null)
        {
            plan[state.g - 1] = state.jointAction;
            state = state.parent;
        }
        return plan;
    }
    // If the coordinate is free and not out of boundary
    private boolean canMoveTo(int x, int y) {
        return cellIsFree(x, y) && isNotOutBoundary(x, y);
    }
    // Find coordinate to make the path unblocked
    private int[] findUnblockedCoordinate(int x, int y, List<int[]> path, int depth) {
        int startX = x;
        int startY = y;
        Set<String> visited = new HashSet<>();
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{x, y});
        while (!queue.isEmpty()) {
            int[] currentCoordinate = queue.poll();
            x = currentCoordinate[0];
            y = currentCoordinate[1];
            // Search near cells recursively
            int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
            for (int[] dir : directions) {
                int newX = x + dir[0];
                int newY = y + dir[1];
                visited.add(Arrays.toString(new int[]{x, y}));
                if (!canMoveTo(newX, newY) || visited.contains(Arrays.toString(new int[]{newX, newY}))) {
                    continue;
                }

                boolean isOn = isBoxOnThePath(path, new int[]{newX, newY});
                int ManhattanDepth = (int)(Math.sqrt(Math.pow((startX - x), 2) + Math.pow((startY - y), 2)));
                // Path is unblocked
                if (!isOn && ManhattanDepth >= depth) {
                    return new int[]{newX, newY};
                }

                queue.add(new int[]{newX, newY});
                visited.add(Arrays.toString(new int[]{newX, newY}));
            }
        }
        return null; // No suitable coordinate found
    }

    private int findAgentOfBox(char boxId) {
        int boxColorIndex = boxId - 'A';
        Color boxColor = boxColors[boxColorIndex];
        for(int i = 0; i < agentColors.length; i++) {
            if (boxColor == agentColors[i]) {
                return i;
            }
        }
        return -1; // Agent not found
    }

    // Check if agent is already a requester or a helper in the help list
    private boolean isInHelp(int agent) {
        for (Help item : helps) {
            if (item != null && (item.requesterAgent == agent || item.helperAgent == agent)) {
                return true;
            }
        }
        return false;
    }



    public Help addHelp(int requesterAgent, char requesterBox) {
        // If requester agent is requesting for help, it cannot request more
        if (isInHelp(requesterAgent)) return null;

        int[] startPosition = this.boxesAndPositon.get(requesterBox);
        int[] goalPosition = this.goalsAndPositon.get(requesterBox);
        int[][] grid = new int[this.grid.length][this.grid[0].length];
        for (int i = 0; i < grid.length; i++) {
            grid[i] = this.grid[i].clone();
        }
        // Add box cost
        for (int i = 0; i < grid.length; i++) {
            Arrays.fill(grid[i], EMPTY_COST);
        }
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (walls[i][j] == true) {
                    grid[i][j] = BLOCK_COST;
                } else if (goals[i][j] != 0) {
                    grid[i][j] = GOAL_COST;
                }
            }
        }
        for (Map.Entry<Character, int[]> entry : this.boxesAndPositon.entrySet()) {
            grid[entry.getValue()[0]][entry.getValue()[1]] = BOX_COST;
        }
//        for (int i = 0; i < agentRows.length; i++) {
//            grid[agentRows[i]][agentCols[i]] = 5;
//        }

//        int[][] printgrid = new int[grid.length][grid[0].length];
//        for (int i = 0; i < grid.length; i++) {
//            for (int j = 0; j < grid[i].length; j++) {
//                grid[i][j] = grid[i][j];
//                if (walls[i][j] == true) {
//                    printgrid[i][j] = 9;
//                } else if (goals[i][j] != 0) {
//                    printgrid[i][j] = 8;
//                }
//            }
//        }
//        for (int[] i : grid) {
//            System.err.println(Arrays.toString(i));
//        }
        // Shortest path
        List<int[]> path = getShortestPath(startPosition, goalPosition, grid);
        // Only the first blocker is considered
        char blocker = firstBoxOnThePath(path);
        // If id is '0', the box is not found; agent don't need to request for help
        if (blocker == '0') return null;
        // Position of blocker
        int x = this.boxesAndPositon.get(blocker)[0];
        int y = this.boxesAndPositon.get(blocker)[1];
//        if (Math.sqrt((Math.pow(x - startPosition[0], 2) + Math.pow(y - startPosition[1], 2))) > 4) return null;
        int[] unblockedCoordinate = findUnblockedCoordinate(x, y, path, 0);
        // Unable to move blocker currently, agent cannot request for help
        if (unblockedCoordinate == null) return null;
        int helperAgent = findAgentOfBox(blocker);
        // Unable to find helper agent, agent cannot request for help
        if (helperAgent == '0') return null;
        // If helper agent is helping, it cannot help more
        if (getBlockerAgentConflict(helperAgent) != null || getRequesterAgentConflict(requesterAgent) != null) return null;
        if (isInHelp(helperAgent)) return null;
//        if (!subgoal.isEmpty() && subgoal.get(helperAgent).peek() == blocker) return null;
        int[] helperCoordinate = new int[] {agentRows[helperAgent], agentCols[helperAgent]};

        List<int[]> pathHelperAgentToBlocker = getShortestPath(helperCoordinate, new int[] {x, y}, grid);
        List<int[]> pathBlockerToBeUnblocked = getShortestPath(new int[] {x, y}, unblockedCoordinate, grid);
        pathHelperAgentToBlocker.addAll(pathBlockerToBeUnblocked);
        List<int[]> helperPath = pathHelperAgentToBlocker;
        int[] requesterCoordinate = new int[] {agentRows[requesterAgent], agentCols[requesterAgent]};
//        System.err.println(Math.sqrt((Math.pow(requesterCoordinate[0] - x, 2) + Math.pow(requesterCoordinate[1] - y, 2))));
        if (Math.sqrt((Math.pow(requesterCoordinate[0] - x, 2) + Math.pow(requesterCoordinate[1] - y, 2))) > 4) return null;
        int[] requesterGoalCoordinate;
        if (isBoxOnThePath(helperPath, requesterCoordinate)) {
            requesterGoalCoordinate = findUnblockedCoordinate(requesterCoordinate[0], requesterCoordinate[1], helperPath, 0);
        } else {
            requesterGoalCoordinate = requesterCoordinate;
        }
        for (int[] i : helperPath) {
            System.err.print(Arrays.toString(i) + " ");
        }
        // Helper cannot move the blocker because requester block the way, and requester cannot move away
        if (requesterGoalCoordinate == null) return null;
        Help help = new Help(requesterAgent, helperAgent, requesterBox, blocker, unblockedCoordinate, requesterGoalCoordinate, new int[] {x, y});
        this.helps.add(help);
        System.err.println(help);
//        for(int[] p: path) {
//            System.err.print(Arrays.toString(p));
//        }
//        System.err.println("");
//        System.err.println("====================================");
//        System.err.println("");
//        for(int[] p: helperPath) {
//            System.err.print(Arrays.toString(p));
//        }
//        System.err.println("");
//        System.err.println("====================================");
//        System.err.println("");
        return help;
    }

    public Help getHelp(int agent) {
        if (helps != null) {
            Iterator<Help> it = helps.iterator();
            while (it.hasNext()) {
                Help item = it.next();
                if (item != null && (item.requesterAgent == agent || item.helperAgent == agent)) {
                    return item;
                }
            }
        }
        return null;
    }

    public Help getHelperHelp(int helperAgent) {
        if (helps != null) {
            Iterator<Help> it = helps.iterator();
            while (it.hasNext()) {
                Help item = it.next();
                if (item != null && item.helperAgent == helperAgent) {
                    return item;
                }
            }
        }
        return null;
    }

    public Help getRequesterHelp(int requesterAgent) {
        if (helps != null) {
            Iterator<Help> it = helps.iterator();
            while (it.hasNext()) {
                Help item = it.next();
                if (item != null && item.requesterAgent == requesterAgent) {
                    return item;
                }
            }
        }
        return null;
    }

    public boolean removeHelp(int agent) {
        Iterator<Help> it = helps.iterator();
        while (it.hasNext()) {
            Help item = it.next();
            if (item != null && (item.requesterAgent == agent || item.helperAgent == agent)) {
                System.err.println("Before: " + helps.toString());
                it.remove();
                System.err.println("After: " + helps.toString());
                return true;
            }
        }
        return false;
    }

    public AgentConflict getAgentConflict(int agent) {
        if (agentConflicts != null) {
            Iterator<AgentConflict> it = agentConflicts.iterator();
            while (it.hasNext()) {
                AgentConflict item = it.next();
                if (item != null && (item.requesterAgent == agent || item.blockerAgent == agent)) {
                    return item;
                }
            }
        }
        return null;
    }

    public AgentConflict getBlockerAgentConflict(int blockerAgent) {
        if (agentConflicts != null) {
            Iterator<AgentConflict> it = agentConflicts.iterator();
            while (it.hasNext()) {
                AgentConflict item = it.next();
                if (item != null && item.blockerAgent == blockerAgent) {
                    return item;
                }
            }
        }
        return null;
    }

    public AgentConflict getRequesterAgentConflict(int requesterAgent) {
        if (agentConflicts != null) {
            Iterator<AgentConflict> it = agentConflicts.iterator();
            while (it.hasNext()) {
                AgentConflict item = it.next();
                if (item != null && item.requesterAgent == requesterAgent) {
                    return item;
                }
            }
        }
        return null;
    }

    public boolean removeAgentConflict(int agent) {
        Iterator<AgentConflict> it = agentConflicts.iterator();
        while (it.hasNext()) {
            AgentConflict item = it.next();
            if (item != null && (item.requesterAgent == agent || item.blockerAgent == agent)) {
                System.err.println("Before: " + agentConflicts.toString());
                it.remove();
                System.err.println("After: " + agentConflicts.toString());
                return true;
            }
        }
        return false;
    }

    //
    public int[] blockerAgentGoalCoordinate(int requesterAgent, int blockerAgent) {
        if (isInHelp(requesterAgent) || isInHelp(blockerAgent)) return null;
        if (getBlockerAgentConflict(blockerAgent) != null || getRequesterAgentConflict(requesterAgent) != null) return null;
        int[] requesterAgentCoordinate = new int[] {agentRows[requesterAgent], agentCols[requesterAgent]};
        int[] blockerAgentCoordinate = new int[] {agentRows[blockerAgent], agentCols[blockerAgent]};
        if (subgoal.get(requesterAgent).isEmpty()) return null;
        char requesterAgentCurrentBox = subgoal.get(requesterAgent).peek();
        if (requesterAgentCurrentBox >= '0' && requesterAgentCurrentBox <= '9') return null;
        int[] requesterAgentCurrentBoxCoordinate = boxesAndPositon.get(requesterAgentCurrentBox);
        int[] requesterGoal;
        if ((int)(Math.sqrt(Math.pow((requesterAgentCoordinate[0] - requesterAgentCurrentBoxCoordinate[0]), 2) + Math.pow((requesterAgentCoordinate[1] - requesterAgentCurrentBoxCoordinate[1]), 2))) > 1) {
            requesterGoal = requesterAgentCurrentBoxCoordinate;
        } else {
            requesterGoal = goalsAndPositon.get(requesterAgentCurrentBox);
        }
        int[][] grid = new int[this.grid.length][this.grid[0].length];
        for (int i = 0; i < grid.length; i++) {
            grid[i] = this.grid[i].clone();
        }
        // Add box cost
        for (int i = 0; i < grid.length; i++) {
            Arrays.fill(grid[i], EMPTY_COST);
        }
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (walls[i][j] == true) {
                    grid[i][j] = BLOCK_COST;
                } else if (goals[i][j] != 0) {
                    grid[i][j] = GOAL_COST;
                }
            }
        }
        for (Map.Entry<Character, int[]> entry : this.boxesAndPositon.entrySet()) {
            grid[entry.getValue()[0]][entry.getValue()[1]] = BOX_COST;
        }
        List<int[]> path = getShortestPath(requesterAgentCoordinate, requesterGoal, grid);
        int[] blockerGoalCoordinate = findUnblockedCoordinate(blockerAgentCoordinate[0], blockerAgentCoordinate[1], path, 0);
//        System.err.println("AAAblockerGoalCoordinate:" + Arrays.toString(blockerGoalCoordinate));
        return blockerGoalCoordinate;
    }
    @Override
    public int hashCode()
    {
        if (this.hash == 0)
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(this.agentColors);
            result = prime * result + Arrays.hashCode(this.boxColors);
            result = prime * result + Arrays.deepHashCode(this.walls);
            result = prime * result + Arrays.deepHashCode(this.goals);
            result = prime * result + Arrays.hashCode(this.agentRows);
            result = prime * result + Arrays.hashCode(this.agentCols);
            for (int row = 0; row < this.boxes.length; ++row)
            {
                for (int col = 0; col < this.boxes[row].length; ++col)
                {
                    char c = this.boxes[row][col];
                    if (c != 0)
                    {
                        result = prime * result + (row * this.boxes[row].length + col) * c;
                    }
                }
            }
            this.hash = result;
        }
        return this.hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (this.getClass() != obj.getClass())
        {
            return false;
        }
        State other = (State) obj;
        return Arrays.equals(this.agentRows, other.agentRows) &&
                Arrays.equals(this.agentCols, other.agentCols) &&
                Arrays.equals(this.agentColors, other.agentColors) &&
                Arrays.deepEquals(this.walls, other.walls) &&
                Arrays.deepEquals(this.boxes, other.boxes) &&
                Arrays.equals(this.boxColors, other.boxColors) &&
                Arrays.deepEquals(this.goals, other.goals);
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < this.walls.length; row++)
        {
            for (int col = 0; col < this.walls[row].length; col++)
            {
                if (this.boxes[row][col] > 0)
                {
                    s.append(this.boxes[row][col]);
                }
                else if (this.walls[row][col])
                {
                    s.append("+");
                }
                else if (this.agentAt(row, col) != 0)
                {
                    s.append(this.agentAt(row, col));
                }
                else
                {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }
}