package newSearchClient;

import java.util.ArrayList;
import java.util.Arrays;

import newSearchClient.search.BFS;
import newSearchClient.search.agent.AgentState;
import newSearchClient.search.agent.SearchAgent;
import newSearchClient.search.box.AgentBoxState;
import newSearchClient.search.box.SearchBox;
import java.util.LinkedList;
import java.util.List;

public class Agent extends Entity{
    public ArrayList<String> actions;
    public LinkedList<Plan> plans;
    public Communication comm;
    private Box movingBox;

    public Agent(int x, int y, Color color, char id, Communication comm) {
        super(x, y, color, id);
        this.plans = new LinkedList<>();
        this.comm = comm;
        this.actions = new ArrayList<>();
        this.type = EntityType.Agent;
    }
    
    public boolean doNextAction(State s){
        String actionName = actions.get(0);
        Action action = Action.getAction(actionName);
        boolean didMove = false;
        switch (action.type){
            case NoOp:
                didMove = true;
                break;
            case Move:
                didMove = move(s, action.agentXDelta, action.agentYDelta);
                break;
            case Pull:
                didMove = pull(s, action.agentXDelta, action.agentYDelta, action.boxXDelta, action.boxYDelta);
                break;
            case Push:
                didMove = push(s, action.agentXDelta, action.agentYDelta, action.boxXDelta, action.boxYDelta);
                break;
        }
        if(!didMove){
            this.moving = false;
            if(movingBox != null){
                movingBox.moving = false;
                movingBox = null;
            }
            actions = new ArrayList<String>();
            makeBigPlan(s);
            return doNextAction(s);
        }

        if(actions.size() == 1){
            this.moving = false;
            if(movingBox != null){
                movingBox.moving = false;
                movingBox = null;
            }
        }

        return didMove;
    }

    public char[][] makeStaticElements(State state){
        char[][] newStaticElements = state.staticElements;
        for (Agent agent : state.agentList.values()){
            newStaticElements[agent.posX][agent.posY] = agent.character;
        }
        for (Box box : state.boxList.values()){
            newStaticElements[box.posX][box.posY] = box.character;
        }
        return newStaticElements;
    }

    private Box chooseBox(State s){
        Box box = BFS.findGrabbableBoxes(s, this);
        return box;
    }

    private Position chooseGoal(State s, Box b){
        Goal closestGoal = null;
        int closestDist = 500;

        for(int i = 0; i < b.accessibleGoals.size(); i++){
            Goal goal = b.accessibleGoals.get(i);
            int idAtGoal = s.entityID[goal.pos.x][goal.pos.y];
            if(!(idAtGoal == -1 || s.entityByID(idAtGoal).character != goal.character)){
                //If the goal is satisfied
                continue;
            }
            int dist = Math.abs(b.posX - goal.pos.x) + Math.abs(b.posY - goal.pos.y);
            if(dist < closestDist){
                closestDist = dist;
                closestGoal = goal;
            }
        }
        if(closestGoal == null){
            System.err.println("no goal for " + b);
            return null;
        }
        return closestGoal.pos;
    }

    private Task chooseNextTask(State s){
        Box box = chooseBox(s);
        if (box != null && box.accessibleGoals.size() > 0){
            Position goal = chooseGoal(s, box);
            if(goal != null){
                comm.notFree.put(character, this);
                return new Task (box, goal.x, goal.y);
            }
        }

        if(accessibleGoals.size() > 0) {
            Goal goal = accessibleGoals.get(0); 
            comm.notFree.put(character, this);
            return new Task(this, goal.pos.x, goal.pos.y);
        } else {
            //Agent is free
            comm.notFree.remove(character);
            return null;
        }
        
    }

    public int makeBestPotentialPlan(State s){
        if(plans.size() > 0){
            return new ArrayList<>(Arrays.asList(plans.get(0).actions)).size();
        }
        Task task = chooseNextTask(s);
        if(task == null){
            return 1000; // Low priority when doing nothing
        }
        if(task.entity.type == EntityType.Block){
            movingBox = (Box) task.entity;
        }

        AgentState finalState = makePlan(s, task.goalX, task.goalY, false);
        if(finalState != null){
            return finalState.getPlan().length;
        } else {
            finalState = makePlan(s, task.goalX, task.goalY, true);

            if(finalState == null){
                return 1000; // Low priority when doing nothing
            }
            return 0;
            //High priority because of blocking elements
        }
    }

