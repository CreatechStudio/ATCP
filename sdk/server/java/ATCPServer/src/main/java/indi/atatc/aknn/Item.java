package indi.atatc.aknn;

public abstract class Item {
    protected abstract int getDimension();

    /**
     * @param coordinateAxis: 0 presents x, 1 presents y...
     * @return the coordinate value
     */
    public abstract double getCoordinate(int coordinateAxis);
}
