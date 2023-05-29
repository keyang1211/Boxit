package newSearchClient.search.agent;

import newSearchClient.State;
import newSearchClient.Action;
import newSearchClient.Communication;

public class AgentState {
    public int agentX, agentY;
    public int goalX, goalY;

    // 2D array holding the goals and the walls
    public char[][] staticElements; // #, 0-9, A-Z

    public int[][] entityID;

    public int g;
    public AgentState parent;
    private boolean noCollision;
    public int blockingID = -1;
    public int tmpBlockingID = -1;
    public String action;
    
    public AgentState(int agentX, int agentY, int goalX, int goalY, char[][] staticElements, int[][] entityID, boolean noCollision){
        this.agentX = agentX;
        this.agentY = agentY;
        this.goalX = goalX;
        this.goalY = goalY;
        this.staticElements = staticElements;
        this.entityID = entityID;
        this.parent = null;
        this.noCollision = noCollision;
        this.g = 0;
    }

    //Make new state from old state
    public AgentState(AgentState state){
        this.agentX = state.agentX;
        this.agentY = state.agentY;
        this.goalX = state.goalX;
        this.goalY = state.goalY;
        this.staticElements = state.staticElements;
        this.entityID = state.entityID;
        this.parent = state;
        this.noCollision = state.noCollision;
        this.g = state.g + 1;
    }
    
    public void doAction(Action action){
        this.agentX += action.agentXDelta;
        this.agentY += action.agentYDelta;
        this.action = action.name;
    }

    public boolean isApplicable(Action action){
        switch (action.type)
        {
            case NoOp:
                return true;
            case Move:
                boolean valid = isFreeCell(agentX + action.agentXDelta, agentY + action.agentYDelta);
                //System.err.println("Agent before isFreecell " + agentX + ", " + agentY + " to " + (agentX + action.agentXDelta) + ", " + (agentY + action.agentYDelta) + " is valid: " + valid);
                return valid;
            case Push:
                return false;
            case Pull:
                return false;
        }
        return false;
    }

    public boolean isFreeCell(int x, int y){
        this.tmpBlockingID = -1;
        if (staticElements[x][y] == '+') return false; //Wall
        if (agentX == x && agentY == y) return false;
        if (entityID[x][y] != -1) {
            this.tmpBlockingID = entityID[x][y];
            //System.err.println("Freecell isblocking " + blockingID + " time: " + g);
            //System.err.println("Position " + x + ", " + y + ": " + g + " EntityID" + Arrays.deepToString(entityID));
            return noCollision;
        } //Box or agent is here
        

        // !(true || true) == !true = false
        // !(false || true) == !true =  false
        // !(false || false) == !false =  true
        //boolean back = Communication.positionHasAction(x, y, g + State.time - 1);
        boolean now = Communication.positionHasAction(x, y, g + State.time);
        boolean future = Communication.positionHasAction(x, y, g + State.time + 1);
        //if(x == 14 && y == 12 && g + State.time == 33){
        //    System.err.println(agentX + ", " + agentY + ": " + " || " + now + " || " + future);
        //}
        return !(now || future);
    }

    public boolean isGoalState(){
        return agentX == goalX && agentY == goalY;
    }

    public int[] getBlockingIDs(){
        AgentState s = this;
        int[] blockingIDs = new int[g];
        
        while (s.action != null){
            blockingIDs[s.g - 1] = s.blockingID;
            s = s.parent;
        }
        
        return blockingIDs;
    }
    
    public String[] getPlan(){
        AgentState s = this;
        String[] actions = new String[g];
        
        while (s.action != null){
            actions[s.g - 1] = s.action;
            s = s.parent;
        }
        return actions;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        AgentState other = (AgentState) o;
        return this.agentX == other.agentX && this.agentY == other.agentY;
    }

    //Used by expanded and frontier in search
    //Made using this https://stackoverflow.com/questions/113511/best-implementation-for-hashcode-method-for-a-collection
    @Override
    public int hashCode(){
        int result = 42; //Answer to the universe
        //37 is the chosen prime number
        result = 37 * result + agentX + agentY;
        return result;
    }

    @Override
    public String toString(){
        return "(" + agentX + ", " + agentY + ")";
    }
}
/*这段代码是AgentState类的实现，用于表示代理的状态。

以下是代码的详细解析：

AgentState类包含以下实例变量：

agentX：代理的当前X坐标。
agentY：代理的当前Y坐标。
goalX：代理的目标X坐标。
goalY：代理的目标Y坐标。
staticElements：一个二维字符数组，表示目标和墙的布局。
entityID：一个二维整数数组，表示位置上的实体ID。
g：从起始状态到当前状态的代价。
parent：指向父状态的引用。
noCollision：一个布尔值，指示代理是否可以发生碰撞。
blockingID：阻塞代理的实体的ID。
tmpBlockingID：临时存储的阻塞代理的实体的ID。
action：执行的动作的名称。
AgentState类包含以下构造函数：

AgentState(int agentX, int agentY, int goalX, int goalY, char[][] staticElements, int[][] entityID, boolean noCollision)：根据给定的参数创建一个AgentState对象。
AgentState(AgentState state)：根据给定的AgentState对象创建一个新的AgentState对象。
doAction方法根据给定的动作更新代理的位置。
isApplicable方法用于检查给定的动作是否适用于当前状态。
isFreeCell方法用于检查给定位置是否是空闲的。它检查静态元素、代理和箱子的位置，并根据通信类中的黑板信息确定是否有其他动作占据位置。
isGoalState方法检查代理是否达到目标状态。
getBlockingIDs方法返回导致当前状态的阻塞实体的ID数组。
getPlan方法返回从起始状态到当前状态的动作数组。
equals方法用于比较两个AgentState对象是否相等。
hashCode方法用于计算AgentState对象的哈希码。
toString方法返回AgentState对象的字符串表示形式。

AgentState类用于表示搜索算法中的代理状态，包括代理的位置、目标位置、静态元素、实体ID等信息。它还提供了一些辅助方法，用于检查动作的适用性、判断是否达到目标状态，以及获取相关的阻塞实体ID和动作计划。 */