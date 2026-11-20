package ph.perfumedex

import org.junit.Assert.*
import org.junit.Test

class VouchRulesTest {
    @Test fun untriedFragranceCannotVouch() { assertFalse(VouchRules.eligible(emptyList(), "001")) }
    @Test fun anyWearFormatQualifiesOnlyItsFragrance() {
        listOf("Tester", "Decant", "Bottle").forEach { format ->
            val wears = listOf(Wear("1", "001", "Office", "", format, "today", false))
            assertTrue(VouchRules.eligible(wears, "001"))
            assertFalse(VouchRules.eligible(wears, "002"))
        }
    }
    @Test fun postsDoNotMultiplyVouchesAndUndoRemovesThem() {
        val key = VouchRules.key("001", "Office")
        var votes = setOf(key, VouchRules.key("001", "Office"))
        assertEquals(1, votes.size)
        assertNotEquals(key, VouchRules.key("001", "Rainy day"))
        votes = votes - key
        assertTrue(votes.isEmpty())
    }
}
