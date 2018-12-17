package com.mycompany.projectbot.enumeration;


import java.util.HashMap;
import java.util.Map;

public enum DistanceRange {
    VERY_FAR(0, new Range(Double.valueOf(5000), Double.POSITIVE_INFINITY)),
    FAR(1, new Range(Double.valueOf(3000), Double.valueOf(5000))),
    NOT_TOO_FAR(2, new Range(Double.valueOf(1500), Double.valueOf(3000))),
    CLOSE(3, new Range(Double.valueOf(1000), Double.valueOf(1500))),
    SO_CLOSE(4, new Range(Double.valueOf(0), Double.valueOf(1000)));

    private Integer qMatrixindex;
    private Range range;

    private static Map<Integer, DistanceRange> values = new HashMap<Integer, DistanceRange>();

    static {
        for (DistanceRange distanceRange : DistanceRange.values()) {
            Integer in = distanceRange.getQMatrixindex();
            values.put(in, distanceRange);
        }
    }

    DistanceRange(Integer qMatrixindex, Range range) {
        this.range = range;
        this.qMatrixindex = qMatrixindex;
    }

    public Integer getQMatrixindex() {
        return qMatrixindex;
    }

    public Range getRange() {
        return range;
    }

    public static DistanceRange getDistanceRange(Double distance) {
        for (DistanceRange enumeration : DistanceRange.values()) {
            if (distance > enumeration.getRange().getMinDistance() && distance < enumeration.getRange().getMaxDistance()) {
                return enumeration;
            }
        }
        return null;
    }

    public DistanceRange[] getValue(Integer qMatrixindex) {
        return DistanceRange.values();
    }

    public DistanceRange[] getValues() {
        return DistanceRange.values();
    }
}

class Range {
    private Double minDistance;
    private Double maxDistance;

    public Range(Double minDistance, Double maxDistance) {
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
    }

    public Double getMinDistance() {
        return minDistance;
    }

    public Double getMaxDistance() {
        return maxDistance;
    }
}
