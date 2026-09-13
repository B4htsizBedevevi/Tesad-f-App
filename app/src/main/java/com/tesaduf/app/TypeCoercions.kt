package com.tesaduf.app

// Compatibility helpers for the animated Canvas math in MainActivity.
// Keep the visual implementation unchanged while making mixed numeric
// expressions resolve consistently to Float under Kotlin 2.1.
operator fun Float.plus(other: Int): Float = this + other.toFloat()
operator fun Int.plus(other: Float): Float = this.toFloat() + other
operator fun Float.minus(other: Int): Float = this - other.toFloat()
operator fun Int.minus(other: Float): Float = this.toFloat() - other
