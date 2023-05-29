package newSearchClient;

public class AgentPlan {
    int agentID;
    int planLength;
    public AgentPlan(int agentID, int planLength){
        this.agentID = agentID;
        this.planLength = planLength;
    }

    @Override
    public String toString(){
        return "ID: " + agentID + ", " + planLength;
    }
}
