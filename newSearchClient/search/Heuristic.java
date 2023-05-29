package newSearchClient.search;

import java.util.Comparator;

public abstract class Heuristic<E> implements Comparator<E>{

    public abstract int h(E s);

    public abstract int f(E s);
}
