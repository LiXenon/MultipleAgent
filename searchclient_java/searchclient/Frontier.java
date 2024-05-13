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