    private void executeFirstPlan(){
        Plan plan = plans.removeFirst();
        actions = new ArrayList<>(Arrays.asList(plan.actions));
        this.moving = true;
        if(plan.movingBox != null){
            movingBox = plan.movingBox;
            movingBox.moving = true;
            movingBox.onGoal = false;
            addActionsToBlackboard();
        } else {
            addActionsToBlackboard();
        }
        System.err.println("Requested task is " + actions);
    }

    private void requestSelf(Task requestTask, State s){
        removeActionsToBlackboard();
        
        Plan plan = evaluateTask(requestTask, s);
        if(plan != null){
            acceptPlan(plan);
        }
        addActionsToBlackboard();
    }

    private int requestFirstBlockingID(State s, int blockingID, int time){
        Entity blocking = s.entityByID(blockingID);
        System.err.println("BlockingID: " + blockingID + " at time " + time + " : " + blocking);
        Position nonBlockingPos = BFS.findNonBlockingPosition(s, blocking);
        Task requestTask = new Task(blocking, nonBlockingPos.x, nonBlockingPos.y);
        if(blocking.type == EntityType.Block && blocking.color == this.color){
            requestSelf(requestTask, s);
            return 0;
        }

        System.err.println("Agent " + character + " requests " + requestTask);
        int requestTime = makeRequest(requestTask, s);
        //No agent could fulfill the request
        if(requestTime == -1){
            return -1;
        }
        int waitTime = requestTime - time;
        
        return waitTime;
    }

    private int requestFromBlockingIDs(State s, int[] blockingIDs){
        int longestWaitTime = 0;
        for(int i = 0; i < blockingIDs.length; i++) {
            
            if(blockingIDs[i] > -1){
                Entity blocking = s.entityByID(blockingIDs[i]);
                System.err.println("BlockingID: " + blockingIDs[i] + " at time " + i + ": " + blocking);
                Position nonBlockingPos = BFS.findNonBlockingPosition(s, blocking);

                Task requestTask = new Task(blocking, nonBlockingPos.x, nonBlockingPos.y);

                if(blocking.type == EntityType.Block && blocking.color == this.color){
                    requestSelf(requestTask, s);
                    continue;
                }
                
                System.err.println("Agent " + character + " requests " + requestTask);
                int waitTime = makeRequest(requestTask, s) - i;
                if(longestWaitTime < waitTime) longestWaitTime = waitTime;
            }
        }
        return longestWaitTime;
    }

    public void makeBigPlan(State s){
        if(plans.size() > 0){
            executeFirstPlan();
            return;
        }

        Task task = chooseNextTask(s);
        if(task == null){
            actions.add("NoOp");
            return;
        }
        System.err.println(this.toString() + " plans to move " + task.entity.character + "(" + task.entity.posX + ", " + task.entity.posY + ")" + " to (" + task.goalX + ", " + task.goalY + ") at time: " + State.time);
        this.moving = true;
        if(task.entity.type == EntityType.Block){
            movingBox = (Box) task.entity;
            movingBox.moving = true;
        }

        AgentState finalState = makePlan(s, task.goalX, task.goalY, false);
        // If an agent cannot find a plan to get to its goal, replan
        if (finalState != null){
            actions = new ArrayList<>(Arrays.asList(finalState.getPlan()));
            addActionsToBlackboard();
            //Box can only be assumed onGoal if the agent actually makes actions Default is {"NoOp"} therefore larger than 1
            if(actions.size() > 1 && movingBox != null){
                movingBox.onGoal = true;
            }
        } else {
            //Replan by going through static boxes and agents
            System.err.println("Agent " + this.character + " is replanning");
            //System.err.println(Arrays.deepToString(s.entityID));
            finalState = makePlan(s, task.goalX, task.goalY, true);

            if(finalState == null){
                stopMoving();
                return;
            }
            actions = new ArrayList<>(Arrays.asList(finalState.getPlan()));
            addActionsToBlackboard();

            //Request other agents to move themselves or boxes
            int[] blockingIDs = finalState.getBlockingIDs();
            int firstBlockingID = -1;
            int index = 0;
            for(; index < blockingIDs.length; index++){
                if(blockingIDs[index] > -1){
                    firstBlockingID = blockingIDs[index];
                    break;
                }
            }
            
            System.err.println("Blocking IDs: " + Arrays.toString(blockingIDs));
            int longestWaitTime = requestFirstBlockingID(s, firstBlockingID, index);

            removeActionsToBlackboard();
            // No agent was able to fulfill the request
            if(longestWaitTime == -1){
                stopMoving();
                return;
            }
            if(plans.size() > 0){
                executeFirstPlan();
                return;
            }
            //Wait until the requested task is finished
            for(int j = 0; j < longestWaitTime; j++){
                actions.add(0, "NoOp");
            }

            int actionsSize = actions.size();
            //Only do the actions up till the first blocking entity
            for(int i = longestWaitTime + index; i < actionsSize; i++){
                actions.remove(longestWaitTime + index); //TODO: longestWaitTime = -1 no agent can take the request not handled
            }

            addActionsToBlackboard(); //Needs to be added after the NoOps have been added
            System.err.println("Plan for agent " + character  + ": " + actions);
        }
        
        //Needed when the agent is already on top of its goal and therefore gives no actions
        if(actions.size() == 0){ 
            this.moving = false;
            actions.add("NoOp");
            return;
        }
        
        //System.err.println("Actions: " + actions.toString());
    }

