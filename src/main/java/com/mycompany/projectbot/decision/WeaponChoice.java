package com.mycompany.projectbot.decision;

import com.mycompany.projectbot.enumeration.DistanceRange;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPref;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WeaponChoice {

    private static Integer minDataAmount = 2;

    private static Map<Integer, WeaponPref> possibleWeaponPrefs = new HashMap<Integer, WeaponPref>();
    private static QValue[][] qMatrix;

    public WeaponChoice() {
    }

    public static Map<Integer, WeaponPref> getPossibleWeaponPrefs() {
        return possibleWeaponPrefs;
    }

    public static WeaponPref getWeaponPref(Integer index) {
        return possibleWeaponPrefs.get(index);
    }

    public static Integer getRandomWeaponPrefIndex() {
        Random rand = new Random();
        return rand.nextInt(possibleWeaponPrefs.size());
    }

    public static void setPossibleWeaponPrefs(WeaponPref[] possibleWeaponPrefs) {
        int i = 0;
        for (WeaponPref pref : possibleWeaponPrefs) {
            WeaponChoice.possibleWeaponPrefs.put(i++, pref);
        }
        qMatrix = new QValue[DistanceRange.values().length][possibleWeaponPrefs.length];
        for (int j = 0; j < DistanceRange.values().length; j++) {
            for (int k = 0; k < possibleWeaponPrefs.length; k++) {
                qMatrix[j][k] = new QValue(0, 0);
            }
        }
    }

    public static void updateQMatrix(Integer i, Integer j, double value) {
        qMatrix[i][j].addValue(value);
    }

    public static WeaponPref getBestChoice(Double distance) {
        return WeaponChoice.getWeaponPref(WeaponChoice.getBestChoiceIndex(distance));
    }

    public static Integer getBestChoiceIndex(Double distance) {
        Map<Integer, WeaponPref> possibleWeaponPrefs = getPossibleWeaponPrefs();

        double maxValue = Double.MIN_VALUE;

        DistanceRange distanceRange = DistanceRange.getDistanceRange(distance);
        Integer index = distanceRange.getQMatrixindex();

        Integer bestChoice = getRandomWeaponPrefIndex();
        if (!isMatrixDataEnough(distanceRange)) {
            return bestChoice;
        }

        // Pick to move to the state that has the maximum Q value
        for (int i = 0; i < possibleWeaponPrefs.size(); i++) {
            double value = qMatrix[index][i].getValue();

            if (value > maxValue) {
                maxValue = value;
                bestChoice = i;
            }
        }
        return bestChoice;
    }

    public static Integer getMinDataAmount() {
        return minDataAmount;
    }

    public static void setMinDataAmount(Integer minDataAmount) {
        WeaponChoice.minDataAmount = minDataAmount;
    }

    public static boolean isMatrixDataEnough(DistanceRange distanceRange) {
        Integer index = distanceRange.getQMatrixindex();
        for (int j = 0; j < possibleWeaponPrefs.size(); j++) {
            if (minDataAmount > qMatrix[index][j].getWeight())
                return false;
        }
        return true;
    }
}