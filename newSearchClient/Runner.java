package newSearchClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Array;

// Runs makeBigPlan on each agent if they don't have an action / task
// Then makes every agent execute their action resulting in a new state
// Loop
public class Runner {
    State state;
    public Runner(State initialState){
        state = initialState;
    }

    public void init(){
        
        for (Agent agent : state.agentList.values()){
            state.initAccessibleGoals(new Agent[]{agent}, agent.character, state.areaLabels[agent.posX][agent.posY]);
        }
        
        Set<Integer> checkedAreaLabels = new HashSet<>();
        Set<Character> checkedCharacters = new HashSet<>();
        for (Box box : state.boxList.values()){
            int areaLabel = state.areaLabels[box.posX][box.posY];
            char character = box.character;
            if(!(checkedAreaLabels.contains(areaLabel) && checkedCharacters.contains(character))){
                ArrayList<Box> boxes = new ArrayList<>();
                for(Box box2 : state.boxList.values()){
                    if(state.areaLabels[box2.posX][box2.posY] == areaLabel && box2.character == character){
                        boxes.add(box2);
                    }
                }
                state.initAccessibleGoals(boxes.toArray(new Box[0]), character, areaLabel);
                checkedAreaLabels.add(areaLabel);
                checkedCharacters.add(character);
            }
        }

        //Debugging
        for (Map.Entry<Integer, Box> entry : state.boxList.entrySet()){
            Box b = entry.getValue();
            System.err.println("\tbox " + b.character + ", " + entry.getKey() + " : (" + b.posX + ", " + b.posY + ")");
            for(Goal goal : b.accessibleGoals){
                System.err.println("\t\t" + goal);
            }
        }
    }

    public void run(BufferedReader serverMessages) throws IOException{
        while(true){
            int noOpCounter = 0;
            int numOfAgents = state.agentList.size();
            List<AgentPlan> agentPlanSize = new ArrayList<>();
            String[] agentActions = new String[numOfAgents];
            for(int i = numOfAgents - 1; i >= 0; i--){
                Agent agent = state.agentList.get(i);
                if(agent.actions.isEmpty()){
                    agentPlanSize.add(new AgentPlan(i, agent.makeBestPotentialPlan(state)));
                } else {
                    agentPlanSize.add(new AgentPlan(i, 0));
                }
            }
            //Sorts with regard to length of the potential plan. Smallest first
            agentPlanSize.sort(new PlanComparator());
            //System.err.println(agentPlanSize.toString());

            for(AgentPlan agentPlan : agentPlanSize){
                Agent agent = state.agentList.get(agentPlan.agentID);
                if(agent.actions.isEmpty()){
                    agent.makeBigPlan(state);
                } else {
                    //If the agent is already doing something check ahead to see if anything is blocking
                    if(agent.isAgentActionsBlocked(state)){
                        agent.makeBigPlan(state);
                    }
                }
                if(agent.actions.get(0) == "NoOp"){
                    noOpCounter++;
                }
                //Store actions
                boolean didMove = agent.doNextAction(state);
                if(didMove){
                    agentActions[agentPlan.agentID] = agent.actions.get(0) + "@" + agent.actions.remove(0);
                } else{
                    agentActions[agentPlan.agentID] = "NoOp@NoOp";
                }
                    
                
            }
            State.time ++; //Advance time
            //Communication.printBlackboard();
            if(noOpCounter == state.agentList.size()){
                System.err.println("Every agent has NoOp");
                break;
            }
            StringBuilder sb = new StringBuilder();
            for(String s : agentActions){
                sb.append(s);
                sb.append("|");
            }
            sb.deleteCharAt(sb.length()-1);
            System.out.println(sb.toString());
            serverMessages.readLine();
        }
        System.err.println("Client has stopped");
    }
    //Concatenates 2 arrays of the same type and returns the combined array
    public static <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;
    
        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
    
        return c;
    }
}
