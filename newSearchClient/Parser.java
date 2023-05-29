package newSearchClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Parser {
    public static State parseLevel(BufferedReader serverMessages)
    throws IOException
    {
        // We can assume that the level file is conforming to specification, since the server verifies this.
        // Read domain
        serverMessages.readLine(); // #domain
        serverMessages.readLine(); // hospital

        // Read Level name
        serverMessages.readLine(); // #levelname
        serverMessages.readLine(); // <name>

        // Read colors
        serverMessages.readLine(); // #colors
        Color[] agentColors = new Color[10];
        Color[] boxColors = new Color[26];
        String line = serverMessages.readLine();
        while (!line.startsWith("#"))
        {
            String[] split = line.split(":");
            Color color = Color.fromString(split[0].trim());
            String[] entities = split[1].split(",");
            for (String entity : entities)
            {
                char c = entity.trim().charAt(0);
                if ('0' <= c && c <= '9')
                {
                    agentColors[c - '0'] = color;
                }
                else if ('A' <= c && c <= 'Z')
                {
                    boxColors[c - 'A'] = color;
                }
            }
            line = serverMessages.readLine();
        }

        // Read initial state
        // line is currently "#initial"
        int numRows = 0;
        int numCols = 0;

        int[][] areaLabels;

        ArrayList<String> levelLines = new ArrayList<>(64);
        line = serverMessages.readLine();
        while (!line.startsWith("#"))
            {
                levelLines.add(line);
                numCols = Math.max(numCols, line.length());
                ++numRows;
                line = serverMessages.readLine();
        }

        areaLabels = new int[numCols][numRows];

        int[][] entityID = new int[numCols][numRows];
        for (int i = 0; i < entityID.length; i++){
            for(int j = 0; j < entityID[i].length; j++){
                entityID[i][j] = -1;
            }
        }

        ArrayList<Goal> goalList = new ArrayList<>(); // Keeps coordinates of all goals
        HashMap<Integer, Agent> agentList = new HashMap<>(); // There can only be 10 agents (0-9), hence why this is used as their IDs
        HashMap<Integer, Box> boxList = new HashMap<>();
        HashMap<Character, Entity> idToEntity = new HashMap<>();
        Communication comm = new Communication();

        // 2D array holding the goals and the walls
        char[][] staticElements = new char[numCols][numRows]; // #, 0-9, A-Z

        int boxID = 10;

        for (int row = 0; row < numRows; ++row)
        {
            line = levelLines.get(row);
            for (int col = 0; col < line.length(); ++col)
            {
                char c = line.charAt(col);

                if ('0' <= c && c <= '9')
                {
                    Agent agent = new Agent(col, row, agentColors[c - '0'], c, comm);
                    idToEntity.put(c, agent);
                    agentList.put((c - '0'), agent);
                    entityID[col][row] = (c - '0');
                }
                else if ('A' <= c && c <= 'Z')
                {
                    Box box = new Box(col, row, boxColors[c - 'A'], c);
                    boxList.put(boxID, box);
                    entityID[col][row] = boxID;
                    boxID++;
                }
                else if (c == '+')
                {
                    staticElements[col][row] = c;
                    // Dummy value set at every wall placement
                    areaLabels[col][row] = -1;
                }
            }
        }
        line = serverMessages.readLine();
        int row = 0;
        while (!line.startsWith("#"))
        {
            for (int col = 0; col < line.length(); ++col)
            {
                char c = line.charAt(col);

                if (('0' <= c && c <= '9') || ('A' <= c && c <= 'Z'))
                {
                    staticElements[col][row] = c;
                    goalList.add(new Goal(new Position(col, row), c));
                    int id = entityID[col][row];
                    if(id > 10 && boxList.get(id).character == c){
                        boxList.get(id).onGoal = true;
                        System.err.println(boxList.get(id) + " is on goal");
                    }
                }
            }

            ++row;
            line = serverMessages.readLine();
        }

        comm.setAgents(agentList);
        
        // End
        // line is currently "#end"

        return new State(entityID, agentList, boxList, goalList, staticElements, numRows, numCols, areaLabels);
    }
}
