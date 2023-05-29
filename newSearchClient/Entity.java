package newSearchClient;
import java.util.ArrayList;

enum EntityType {
    Agent,
    Block
}

public abstract class Entity {
    public EntityType type;
    public int posX, posY;
    public Color color;
    public char character; // 0-9, A-Z
    // Saves the coordinates of all accessible goals in the same area
    public ArrayList<Goal> accessibleGoals;
    //Used to determine whether it should be added to the search as static or if it is in the blackboard
    public boolean moving;
    public Entity(int posX, int posY, Color color, char id){
        this.posX = posX;
        this.posY = posY;
        this.color = color;
        this.character = id;
        accessibleGoals = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        Entity other = (Entity) o;
        return this.character == other.character;
    }
}