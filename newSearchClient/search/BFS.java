package newSearchClient.search;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import newSearchClient.Agent;
import newSearchClient.Box;
import newSearchClient.Communication;
import newSearchClient.Entity;
import newSearchClient.State;
import newSearchClient.Position;

public class BFS {
    static State state;
    static boolean[][] cellSearched;
    static int[] deltaX = {1,-1,0,0};
    static int[] deltaY = {0,0,1,-1};

    private static void initBFS(State s){
        state = s;
        cellSearched = new boolean[state.numCols][state.numRows];
    }

    //BFS from the blocking position
    //When it hits a spot that is not used, by checking in the blackboard
    public static Position findNonBlockingPosition(State s, Entity e){
        initBFS(s);
        Queue<Position> frontier = new LinkedList<>();
        frontier.add(new Position(e.posX, e.posY));
        cellSearched[e.posX][e.posY] = true;
        while(!frontier.isEmpty()){
            Position cell = frontier.remove();
            int x = cell.x;
            int y = cell.y;

            for(int i = 0; i < 4; i++){
                int newX = x + deltaX[i];
                int newY = y + deltaY[i];
                
                //Based on the assumption that an agent with the same color is either moving away or the one to take the box
                int entityID = state.entityID[newX][newY];
                boolean sameColorAgent = false;
                if(entityID >= 0 && entityID < 10){
                    Agent agent = (Agent) s.entityByID(entityID);
                    sameColorAgent = e.color == agent.color;
                } else if (entityID == -1){
                    sameColorAgent = true;
                }

                if(isValid(newX, newY) && sameColorAgent){ // Agent does not have same color
                    //Check the blackboard
                    boolean occupied = false;
                    //System.err.println("Checking position " + newX + ", " + newY);
                    for(int j = State.time; j < State.time+100; j++) //TODO: Add static int size of blackboard to Communication
                    {
                        if(Communication.positionHasAction(newX, newY, j)){
                            //System.err.println("Occupied at time " + j + " at");
                            occupied = true;
                            break;
                        }
                    }

                    if(occupied){
                        frontier.add(new Position(newX, newY));
                    } else {
                        return new Position(newX, newY);
                    }
                }
            }
        }
        return null;
    }

    public static Box findGrabbableBoxes(State s, Agent agent){
        //System.err.println("AgentPos " + agent.posX + ", "+ agent.posY);
        initBFS(s);
        Queue<Position> frontier = new LinkedList<>();
        frontier.add(new Position(agent.posX, agent.posY));
        cellSearched[agent.posX][agent.posY] = true;

        while(!frontier.isEmpty()){
            Position cell = frontier.remove();
            int x = cell.x;
            int y = cell.y;

            for(int i = 0; i < 4; i++){
                int newX = x + deltaX[i];
                int newY = y + deltaY[i];
                if(isValid(newX, newY)){
                    int entityID = state.entityID[newX][newY];
                    if (entityID >= 10){
                        Box box = state.boxList.get(entityID);
                        //System.err.println("Agent " + agent.character + " found box " + box.character + " has " + box.accessibleGoals.size() + " goals and is onGoal " + box.onGoal);
                        if(box.color == agent.color && box.accessibleGoals.size() > 0 && !box.onGoal){
                            //TODO: Just return the first box found? (closest box)
                            //grabbableBoxes.add(box);
                            return box;
                        } else {
                            frontier.add(new Position(newX, newY)); //Allowed to go through other boxes
                        }
                    } else {
                        frontier.add(new Position(newX, newY));
                    }
                    cellSearched[newX][newY] = true;
                }
            }
        }
        //System.err.println(grabbableBoxes);
        //return grabbableBoxes.toArray(new Box[0]);
        return null;
    }
    private static boolean isValid(int x, int y){
        //System.err.println("("+ x + ", " + y + ")");
        boolean wall = state.staticElements[x][y] != '+';
        boolean isCellSearched = !cellSearched[x][y];
        return  wall && isCellSearched;
    }
}
