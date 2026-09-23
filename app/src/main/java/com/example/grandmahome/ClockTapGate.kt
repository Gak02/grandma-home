package com.example.grandmahome

internal class ClockTapGate {
    private var count = 0
    private var lastTap = 0L

    fun tap(now: Long): Boolean {
        count = if (now - lastTap > 1500) 1 else count + 1
        lastTap = now
        if (count < 5) return false
        count = 0
        return true
    }
}
