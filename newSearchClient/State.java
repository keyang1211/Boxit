package newSearchClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;

public class State {

    public int numRows;
    public int numCols;

    //2D array of all entities ids
    public int[][] entityID;

    public HashMap<Integer, Agent> agentList; // There can only be 10 agents (0-9), hence why this is used as their IDs
    public HashMap<Integer, Box> boxList;
    public ArrayList<Goal> goalList;

    // 2D array holding the goals and the walls
    public char[][] staticElements; // #, 0-9, A-Z

    // Labels all contiguous areas of the map with respective integers/labels
    // Used to distinguish between distinct areas of a map which are not accessible to each other
    public int[][] areaLabels;

    public State parent;
    public String[] jointAction;

    //Global time to keep track of the blackboard and when to search it
    public static int time = 0;
    
    public State(int[][] entityID, 
                HashMap<Integer, Agent> agentList,
                HashMap<Integer, Box> boxList,
                ArrayList<Goal> goalList,
                char[][] staticElements,
                int numRows,
                int numCols,
                int[][] areaLabels
                )
        {
        this.numRows = numRows;
        this.numCols = numCols;
        this.entityID = entityID;
        this.staticElements = staticElements;
        parent = null;
        jointAction = new String[1];
        this.areaLabels = areaLabels;
        this.agentList = agentList;
        this.boxList = boxList;
        this.goalList = goalList;
        initAreaLabels();
    }
    
    // Function which uses BFS to label all distinct areas of the level
    public void bfsLabeling(int xCoord, int yCoord, int labelInt) {
        areaLabels[xCoord][yCoord] = labelInt;
        // Continue BFS on all adjacent squares that have not been labelled
        if ((xCoord+1) <= (numCols-1)) {
            if (areaLabels[xCoord+1][yCoord] == 0){
                bfsLabeling (xCoord+1,yCoord,labelInt);
            }
        }
        if ((yCoord+1) <= (numRows-1)) {
            if (areaLabels[xCoord][yCoord+1] == 0){
                bfsLabeling (xCoord,yCoord+1,labelInt);
            }
        }
        if ((xCoord-1) >= 0) {
            if (areaLabels[xCoord-1][yCoord] == 0){
                bfsLabeling (xCoord-1,yCoord,labelInt);
            }
        }
        if ((yCoord-1) >= 0) {
            if (areaLabels[xCoord][yCoord-1] == 0){
                bfsLabeling (xCoord,yCoord-1,labelInt);
            }
        }
    }

    // Initializes the labelling of the map
    public void initAreaLabels() {
        int labelInt = 0;
        for (int i = 0; i < numCols; i++) {
            for (int j = 0; j < numRows; j++) {
                if (areaLabels[i][j] == 0) {
                    labelInt++;
                    bfsLabeling(i, j, labelInt);
                }
            }
        }
    }

    //Takes boxes of one character and one area
    //Or one agent
    //assigns a list of goals possible
    public void initAccessibleGoals(Entity[] entities, char c, int areaLabel) {
        ArrayList<Goal> accessibleGoals = new ArrayList<>();
        for (int i = 0; i < goalList.size(); i++) {
            int goalCoordX = goalList.get(i).pos.x;
            int goalCoordY = goalList.get(i).pos.y;
            if (areaLabels[goalCoordX][goalCoordY] == areaLabel && 
            staticElements[goalCoordX][goalCoordY] == c){
                accessibleGoals.add(goalList.get(i));
            }
        }
        for(Entity entity : entities){
            entity.accessibleGoals = accessibleGoals;
        }
        // Sorts the goals in the ArrayList
        //Collections.sort(accessibleGoals, new CoordinateComparator(entity.posX, entity.posY));
        /* Testing if accessibleGoals is sorted properly
        for (int[] goalCoords : entity.accessibleGoals) {
            System.err.println("Current goal, x = " + goalCoords[0] + ", y = " + goalCoords[1]);
        }
        */
    }

    @Override
    public String toString() {
        return Arrays.deepToString(entityID);
    }

    //Tests if the coordinate is free 
    // Nothing in entityID
    public boolean isFreeCell(int x, int y){
        if (entityID[x][y] != -1) return false;
        return true;
    }

    // Checks if entity is a box based on location
    public boolean isBox(int x, int y){
        return entityID[x][y] >= 10;
    }

    // Checks if entity is a box based on ID
    public boolean isBox(int id){
        return id >= 10;
    }

    public Entity entityAt(int x, int y){
        int id =  entityID[x][y];
        return entityByID(id);
    }

    public Entity entityByID(int id){
        if (isBox(id)){
            return boxList.get(id);
        } else {
            return agentList.get(id);
        }
    }

    // Moves an entity (agent/box) and ensures consistency between an entity's internal state and the entityID map
    public boolean moveEntity(int oldX, int oldY, int newX, int newY){
        int id = entityID[oldX][oldY];
        Entity e = entityByID(id);
        if(entityID[newX][newY] > -1){
            //Overwriting entity at place
            System.err.println("Overwriting " + entityID[newX][newY]);
            return false;
        }
        if (e == null){
            System.err.println("null entity is " + id + " at: (" + oldX + ", " + oldY + ")");
            return false;
        } 
        entityID[e.posX][e.posY] = -1;
        entityID[newX][newY] = id;
        e.posX = newX;
        e.posY = newY;
        return true;
    }

    // Moves a box BEFORE moving the agent
    public boolean pushBox(int oldAgentX, int oldAgentY, int newAgentX, int newAgentY, int newBoxX, int newBoxY) {
        boolean moveBox = moveEntity(newAgentX, newAgentY, newBoxX, newBoxY);//Move box
        boolean moveAgent = moveEntity(oldAgentX, oldAgentY, newAgentX, newAgentY); //Move agent
        return moveBox && moveAgent;
    }

    // Moves an agent BEFORE moving the box
    public boolean pullBox(int oldBoxX, int oldBoxY, int newBoxX, int newBoxY, int newAgentX, int newAgentY) {
        boolean moveAgent = moveEntity(newBoxX, newBoxY, newAgentX, newAgentY); //Move agent
        boolean moveBox = moveEntity(oldBoxX, oldBoxY, newBoxX, newBoxY); //Move box
        return moveBox && moveAgent;
    }

    public boolean isGoalState(){
        for(Goal goal : goalList){
            int id = entityID[goal.pos.x][goal.pos.y];
            if(id == -1 || entityByID(id).character != goal.character){
                return false;
            }
        }
        return true;
    }
}
