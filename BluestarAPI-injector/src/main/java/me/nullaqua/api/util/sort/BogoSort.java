package me.nullaqua.api.util.sort;

import java.util.*;

public class BogoSort
{
    public static <T extends Comparable<? super T>> void bogoSort(List<T> list)
    {
        bogoSort(list, Comparator.naturalOrder());
    }

    public static <T> void bogoSort(List<T> list, Comparator<? super T> comparator)
    {
        var random = new Random();
        while (!bogoIsSorted(list, comparator))
        {
            Collections.shuffle(list, random);
        }
    }

    private static <T> boolean bogoIsSorted(List<T> list, Comparator<? super T> comparator)
    {
        for (int i = 1; i < list.size(); i++)
        {
            if (comparator.compare(list.get(i - 1), list.get(i)) > 0) return false;
        }
        return true;
    }

    public static <T extends Comparable<? super T>> void bogobogoSort(List<T> list)
    {
        bogobogoSort(list, Comparator.naturalOrder());
    }

    public static <T> void bogobogoSort(List<T> list, Comparator<? super T> comparator)
    {
        var random = new Random();
        while (!bogobogoIsSorted(list, comparator)) Collections.shuffle(list, random);
    }

    private static <T> boolean bogobogoIsSorted(List<T> list, Comparator<? super T> comparator)
    {
        if (list.size() <= 1) return true;
        var subList = list.subList(0, list.size() - 1);
        bogobogoSort(subList, comparator);
        for (var x: subList)
        {
            if (comparator.compare(x, list.get(list.size() - 1)) > 0) return false;
        }
        return true;
    }


    public static <T extends Comparable<? super T>> void bogoSort(T[] array)
    {
        var list = Arrays.asList(array);
        bogoSort(list, Comparator.naturalOrder());
        list.toArray(array);
    }

    public static <T> void bogoSort(T[] array, Comparator<? super T> comparator)
    {
        var list = Arrays.asList(array);
        bogoSort(list, comparator);
        list.toArray(array);
    }

    public static <T extends Comparable<? super T>> void bogobogoSort(T[] array)
    {
        var list = Arrays.asList(array);
        bogobogoSort(list, Comparator.naturalOrder());
        list.toArray(array);
    }

    public static <T> void bogobogoSort(T[] array, Comparator<? super T> comparator)
    {
        var list = Arrays.asList(array);
        bogobogoSort(list, comparator);
        list.toArray(array);
    }
}
