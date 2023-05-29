package newSearchClient;

import java.util.Comparator;

public class CoordinateComparator implements Comparator<int[]> {

    int entityX;
    int entityY;

    CoordinateComparator(int entityX, int entityY) {
        this.entityX = entityX;
        this.entityY = entityY;
    }

    @Override
    public int compare(int[] coord1, int[] coord2) {
        double distCoord1 = Math.sqrt(Math.pow(entityX-coord1[0], 2)+Math.pow(entityY-coord1[1], 2));
        double distCoord2 = Math.sqrt(Math.pow(entityX-coord2[0], 2)+Math.pow(entityY-coord2[1], 2));

        if (distCoord1 < distCoord2) {
            return -1;
        } else if (distCoord1 > distCoord2) {
            return 1;
        } else {
            return 0;
        }
    }
}