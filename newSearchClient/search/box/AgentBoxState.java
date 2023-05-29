package newSearchClient.search.box;

import newSearchClient.Action;
import newSearchClient.Communication;
import newSearchClient.State;
import newSearchClient.search.agent.AgentState;

public class AgentBoxState extends AgentState {
    int boxX, boxY;
    public AgentBoxState(int agentX, int agentY, int boxX, int boxY, int goalX, int goalY, char[][] staticElements, int[][] entityID, boolean noCollision){
        super(agentX, agentY, goalX, goalY, staticElements, entityID, noCollision);
        this.boxX = boxX;
        this.boxY = boxY;
    }

    public AgentBoxState(AgentBoxState state){
        super(state);
        this.boxX = state.boxX;
        this.boxY = state.boxY;
    }
    @Override 
    public void doAction(Action action){
        super.doAction(action);
        this.boxX += action.boxXDelta;
        this.boxY += action.boxYDelta;
    }

    @Override
    public boolean isGoalState(){
        return boxX == goalX && boxY == goalY;
    }

    @Override
    public boolean isApplicable(Action action){
        switch (action.type)
        {
            case NoOp:
                return true;
            case Move:
                return isFreeCell(agentX + action.agentXDelta, agentY + action.agentYDelta);
            case Push:
                if (boxX == agentX + action.agentXDelta && boxY == agentY + action.agentYDelta) {
                    return isFreeCell(boxX + action.boxXDelta, boxY + action.boxYDelta) && !Communication.positionHasAction(agentX + action.agentXDelta, agentY + action.agentYDelta, g + State.time + 1);
                } else {
                    return false;
                }
            case Pull:
                if (boxX == agentX - action.boxXDelta && boxY == agentY - action.boxYDelta) {
                    return isFreeCell(agentX + action.agentXDelta, agentY + action.agentYDelta) && !Communication.positionHasAction(boxX + action.boxXDelta, boxY + action.boxYDelta, g + State.time + 1);
                } else {
                    return false;
                }
        }
        return false;
    }

    public boolean isFreeCell(int x, int y){
        if(boxX == x && boxY == y) return false;
        return super.isFreeCell(x, y);
    }

    @Override
    public String toString(){
        return "Agent (" + this.agentX + ", " + this.agentY +") does " + this.action + " Box (" + this.boxX + ", " + this.boxY + ")";
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        AgentBoxState other = (AgentBoxState) o;
        boolean agents = this.agentX == other.agentX && this.agentY == other.agentY;
        boolean boxes = this.boxX == other.boxX && this.boxY == other.boxY;
        return agents && boxes;
    }
    
}
