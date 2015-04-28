package com.soutvoid.ProjectSozy;

import java.util.Comparator;

/**
 * Created by andrew on 12.04.15.
 */
public class ComparatorDelays implements Comparator<Integer[]> {

    //нужен для сортировки массива, содержащего времена задержки

    @Override
    public int compare(Integer[] obj1, Integer[] obj2) {
        if (obj1[1] > obj2[1])
            return 1;
        else if (obj1[1] < obj2[1])
            return -1;
        else return 0;
    }
}
