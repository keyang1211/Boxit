package newSearchClient.search.box;

import java.util.HashSet;
import java.util.PriorityQueue;

import newSearchClient.Action;
import newSearchClient.Agent;
import newSearchClient.Box;
import newSearchClient.Communication;
import newSearchClient.State;


public class SearchBox{

    /*public Search(char[][] staticElements, Agent agent, int goalX, int goalY){
        frontier = new PriorityQueue<>(10000, new Heuristic());
        frontier.add(new SimpleState(agent.posX, agent.posY, goalX, goalY, staticElements));
    }*/

    public static AgentBoxState startSearch(char[][] staticElements, int[][] entityID, Agent agent, Box box, int goalX, int goalY, boolean noCollision){
        //If something is on top of the goal there is no reason to plan without noCollision = false
        if(!noCollision && entityID[goalX][goalY] > -1){
            System.err.println(entityID[goalX][goalY] + " is on top of the goal");
            return null;
        }

        PriorityQueue<AgentBoxState> frontier = new PriorityQueue<>(10000, new BoxHeuristic());
        AgentBoxState simpleState = new AgentBoxState(agent.posX, agent.posY, box.posX, box.posY, goalX, goalY, staticElements, entityID, noCollision);
        frontier.add(simpleState);
        AgentBoxState s = null;
        HashSet<AgentBoxState> expanded = new HashSet<>();
        int timeout = 100;
        int i = 0;
        while( i < timeout){
            s = frontier.poll();
            //System.err.println("(" + s.boxX + ", " + s.boxY + ") -> (" + s.goalX + ", " + s.goalY + ")");
            if(s == null || s.isGoalState()) break;


            expanded.add(s);
            for(Action action : Action.values()){
                boolean cond = s.isApplicable(action);
                if(cond){
                    AgentBoxState newS = new AgentBoxState(s);
                    newS.blockingID = s.tmpBlockingID;
                    newS.doAction(action);
                    if (!frontier.contains(newS) && !expanded.contains(newS))
                    {
                        frontier.add(newS);
                        i = 0;
                    }
                }
            }
            i++;
        }

        if(s == null){
            return null;
        }

        //test if finalstate is non-blocking
        //Replan from previous step
        boolean occupied = false;
        int x = s.agentX;
        int y = s.agentY;
        int startTime = s.g + State.time - 1;
        for(int j = startTime; j < startTime+20; j++)
        {
            if(Communication.positionHasAction(x, y, j)){
                System.err.println("Occupied at time " + j + " at");
                occupied = true;
                break;
            }
        }

        if(occupied){
            System.err.println("SearchBox replanning");
            while( i < timeout){
                s = frontier.poll();
                //System.err.println("(" + s.boxX + ", " + s.boxY + ") -> (" + s.goalX + ", " + s.goalY + ")");
                if(s == null || s.isGoalState()) break;
    
    
                expanded.add(s);
                for(Action action : Action.values()){
                    if(s.isApplicable(action)){
                        AgentBoxState newS = new AgentBoxState(s);
                        newS.blockingID = s.tmpBlockingID;
                        newS.doAction(action);
                        if (!frontier.contains(newS) && !expanded.contains(newS))
                        {
                            frontier.add(newS);
                            i = 0;
                        }
                    }
                }
                i++;
            }
        }

        return s;
    }
}
/*这段代码是SearchBox类的实现，用于在代理和箱子的状态下执行搜索。

以下是代码的详细解析：

startSearch方法是搜索的入口点，它接受静态元素的布局、实体ID数组、代理的位置、箱子的位置、目标位置以及是否允许碰撞作为参数。

首先，它检查如果目标位置上有其他实体（entityID[goalX][goalY] > -1）并且不允许碰撞（noCollision为false），则打印错误信息并返回null。

然后，它创建一个优先级队列frontier，用于存储待扩展的状态。它创建一个AgentBoxState对象simpleState，使用提供的代理位置、箱子位置、目标位置、静态元素布局、实体ID数组和是否允许碰撞来初始化它，并将其添加到frontier队列中。

接下来，它定义一个AgentBoxState变量s，用于存储搜索过程中的当前状态。它还创建一个HashSet对象expanded，用于存储已经扩展过的状态。

然后，它使用一个循环进行搜索，循环条件为i小于超时值timeout。在每次迭代中，它从frontier队列中取出一个状态s。如果s为null或者s是目标状态，则跳出循环。

在每次迭代中，它将当前状态s添加到expanded集合中，并遍历所有可能的动作。对于每个动作，它检查动作是否适用于当前状态（通过调用isApplicable方法），如果适用，则创建一个新的AgentBoxState对象newS作为s的副本，并执行该动作。然后，它检查frontier队列和expanded集合中是否已经存在newS对象，如果不存在，则将其添加到frontier队列中，并将i重置为0，表示有新的状态被添加。

最后，如果搜索过程中找到了目标状态，则返回该状态s。如果搜索过程中未找到目标状态，则返回null。

值得注意的是，代码中还包含了一个处理状态被占用的情况。它检查最终状态的代理位置是否在某个时间步骤上被占用。如果是，则进行重新规划。在重新规划的过程中，它执行与前面相同的搜索过程，直到找到一个非被占用的状态或超过超时值。最后，它返回找到的状态s。

SearchBox类用于执行代理和箱子状态下的搜索，通过使用优先级队列和状态扩展集合来实现状态空间搜索。 */