    private void stopMoving(){
        this.moving = false;
        if(movingBox != null){
            movingBox.moving = false;
            movingBox.onGoal = false;
            movingBox = null;
        }
        actions = new ArrayList<String>();
        actions.add("NoOp");
    }

    //Checks if the agents next x moves are blocked
    public boolean isAgentActionsBlocked(State s){
        Position agentPos = new Position(this.posX, this.posY);
        Position boxPos = null;
        if(movingBox != null){
            boxPos = new Position(movingBox.posX, movingBox.posY);
        }
        boolean blocked = false;
        int numOfActionsToCheck = Math.min(5, actions.size());
        for(int i = 0; i < numOfActionsToCheck; i++){
            Action action = Action.getAction(actions.get(i));
            if((action.type == ActionType.Pull || action.type == ActionType.Push) && movingBox != null){
                simulateBoxAction(action, agentPos, boxPos);
                blocked = !s.isFreeCell(boxPos.x, boxPos.y);
                // if(blocked){
                //     System.err.println(movingBox + " is blocked by " + s.entityID[boxPos.x][boxPos.y]);
                // }
            } else {
                simulateAction(action, agentPos);
            }
            blocked = blocked && !s.isFreeCell(agentPos.x, agentPos.y);
            if(blocked){
                //System.err.println(this + " is blocked by " + s.entityID[agentPos.x][agentPos.y]);
                break;
            }
        }

        if(blocked){
            removeActionsToBlackboard();
            if(movingBox != null){
                movingBox.moving = false;
                movingBox.onGoal = false;
                movingBox = null;
            }
            moving = false;
            actions = new ArrayList<>();
        }
        return blocked;
    }

    private void addActionsToBlackboard(){
        if(movingBox != null){
            addBoxActionsToBlackboard();
        } else {
            addAgentActionsToBlackboard();
        }
    }
    
    private void removeActionsToBlackboard(){
        if(movingBox != null){
            removeBoxActionsInBlackboard();
        } else {
            removeAgentActionsInBlackboard();
        }
    }

    //Add all actions to the blackboard. Only used for the agent
    private void addAgentActionsToBlackboard(){
        Position agentPos = new Position(this.posX, this.posY);
        for (int time = 0; time < actions.size(); time++) {
            //System.err.println("Position being added = " + agentPos + " with " + actions.get(time) + " at time " + (time + State.time));
            Action action = Action.getAction(actions.get(time));
            Communication.addToBlackboard(agentPos, this, time);
            simulateAction(action, agentPos);
            Communication.addToBlackboard(agentPos, this, time);
        }
    }

    //Remove all actions from the blackboard. Only used for the agent
    private void removeAgentActionsInBlackboard(){
        Position agentPos = new Position(this.posX, this.posY);
        for (int time = 0; time < actions.size(); time++) {
            //System.err.println("Position being removed = " + agentPos + " with " + actions.get(time) + " at time " + (time + State.time));
            Action action = Action.getAction(actions.get(time));
            Communication.removeFromBlackboard(agentPos, this, time);
            simulateAction(action, agentPos);
            Communication.removeFromBlackboard(agentPos, this, time);
        }
    }
    
    //Add all actions to the blackboard. Only used for agent with a box
    private void addBoxActionsToBlackboard(){
        Position agentPos = new Position(this.posX, this.posY);
        Position boxPos = new Position(movingBox.posX, movingBox.posY);
        int time = 0;
        for (; time < actions.size(); time++) {
            //System.err.println("Position being added = " + agentPos + " with " + actions.get(time) + " at time " + (time + State.time));
            Action action = Action.getAction(actions.get(time));
            Communication.addToBlackboard(agentPos, this, time);
            Communication.addToBlackboard(boxPos, movingBox, time);
            simulateBoxAction(action, agentPos, boxPos);
            Communication.addToBlackboard(agentPos, this, time);
            //Only add the box with new position if it was moved by the action
            if(action.type == ActionType.Pull || action.type == ActionType.Push){
                Communication.addToBlackboard(boxPos, movingBox, time);
            }
        }
    }

