package indi.atatc.aknn;

import java.util.Arrays;

public final class AKNN<T extends Item> {

    private Item[] items;

    @SafeVarargs
    public final void importObjects(T... items) {
        this.items = items;
    }

    @SafeVarargs
    public final void addObjects(T... items) {
        this.items = Arrays.copyOf(this.items, this.items.length + items.length);//数组扩容
        System.arraycopy(items, 0, this.items, this.items.length, items.length);
    }

    private static double measureDistance(Item a, Item b) {
        double d = 0;
        int dimension = a.getDimension();
        if (a.getDimension() > b.getDimension()) {
            dimension = b.getDimension();
        }
        for (int i = 0; i < dimension; i++) {
            d += Math.pow(Math.abs(a.getCoordinate(i) - b.getCoordinate(i)), 2);
        }
        return Math.sqrt(d);
    }

    @SuppressWarnings("unchecked")
    public T getClosetTo(T target) {
        Item item = items[0];
        double minimum = measureDistance(items[0], target);
        for (Item i: items) {
            double d = measureDistance(i, target);
            if (d < minimum) {
                minimum = d;
                item = i;
            }
        }
        return (T) item;
    }
}
