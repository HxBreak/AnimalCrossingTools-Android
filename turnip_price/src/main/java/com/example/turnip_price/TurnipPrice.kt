package com.example.turnip_price

@UseExperimental(ExperimentalUnsignedTypes::class)
public class TurnipPrice {
    var basePrice: Int = 0
    var sellPrices = IntArray(14)
    var whatPattern: UInt = 0U
    var tmp40: Int = 0

    val rng = TurnipRandom()

    fun randBool(): Boolean = (rng.getUInt() and 0x80000000U) != 0U

    fun randInt(min: Int, max: Int): ULong {
        return ((rng.getUInt().toULong()) * (max - min + 1).toULong() shr 32) + min.toULong()
    }

    fun randFloat(a: Float, b: Float) {
        val fva = (0x3F800000U and (rng.getUInt() shr 9)).toFloat()

    }
}

@UseExperimental(ExperimentalUnsignedTypes::class)
class TurnipRandom {

    val mContext = uintArrayOf(0U, 0U, 0U, 0U)

    fun seed1() = mContext[0]
    fun seed2() = mContext[1]
    fun seed3() = mContext[2]
    fun seed4() = mContext[3]

    constructor() {
        TurnipRandom(42069U)
    }

    constructor(seed: UInt) {
        mContext[0] = 0x6C078965U * (seed xor (seed shr 30)) + 1U
        mContext[1] = 0x6C078965U * (mContext[0] xor (mContext[0] shr 30)) + 2U
        mContext[2] = 0x6C078965U * (mContext[1] xor (mContext[1] shr 30)) + 3U
        mContext[3] = 0x6C078965U * (mContext[2] xor (mContext[2] shr 30)) + 4U
    }

    constructor(seed1: UInt, seed2: UInt, seed3: UInt, seed4: UInt) {
        if ((seed1 or seed2 or seed3 or seed4) == 0U) {
            mContext[0] = 1U
            mContext[1] = 0x6C078967U
            mContext[2] = 0x714ACB41U
            mContext[3] = 0x48077044U
        }
        mContext[0] = seed1
        mContext[1] = seed2
        mContext[2] = seed3
        mContext[3] = seed4
    }

    fun getUInt(): UInt {
        val n = mContext[0] xor (mContext[0] shl 11)
        mContext[0] = mContext[1]
        mContext[1] = mContext[2]
        mContext[2] = mContext[3]
        mContext[3] = n xor (n shr 8) xor mContext[3] xor (mContext[3] shr 19)
        return mContext[3]
    }

    fun getULong(): ULong {
        val n1 = mContext[0] xor (mContext[0] shl 11)
        val n2 = mContext[1]
        val n3 = n1 xor (n1 shr 8) xor mContext[3]

        mContext[0] = mContext[2]
        mContext[1] = mContext[3]
        mContext[2] = n3 xor (mContext[3] shr 19)
        mContext[3] =
            n2 xor (n2 shl 11) xor (n2 xor (n2 shl 11) shr 8) xor mContext[2] xor (n3 shr 19)
        return (mContext[2].toULong() shl 32) and mContext[3].toULong()
    }
}