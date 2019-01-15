package org.pathfinderfr.app.util;

/**
 * This class is a simpler copy of android.util.Pair
 * Easier for unit testing
 */
public class Pair<TYPE1, TYPE2> {
    public final TYPE1 first;
    public final TYPE2 second;

    /**
     * Constructor for a Pair.
     *
     * @param first  the first object in the Pair
     * @param second the second object in the pair
     */
    public Pair(TYPE1 first, TYPE2 second) {
        this.first = first;
        this.second = second;
    }
}