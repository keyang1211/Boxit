package newSearchClient;

public class Position {
    public int x, y;
    public Position(int x, int y){
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString(){
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null){
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o instanceof Position) {
            Position p = (Position) o;
            return p.x == this.x && p.y == this.y;
        }
        return false;
    }
}
