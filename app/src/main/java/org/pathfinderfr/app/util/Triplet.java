package org.pathfinderfr.app.util;

public class Triplet<TYPE1, TYPE2, TYPE3> {
    public final TYPE1 first;
    public final TYPE2 second;
    public final TYPE3 third;

    /**
     * Constructor for a Triple.
     *
     * @param first  the first object in the triplet
     * @param second the second object in the triplet
     * @param third the third object in the triplet
     */
    public Triplet(TYPE1 first, TYPE2 second, TYPE3 third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}