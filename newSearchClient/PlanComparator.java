package newSearchClient;

import java.util.Comparator;

public class PlanComparator implements Comparator<AgentPlan> {

    @Override
    public int compare(AgentPlan o1, AgentPlan o2) {
        return o1.planLength - o2.planLength;
    }
    
}