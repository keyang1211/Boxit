package newSearchClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SearchClient {
    public static void main(String[] args) throws IOException{
        // Use stderr to print to the console.
        System.err.println("SearchClient initializing. I am sending this using the error output stream.");

        // Send client name to server.
        System.out.println("SearchClient");

        // Parse the level.
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
        State initialState = Parser.parseLevel(serverMessages);
        
        //System.out.println(initialState);
        //((Agent) initialState.idToEntity.get('0')).push(initialState, 2, 1, 3, 1);

        Runner runner = new Runner(initialState);
        runner.init();
        runner.run(serverMessages);

        
        /*
        System.out.print(initialState.jointAction[0]);
        for (int action = 1; action < initialState.jointAction.length; ++action)
        {
            System.out.print("|");
            System.out.print(initialState.jointAction[action]);
        }
        System.out.println();
        serverMessages.readLine();
        */
        
    }

}
