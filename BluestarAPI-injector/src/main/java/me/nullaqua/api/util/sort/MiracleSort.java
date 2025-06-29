package me.nullaqua.api.util.sort;

import java.util.Comparator;
import java.util.List;

/**
 * A miracle sort implementation.
 * <p>
 * This sort is guaranteed to sort the list, but it may take a while.
 * <p>
 */
public class MiracleSort
{
    public static <T extends Comparable<? super T>> void miracleSort(List<T> list)
    {
        miracleSort(list, Comparator.naturalOrder());
    }

    public static <T> void miracleSort(List<T> list, Comparator<? super T> comparator)
    {
        //noinspection StatementWithEmptyBody
        while (!miracleIsSorted(list, comparator));
    }

    private static <T> boolean miracleIsSorted(List<T> list, Comparator<? super T> comparator)
    {
        for (int i = 1; i < list.size(); i++)
        {
            if (comparator.compare(list.get(i - 1), list.get(i)) > 0) return false;
        }
        return true;
    }

    public static <T extends Comparable<? super T>> void miracleSort(T[] array)
    {
        miracleSort(array, Comparator.naturalOrder());
    }

    public static <T> void miracleSort(T[] array, Comparator<? super T> comparator)
    {
        //noinspection StatementWithEmptyBody
        while (!miracleIsSorted(array, comparator));
    }

    private static <T> boolean miracleIsSorted(T[] array, Comparator<? super T> comparator)
    {
        for (int i = 1; i < array.length; i++)
        {
            if (comparator.compare(array[i - 1], array[i]) > 0) return false;
        }
        return true;
    }
}
