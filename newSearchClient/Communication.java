package newSearchClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Communication {
    public HashMap<Integer, Agent> charToAgent;
    public HashMap<Character, Agent> notFree;
    // example Lookup: blackboard.get(x).get(y).get(time)
    // A blackboard is created - a mapping from position to a list of actions with timestamps
    public static HashMap<Integer, HashMap<Integer, HashMap<Integer, Entity>>> blackboard = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Entity>>>();
    Communication() {
        this.notFree = new HashMap<>();
    }

    public void setAgents(HashMap<Integer, Agent> agents){
        this.charToAgent = agents;
    }

    public static void addToBlackboard(Position agentPos, Entity entity, int time){
        HashMap<Integer, Entity> entityTimePairs;
        // Check if there is a list for the given agent's position
        if (Communication.blackboard.containsKey(agentPos.x)) {
            if (Communication.blackboard.get(agentPos.x).containsKey(agentPos.y)){
                entityTimePairs = Communication.blackboard.get(agentPos.x).get(agentPos.y);
            } else {
                entityTimePairs = new HashMap<Integer, Entity>();
            }
        } else {
            Communication.blackboard.put(agentPos.x, new HashMap<>());
            entityTimePairs = new HashMap<Integer, Entity>();
        }
        //State.time is added to make sure 
        entityTimePairs.put(time + State.time, entity);
        Communication.blackboard.get(agentPos.x).put(agentPos.y, entityTimePairs);
    }
    
    public static void removeFromBlackboard(Position agentPos, Entity entity, int time){
        //State.time is added to make sure 
        try{
            Communication.blackboard.get(agentPos.x).get(agentPos.y).remove(time + State.time);
        } catch (NullPointerException e){
            System.err.println("Requested actions are not added to the blackboard");
        }
        
    }

    public static boolean positionHasAction(int x, int y, int time){
        if(Communication.blackboard.containsKey(x)){
            if(Communication.blackboard.get(x).containsKey(y)){
                if(Communication.blackboard.get(x).get(y).containsKey(time)){
                    return true;
                }
            }
        }
        return false;
    }

    public static void printBlackboard(){
        // Print out contents of the blackboard
            System.err.println("--------------  Blackboard  --------------");
            HashMap<Integer, HashMap<Integer, HashMap<Integer, Entity>>> blackboard = Communication.blackboard;
            for (Map.Entry<Integer, HashMap<Integer, HashMap<Integer, Entity>>> entryX : blackboard.entrySet()) {
                for(Map.Entry<Integer, HashMap<Integer, Entity>> entryY : entryX.getValue().entrySet()){
                    System.err.println(entryX.getKey() + ", " + entryY.getKey() + ": ");
                    for(Map.Entry<Integer, Entity> actionTimePair : entryY.getValue().entrySet()){
                        System.err.println("\t" + actionTimePair.getKey() + ", " + actionTimePair.getValue());
                    }
                }
                
            }
            System.err.println("------------------------------------------");
    }

    public int inquire(Agent agent, Task task, State s) {
        Agent bestAgent = null;
        Plan bestPlan = null;
        int bestValue = Integer.MAX_VALUE;

        System.err.println("Inquire " + task);

        // task is for agent to move
        if (task.entity.character - '0' >= 0 && task.entity.character - '0' <= 9) {
            Agent toEvaluateAgent = (Agent) charToAgent.get(task.entity.character - '0');
            Plan plan = toEvaluateAgent.evaluateTask(task, s);
            if(plan != null){
                bestAgent = toEvaluateAgent;
                bestPlan = plan;
            }
        } else {
            // task is for agent to move box
            for (Agent toEvaluateAgent : charToAgent.values()) {
                // Iterate through all agents that are free to accept requests
                if (toEvaluateAgent.color.equals(task.entity.color)) {
                    System.err.println("Evaluate task for " + toEvaluateAgent);
                    Plan plan = toEvaluateAgent.evaluateTask(task, s);
                    if(plan == null){
                        continue;
                    }
    
                    // Choose best agent to fulfill task with lowest value
                    if (plan.actions.length < bestValue) {
                        bestAgent = toEvaluateAgent;
                        bestPlan = plan;
                        bestValue = plan.actions.length;
                    }
                }
            }
        }

        if (bestAgent != null && bestAgent != agent) {
            System.err.println("Best agent " + bestAgent + " does task: " + task);
            bestAgent.acceptPlan(bestPlan);
            return bestPlan.actions.length;
        } else {
            System.err.println("No best agent");
            return -1;
        }
        
    }

}
/*这段代码是一个名为Communication的Java类，它实现了代理之间的通信功能。

以下是代码的详细解析：

Communication类包含以下实例变量：

charToAgent：一个HashMap，将字符映射到代理对象。字符用于标识不同的代理。
notFree：一个HashMap，用于存储当前不可用的代理对象。
blackboard：一个静态的HashMap，用作黑板，将位置映射到带有时间戳的动作实体的列表。
Communication类的构造函数初始化了notFree变量。

setAgents方法用于设置代理对象的映射关系。

addToBlackboard方法向黑板添加动作实体。它根据代理的位置、时间和实体创建一个HashMap，并将其添加到黑板中。

removeFromBlackboard方法从黑板中移除动作实体。它根据代理的位置、时间和实体从黑板中找到相应的HashMap，并将其移除。

positionHasAction方法用于检查给定位置和时间是否存在动作。

printBlackboard方法打印黑板的内容，显示位置、时间和对应的动作实体。

inquire方法用于代理之间的询问和请求。它根据任务的类型（代理移动或代理移动箱子），选择最佳的代理来执行任务，并返回最佳代理执行任务所需的步骤数。如果找到最佳代理，则将任务和计划分配给最佳代理执行。

请注意，提供的代码只是完整实现的一部分，引用了其他类（Agent、Task、State、Plan等）的内容。 */