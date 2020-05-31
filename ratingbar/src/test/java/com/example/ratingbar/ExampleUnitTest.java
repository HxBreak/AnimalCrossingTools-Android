package com.example.ratingbar;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testCalc() {
        float star = 3.9f;
        if (star < 5f && star > 0f) {
            float d = star * 2f;
            int tmpStar = ((int) d) + ((d % 1) > 0.5f ? 1 : 0);
            System.out.println(tmpStar);
        }
    }
}