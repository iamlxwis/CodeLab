package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneSplitTest {
    @Test fun percentagesAreBounded() {
        assertEquals(35, clampPhoneSplit(-20))
        assertEquals(80, clampPhoneSplit(100))
        assertEquals(60, clampPhoneSplit(60))
    }

    @Test fun snappingUsesThreeUsableStops() {
        assertEquals(40, snapPhoneSplit(43))
        assertEquals(60, snapPhoneSplit(55))
        assertEquals(75, snapPhoneSplit(78))
    }
}
