package me.nullaqua.api.kotlin.utils

import me.nullaqua.api.util.sort.BogoSort
import me.nullaqua.api.util.sort.MiracleSort

/// Multi-threaded sorting

typealias Comparator<T> = (T, T) -> Int

/// Bogo sorting

@UselessSortingAPI fun <T: Comparable<T>> MutableList<T>.bogoSort() = BogoSort.bogoSort(this)
@UselessSortingAPI fun <T: Comparable<T>> Array<T>.bogoSort() = BogoSort.bogoSort(this)
@UselessSortingAPI fun <T: Comparable<T>> Iterable<T>.bogoSorted() = toMutableList().apply { BogoSort.bogoSort(this) }
@UselessSortingAPI fun <T: Comparable<T>> Sequence<T>.bogoSorted() = toMutableList().apply { BogoSort.bogoSort(this) }

@UselessSortingAPI fun <T: Comparable<T>> MutableList<T>.bogoSort(comparator: Comparator<T>) = BogoSort.bogoSort(this, comparator)
@UselessSortingAPI fun <T: Comparable<T>> Array<T>.bogoSort(comparator: Comparator<T>) = BogoSort.bogoSort(this, comparator)
@UselessSortingAPI fun <T: Comparable<T>> Iterable<T>.bogoSorted(comparator: Comparator<T>) = toMutableList().apply { BogoSort.bogoSort(this, comparator) }
@UselessSortingAPI fun <T: Comparable<T>> Sequence<T>.bogoSorted(comparator: Comparator<T>) = toMutableList().apply { BogoSort.bogoSort(this, comparator) }

/// Bogo-bogo sorting

@UselessSortingAPI fun <T: Comparable<T>> MutableList<T>.bogobogoSort() = BogoSort.bogobogoSort(this)
@UselessSortingAPI fun <T: Comparable<T>> Array<T>.bogobogoSort() = BogoSort.bogobogoSort(this)
@UselessSortingAPI fun <T: Comparable<T>> Iterable<T>.bogobogoSorted() = toMutableList().apply { BogoSort.bogobogoSort(this) }
@UselessSortingAPI fun <T: Comparable<T>> Sequence<T>.bogobogoSorted() = toMutableList().apply { BogoSort.bogobogoSort(this) }

@UselessSortingAPI fun <T: Comparable<T>> MutableList<T>.bogobogoSort(comparator: Comparator<T>) = BogoSort.bogobogoSort(this, comparator)
@UselessSortingAPI fun <T: Comparable<T>> Array<T>.bogobogoSort(comparator: Comparator<T>) = BogoSort.bogobogoSort(this, comparator)
@UselessSortingAPI fun <T: Comparable<T>> Iterable<T>.bogobogoSorted(comparator: Comparator<T>) = toMutableList().apply { BogoSort.bogobogoSort(this, comparator) }
@UselessSortingAPI fun <T: Comparable<T>> Sequence<T>.bogobogoSorted(comparator: Comparator<T>) = toMutableList().apply { BogoSort.bogobogoSort(this, comparator) }

// Miracle sorting

@UselessSortingAPI fun <T: Comparable<T>> MutableList<T>.miracleSort() = MiracleSort.miracleSort(this)
@UselessSortingAPI fun <T: Comparable<T>> Array<T>.miracleSort() = MiracleSort.miracleSort(this)
@UselessSortingAPI fun <T: Comparable<T>> Iterable<T>.miracleSorted() = toMutableList().apply { MiracleSort.miracleSort(this) }
@UselessSortingAPI fun <T: Comparable<T>> Sequence<T>.miracleSorted() = toMutableList().apply { MiracleSort.miracleSort(this) }

@UselessSortingAPI fun <T: Comparable<T>> MutableList<T>.miracleSort(comparator: Comparator<T>) = MiracleSort.miracleSort(this, comparator)
@UselessSortingAPI fun <T: Comparable<T>> Array<T>.miracleSort(comparator: Comparator<T>) = MiracleSort.miracleSort(this, comparator)
@UselessSortingAPI fun <T: Comparable<T>> Iterable<T>.miracleSorted(comparator: Comparator<T>) = toMutableList().apply { MiracleSort.miracleSort(this, comparator) }
@UselessSortingAPI fun <T: Comparable<T>> Sequence<T>.miracleSorted(comparator: Comparator<T>) = toMutableList().apply { MiracleSort.miracleSort(this, comparator) }

@RequiresOptIn("This API is just for fun and should not be used in production code", RequiresOptIn.Level.WARNING)
@MustBeDocumented
annotation class UselessSortingAPI