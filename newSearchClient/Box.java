package newSearchClient;

public class Box extends Entity{
    public boolean onGoal;
    public Box(int x, int y,Color color, char id) {
        super(x, y, color, id);
        this.type = EntityType.Block;
    }
    @Override
    public String toString(){
        return "Box " + character + ": (" + posX + ", " + posY + ")";
    }
}