    private void removeBoxActionsInBlackboard(){
        Position agentPos = new Position(this.posX, this.posY);
        Position boxPos = new Position(movingBox.posX, movingBox.posY);
        for (int time = 0; time < actions.size(); time++) {
            //System.err.println("Position being added = " + agentPos + " with " + actions.get(time) + " at time " + (time + State.time));
            Action action = Action.getAction(actions.get(time));
            Communication.removeFromBlackboard(agentPos, this, time);
            Communication.removeFromBlackboard(boxPos, movingBox, time);
            simulateBoxAction(action, agentPos, boxPos);
            Communication.removeFromBlackboard(agentPos, this, time);
            //Only add the box with new position if it was moved by the action
            if(action.type == ActionType.Pull || action.type == ActionType.Push){
                Communication.removeFromBlackboard(boxPos, movingBox, time);
            }
            
        }
    }
    
    // Provided an action and a positional array, simulates the coordinate translations of an entity
    private void simulateAction(Action action, Position pos){
        switch (action.type){
            case NoOp:
                break;
            case Move:
                pos.x += action.agentXDelta;
                pos.y += action.agentYDelta;
                break;
            case Pull:
                
                break;
            case Push:
                
                break;
        }
    }

    private void simulateBoxAction(Action action, Position pos, Position boxPos){
        switch (action.type){
            case NoOp:
                break;
            case Move:
                pos.x += action.agentXDelta;
                pos.y += action.agentYDelta;
                break;
            case Pull:
                pos.x += action.agentXDelta;
                pos.y += action.agentYDelta;
                boxPos.x += action.boxXDelta;
                boxPos.y += action.boxYDelta;
                break;
            case Push:
                pos.x += action.agentXDelta;
                pos.y += action.agentYDelta;
                boxPos.x += action.boxXDelta;
                boxPos.y += action.boxYDelta;
                break;
        }
    }

    private int[][] makeNewEntityID(State s, int[][] entityID){
        int[][] newEntityID = new int[entityID.length][entityID[0].length];
        for(int i = 0; i < newEntityID.length; i++){
            for(int j = 0; j < newEntityID[i].length; j++){
                newEntityID[i][j] = entityID[i][j];
            }
        }
        //Remove agent from static entityID if it is moving
        for(Agent agent : s.agentList.values()){
            if(agent.moving){
                newEntityID[agent.posX][agent.posY] = -1;
            }
        }
        //Remove box from static entityID if it is moving
        for(Box box : s.boxList.values()){
            if(box.moving){
                newEntityID[box.posX][box.posY] = -1;
            }
        }

        return newEntityID;
    }

    public AgentState makePlan(State s, int goalX, int goalY, boolean noCollision){
        if(movingBox != null){
            return makeBoxPlan(s, movingBox, goalX, goalY, noCollision);
        } else {
            return makeAgentPlan(s, goalX, goalY, noCollision);
        }
    }

    public AgentState makeAgentPlan(State s, int goalX, int goalY, boolean noCollision){
        //Remove agent from entityID array else it will collide with itself
        int[][] newEntityID = makeNewEntityID(s, s.entityID);
        newEntityID[this.posX][this.posY] = -1;
        AgentState finalState = SearchAgent.startSearch(s.staticElements, newEntityID, this, goalX, goalY, noCollision);
        return finalState;        
    }
    
    public AgentBoxState makeBoxPlan(State s, Box box, int goalX, int goalY, boolean noCollision){
        //Remove box and agent from entityID array else it will collide with itself
        int[][] newEntityID = makeNewEntityID(s, s.entityID);
        newEntityID[this.posX][this.posY] = -1;
        newEntityID[box.posX][box.posY] = -1;
        //System.err.println(Arrays.deepToString(newEntityID));
        return SearchBox.startSearch(s.staticElements, newEntityID, this, box, goalX, goalY, noCollision);
    }

    private boolean move(State s, int deltaX, int deltaY){
        return s.moveEntity(posX, posY, posX+deltaX, posY + deltaY);
    }

    private boolean pull(State s, int agentDeltaX, int agentDeltaY, int boxDeltaX, int boxDeltaY){
        int boxX = posX - boxDeltaX, boxY = posY - boxDeltaY;
        int newAgentX = posX + agentDeltaX, newAgentY = posY + agentDeltaY;
        return s.pullBox(boxX, boxY, this.posX, this.posY, newAgentX, newAgentY);
    }

