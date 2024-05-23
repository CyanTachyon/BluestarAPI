package me.nullaqua.api.util;

import java.util.*;

public class MultiThreadedSort
{
    /**
     * 多线程归并排序
     */
    public static <T extends Comparable<? super T>> void mergeSort(List<T> list,int maxThread)
    {
        mergeSort(list,Comparator.naturalOrder(),maxThread);
    }

    /**
     * 多线程归并排序
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public static <T> void mergeSort(List<T> list,Comparator<? super T> comparator,int maxThread)
    {
        var array = list.toArray();
        mergeSort(array,(Comparator) comparator,maxThread);
        ListIterator<T> i = list.listIterator();
        for (Object e: array)
        {
            i.next();
            i.set((T) e);
        }
    }

    /**
     * 多线程归并排序
     */
    public static <T extends Comparable<? super T>> void mergeSort(T[] array,int maxThread)
    {
        mergeSort(array,0,array.length,Comparator.naturalOrder(),maxThread);
    }

    /**
     * 多线程归并排序
     */
    public static <T> void mergeSort(T[] array,Comparator<? super T> comparator,int maxThread)
    {
        mergeSort(array,0,array.length,comparator,maxThread);
    }

    /**
     * 多线程归并排序
     */
    private static <T> void mergeSort(T[] array,int start,int end,Comparator<? super T> comparator,int maxThread)
    {
        if (end-start <= 1) return;
        int mid = (start+end)/2;
        if (maxThread > 0)
        {
            var subMaxThread0 = (maxThread-1)>>1;
            var subMaxThread1 = maxThread-1-subMaxThread0;
            var thread = new Thread(()->mergeSort(array,start,mid,comparator,subMaxThread0));
            thread.start();
            mergeSort(array,mid,end,comparator,subMaxThread1);
            try
            {
                thread.join();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            merge(array,start,mid,end,comparator);
        }
        else Arrays.sort(array,start,end,comparator);
    }

    @SuppressWarnings("unchecked")
    private static <T> void merge(T[] array,int start,int mid,int end,Comparator<? super T> comparator)
    {
        var temp = (T[]) new Object[end-start];
        int i = start, j = mid, k = 0;
        while (i < mid && j < end)
        {
            if (comparator.compare(array[i],array[j]) <= 0) temp[k++] = array[i++];
            else temp[k++] = array[j++];
        }
        while (i < mid) temp[k++] = array[i++];
        while (j < end) temp[k++] = array[j++];
        System.arraycopy(temp,0,array,start,end-start);
    }
}
