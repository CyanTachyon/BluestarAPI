package me.nullaqua.api.kotlin

@Suppress("unused")
fun lagrange(vararg points: Pair<Double, Double>): (Double)->Double = {
    var ans = 0.0
    for (i in points.indices)
    {
        var s = 1.0
        for (j in points.indices)
        {
            if (i == j) continue
            s *= (it-points[j].first)/(points[i].first-points[j].first)
        }
        ans += s*points[i].second
    }
    ans
}