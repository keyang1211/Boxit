package newSearchClient;

public class Goal {
    Position pos;
    char character;
    public Goal(Position pos, char character){
        this.pos = pos;
        this.character = character;
    }

    public String toString(){
        return "Goal " + this.character + " at " + this.pos;
    }
}
