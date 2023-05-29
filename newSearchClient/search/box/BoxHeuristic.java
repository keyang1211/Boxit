package newSearchClient.search.box;

import newSearchClient.search.Heuristic;

public class BoxHeuristic extends Heuristic<AgentBoxState>{

    public BoxHeuristic(){}

    public int h(AgentBoxState s){
        return (int) Math.sqrt(Math.pow(s.boxX - s.goalX, 2) + Math.pow(s.boxY - s.goalY, 2));
        //return (int) Math.abs(s.boxX - s.goalX) + Math.abs(s.boxY - s.goalY);
    }

    public int f(AgentBoxState s)
    {
        return s.g + this.h(s);
    }

    @Override
    public int compare(AgentBoxState s1, AgentBoxState s2) {
        return f(s1) - f(s2);
    }
    
}