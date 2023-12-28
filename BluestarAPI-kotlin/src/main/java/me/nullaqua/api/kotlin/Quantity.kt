package me.nullaqua.api.kotlin

import me.nullaqua.api.util.quantity.DataRate
import me.nullaqua.api.util.quantity.DataSize
import me.nullaqua.api.util.quantity.Distance
import me.nullaqua.api.util.quantity.Time

infix operator fun DataRate.plus(other: DataRate): DataRate = this.add(other)
infix operator fun DataRate.minus(other: DataRate): DataRate = this.sub(other)
infix operator fun DataSize.plus(other: DataSize): DataSize = this.add(other)
infix operator fun DataSize.minus(other: DataSize): DataSize = this.sub(other)
infix operator fun Distance.plus(other: Distance): Distance = this.add(other)
infix operator fun Distance.minus(other: Distance): Distance = this.sub(other)
infix operator fun Time.plus(other: Time): Time = this.add(other)
infix operator fun Time.minus(other: Time): Time = this.sub(other)