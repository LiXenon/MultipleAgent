package searchclient;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;


public interface Frontier
{
    void add(State state);
    State pop();
    boolean isEmpty();
    int size();
    boolean contains(State state);
    String getName();
}

class FrontierBFS
        implements Frontier
{
    private final ArrayDeque<State> queue = new ArrayDeque<>(65536);
    private final HashSet<State> set = new HashSet<>(65536);

    @Override
    public void add(State state)
    {
        this.queue.addLast(state);
        this.set.add(state);
    }

    @Override
    public State pop()
    {
        State state = this.queue.pollFirst();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    @Override
    public int size()
    {
        return this.queue.size();
    }

    @Override
    public boolean contains(State state)
    {
        return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return "breadth-first search";
    }
}

class FrontierDFS
        implements Frontier
{
    private final ArrayDeque<State> queue = new ArrayDeque<>(65536);
    private final HashSet<State> set = new HashSet<>(65536);

    @Override
    public void add(State state)
    {
        this.queue.addLast(state);
        this.set.add(state);
    }

    @Override
    public State pop()
    {
        State state = this.queue.pollLast();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    @Override
    public int size()
    {
        return this.queue.size();
    }

    @Override
    public boolean contains(State state)
    {
        return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return "depth-first search";
    }
}

class FrontierBestFirst
        implements Frontier
{
    private Heuristic heuristic;
    private final PriorityQueue<StateWrapper> queue = new PriorityQueue<>();
    private final HashSet<State> set = new HashSet<>(65536);

    public FrontierBestFirst(Heuristic h)
    {
        this.heuristic = h;
    }

    private class StateWrapper implements Comparable<StateWrapper> {
        private final State state;
        private final int f;

        public StateWrapper(State state, int f) {
            this.state = state;
            this.f = f;
        }

        @Override
        public int compareTo(StateWrapper other) {
            return this.f - other.f;
        }

        @Override
        public String toString() {
            return "StateWrapper{" +
                    "state=" + state.toString() +
                    ", f=" + f +
                    '}';
        }
    }

    @Override
    public void add(State state) {
            int f = heuristic.f(state);
            queue.add(new StateWrapper(state, f));
            set.add(state);

//            System.err.println("Current Frontier:");
//            System.err.println(toString());
    }

    @Override
    public State pop()
    {
        StateWrapper stateWrapper = this.queue.poll();
        State state = stateWrapper.state;
        this.set.remove(state);
        try {
            System.err.println(state.toString());
            System.err.println(stateWrapper.f);
            System.err.println(state.helps.toString());
            System.err.println(state.agentConflicts.toString());
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

                    if (s.getRequesterAgentConflict(i) != null && s.getRequesterHelp(i) != null) {
                        if ((agentRows[i] == parentAgentRows[i] && agentCols[i] == parentAgentCols[i]) && (!subgoal.get(i).isEmpty())) {

                        }
                    }
                }
//            }


            }
            if (state.jointAction != null) {
                for (Action action : state.jointAction) {
                    System.err.println(action.toString());
                }
            }
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        System.err.println(toString());
        return state;
    }

    @Override
    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    @Override
    public int size()
    {
        return this.queue.size();
    }

    @Override
    public boolean contains(State state)
    {
        return this.set.contains(state);
    }

    @Override
    public String toString() {
        return "FrontierBestFirst{" +
//                "heuristic=" + heuristic +
//                ", queue=" + queue +
//                ", set=" + set +
                '}';
    }

    @Override
    public String getName()
    {
        return String.format("best-first search using %s", this.heuristic.toString());
    }
}