    private boolean push(State s, int agentDeltaX, int agentDeltaY, int boxDeltaX, int boxDeltaY){
        int newAgentX = posX + agentDeltaX, newAgentY = posY + agentDeltaY;
        int newBoxX = newAgentX + boxDeltaX, newBoxY = newAgentY + boxDeltaY;
        return s.pushBox(posX, posY, newAgentX, newAgentY, newBoxX, newBoxY);
    }

    //Move box out of the way

    public int makeRequest(Task task, State s) {
        return comm.inquire(this, task, s);
    }

    // returns value on how efficient it can complete the task
    public Plan evaluateTask(Task task, State s) {
        // task is for agent to move itself
        AgentState finalState;
        Box box = null;
        if (task.entity.character == this.character) {
            finalState = makeAgentPlan(s, task.goalX, task.goalY, false);
        } else {
            // task is for agent to move box
            box = (Box) task.entity;
            finalState = makeBoxPlan(s, box, task.goalX, task.goalY, false);
        }
        if(finalState == null){
            return null;
        }
        return new Plan(finalState.getPlan(), box);
    }

    public void acceptPlan(Plan plan) {
        plans.add(plan);
    }

    @Override
    public String toString(){
        return "Agent " + this.character + " at (" + posX + ", " + posY + ")";
    }
    /*
    private boolean canMove(State state, int newX, int newY) {
        return state.isFreeCell(newX, newY);
    }
    
    private Box returnBox(State state, int x, int y){
        Entity entity = state.entityAt(x, y);
        System.err.println("Entity: " + entity);
        if (entity == null || entity.getClass() != Box.class)
            return null;
        return (Box) entity;
    }

    // Checks if agent and box have the same color and if there is space enough to push
    private boolean canPush(State state, Box box, int boxNewX, int boxNewY) {
        if (canMove(state, boxNewX, boxNewY) && isNeighbors(this.posX, this.posY, box.posX, box.posY)) {
            if (this.color == box.color) {
                return true;
            }
        }
        return false;
    }

    // Checks if agent and box have the same color and if there is space enough to pull
    private boolean canPull(State state, Box box, int agentNewX, int agentNewY) {        
        if (canMove(state, agentNewX, agentNewY) && isNeighbors(this.posX, this.posY, box.posX, box.posY)) {
            if (this.color == box.color) {
                return true;
            }
        }
        return false;
    }

    private String getDirection(int newX, int newY){
        if(newY == 1){
            return "N";
        } else if(newY == -1){
            return "S";
        } else if (newX == 1){
            return "E";
        } else if (newX == -1){
            return "W";
        }
        System.err.println("null direction, newX = " + newX + ", newY = " + newY);
        return null;
    }

    private boolean isNeighbors(int posX1, int posY1, int posX2, int posY2){
        int xDif = Math.abs(posX1 - posX2);
        int yDif = Math.abs(posY1 - posY2);
        if(xDif == 1 && yDif == 0){
            return true;
        } else if(yDif == 1 && xDif == 0){
            return true;
        }
        return false;
    }

    public void move(State state, int newX, int newY){
        if (canMove(state, newX, newY)) {
            String agentDirection = getDirection(newX - this.posX, newY - this.posY);
            state.moveEntity(this.posX, this.posY, newX, newY);
            state.jointAction[0] = "Move(" + agentDirection + ")";
        }
    }

    public void push(State state, int agentNewX, int agentNewY, int boxNewX, int boxNewY) {
        // Check if agent is actually trying to push a box
        Box box = returnBox(state, agentNewX, agentNewY);
        if (box == null) return; //Throw exception??

        if (canPush(state, box, boxNewX, boxNewY)) {
            String agentDirection = getDirection(agentNewX - this.posX, agentNewY - this.posY);
            String boxDirection = getDirection(boxNewX - agentNewX, boxNewY - agentNewY);
            state.jointAction[0] = "Push(" + agentDirection + "," + boxDirection + ")";

            state.pushBox(posX, posY, agentNewX, agentNewY, boxNewX, boxNewY);
        }
    }

    // boxX and boxY are the box's current coordinates BEFORE the pull action
    public void pull(State state, int boxX, int boxY, int agentNewX, int agentNewY) {
        // Check if agent is actually trying to pull a box
        Box box = returnBox(state, boxX, boxY);
        if (box == null) return; //Throw exception??

        if (canPull(state, box, agentNewX, agentNewY)) {
            String agentDirection = getDirection(agentNewX - this.posX, agentNewY - this.posY);
            String boxDirection = getDirection(this.posX - boxX, this.posY - boxY);
            state.jointAction[0] = "Pull(" + agentDirection + "," + boxDirection + ")";

            state.pullBox(boxX, boxY, this.posX, this.posY, agentNewX, agentNewY);
        }
    }
    */
}