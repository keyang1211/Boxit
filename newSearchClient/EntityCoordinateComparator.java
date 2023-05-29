package newSearchClient;

import java.util.Comparator;

public class EntityCoordinateComparator implements Comparator<Entity> {

    int entityX;
    int entityY;

    EntityCoordinateComparator(int entityX, int entityY) {
        this.entityX = entityX;
        this.entityY = entityY;
    }

    @Override
    public int compare(Entity e1, Entity e2) {
        double distCoord1 = Math.sqrt(Math.pow(entityX-e1.posX, 2)+Math.pow(entityY-e1.posY, 2));
        double distCoord2 = Math.sqrt(Math.pow(entityX-e2.posX, 2)+Math.pow(entityY-e2.posY, 2));

        if (distCoord1 < distCoord2) {
            return -1;
        } else if (distCoord1 > distCoord2) {
            return 1;
        } else {
            return 0;
        }
    }
}