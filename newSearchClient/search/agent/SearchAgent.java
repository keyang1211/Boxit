package newSearchClient.search.agent;

import java.util.HashSet;
import java.util.PriorityQueue;
import newSearchClient.Action;
import newSearchClient.Agent;

public class SearchAgent {
    /*public Search(char[][] staticElements, Agent agent, int goalX, int goalY){
        frontier = new PriorityQueue<>(10000, new Heuristic());
        frontier.add(new SimpleState(agent.posX, agent.posY, goalX, goalY, staticElements));
    }*/
    // noCollision = true

    public static AgentState startSearch(char[][] staticElements, int[][] entityID, Agent agent, int goalX, int goalY, boolean noCollision){
        if(!noCollision && entityID[goalX][goalY] > -1){
            System.err.println(entityID[goalX][goalY] + " is on top of the goal");
            return null;
        }
        
        PriorityQueue<AgentState> frontier = new PriorityQueue<>(10000, new AgentHeuristic());
        AgentState simpleState = new AgentState(agent.posX, agent.posY, goalX, goalY, staticElements, entityID, noCollision);
        frontier.add(simpleState);

        AgentState s = null;
        HashSet<AgentState> expanded = new HashSet<>();
        while(true){
            s = frontier.poll();
            if(s == null || s.isGoalState()) break;

            expanded.add(s);

            for(Action action : Action.values()){
                if(s.isApplicable(action)){
                    AgentState newS = new AgentState(s);
                    newS.blockingID = s.tmpBlockingID;
                    newS.doAction(action);
                    if (!frontier.contains(newS) && !expanded.contains(newS))
                    {
                        frontier.add(newS);
                    }
                } 
            }
        }
        
        if(s == null){
            return null;
        }
        return s;
    }
}
/*
这段代码是SearchAgent类的实现，用于执行搜索算法以找到代理的最佳行动路径。

以下是代码的详细解析：

SearchAgent类包含一个静态方法startSearch，用于开始搜索并返回代理的最佳行动路径。它接受以下参数：

staticElements：代理的目标和墙的布局。
entityID：位置上的实体ID。
agent：代理对象。
goalX：目标的X坐标。
goalY：目标的Y坐标。
noCollision：一个布尔值，指示代理是否可以发生碰撞。
方法内部创建了一个优先级队列frontier和一个AgentState对象simpleState，并将simpleState添加到frontier中。frontier使用AgentHeuristic作为比较器来确定最佳的下一个状态。

使用一个HashSet来存储已扩展的状态，以避免重复扩展相同的状态。

通过循环从frontier中取出状态s，如果s是目标状态，则终止循环。否则，将s添加到已扩展的状态集合中，并遍历所有可能的动作。对于每个可行的动作，创建一个新的代理状态newS，并根据动作更新代理的位置。如果newS既不在frontier中也不在已扩展的状态集合中，则将其添加到frontier中。

最后，如果找到了目标状态s，则返回该状态作为代理的最佳行动路径。否则，返回null。

SearchAgent类用于实现搜索算法，通过探索可能的状态和动作来寻找代理的最佳行动路径。它利用优先级队列来确定下一个最有希望的状态，并使用哈希集合来避免重复扩展相同的状态。 */