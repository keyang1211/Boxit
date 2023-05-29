package newSearchClient;

public class Task {
    public Entity entity;
    public int goalX, goalY;
    
    Task(Entity entity, int goalX, int goalY) {
        this.entity = entity;
        this.goalX = goalX;
        this.goalY = goalY;
    }

    public String toString(){
        return entity.toString() + " go to (" + goalX + ", " + goalY + ")";
    }
}
