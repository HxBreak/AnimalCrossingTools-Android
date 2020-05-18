package com.hxbreak.animalcrossingtools.jni

object NativeTurnip {

    init {
        System.loadLibrary("turnip")
    }

    external fun calculate(pattern: Int, basePrice: Int, params: IntArray)
}