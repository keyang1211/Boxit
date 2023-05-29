package newSearchClient;

public class ActionTimePair {

    public Action action;
    public int time;

    public ActionTimePair(Action action, int time) {
        this.action = action;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Action = " + action.name + ", time = " + time;
    }
    
}
