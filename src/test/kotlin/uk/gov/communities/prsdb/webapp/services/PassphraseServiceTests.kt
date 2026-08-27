package uk.gov.communities.prsdb.webapp.services

import java.util.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PassphraseServiceTests {
    private val service = PassphraseService()

    @Test
    fun `generatePassphrase returns three supplied words`() {
        val words = service.generatePassphrase().split(" ")

        assertEquals(3, words.size)
        assertTrue(words.all { it in PassphraseService.WORDS })
    }

    @Test
    fun `generatePassphrase allows repeated words`() {
        val alwaysFirstRandom =
            object : Random() {
                override fun next(bits: Int): Int = 0
            }

        val passphrase = service.generatePassphrase(alwaysFirstRandom)

        assertEquals(List(3) { PassphraseService.WORDS.first() }.joinToString(" "), passphrase)
    }
}
