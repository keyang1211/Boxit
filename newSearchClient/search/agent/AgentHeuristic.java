package newSearchClient.search.agent;

import newSearchClient.search.Heuristic;

public class AgentHeuristic extends Heuristic<AgentState>{

    public AgentHeuristic(){}

    public int h(AgentState s){
        return (int) Math.sqrt(Math.pow(s.agentX - s.goalX, 2) + Math.pow(s.agentY - s.goalY, 2));
    }

    public int f(AgentState s)
    {
        return s.g + this.h(s);
    }

    @Override
    public int compare(AgentState s1, AgentState s2) {
        return f(s1) - f(s2);
    }
    
}