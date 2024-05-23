package me.nullaqua.api.kotlin.utils

import me.nullaqua.api.util.MultiThreadedSort

private fun getCoreCount(): Int =
    Runtime.getRuntime().availableProcessors()
fun <T: Comparable<T>> MutableList<T>.multiThreadedSort(threadCount: Int = getCoreCount() - 1) =
    MultiThreadedSort.mergeSort(this, threadCount)
fun <T: Comparable<T>> Array<T>.multiThreadedSort(threadCount: Int = getCoreCount() - 1) =
    MultiThreadedSort.mergeSort(this, threadCount)
fun <T: Comparable<T>> Iterable<T>.multiThreadedSorted(threadCount: Int = getCoreCount() - 1) =
    toMutableList().apply { MultiThreadedSort.mergeSort(this, threadCount) }
fun <T: Comparable<T>> Sequence<T>.multiThreadedSorted(threadCount: Int = getCoreCount() - 1) =
    toMutableList().apply { MultiThreadedSort.mergeSort(this, threadCount) }