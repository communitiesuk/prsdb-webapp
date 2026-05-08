package uk.gov.communities.prsdb.webapp.controllers.controllerAdvice

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.controllers.CustomErrorController.Companion.CYA_ERROR_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.CustomErrorController.Companion.UPDATE_CONFLICT_ERROR_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.CyaDataHasChangedException
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalExceptionHandlerTests {
    private val globalExceptionHandler = GlobalExceptionHandler()

    @Test
    fun `handleCyaDataHasChangedException redirects to the CYA error route`() {
        val result = globalExceptionHandler.handleCyaDataHasChangedException(CyaDataHasChangedException("test message"))

        assertEquals("redirect:$CYA_ERROR_ROUTE", result)
    }

    @Test
    fun `handleCyaDataHasChangedException logs the exception message`() {
        val outContent = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outContent))
        try {
            globalExceptionHandler.handleCyaDataHasChangedException(CyaDataHasChangedException("test message"))

            val output = outContent.toString()
            assertTrue(output.contains("CYA data has changed: test message"))
        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun `handleUpdateConflictException redirects to the update conflict error route`() {
        val result = globalExceptionHandler.handleUpdateConflictException(UpdateConflictException("test message"))

        assertEquals("redirect:$UPDATE_CONFLICT_ERROR_ROUTE", result)
    }

    @Test
    fun `handleUpdateConflictException logs the exception message`() {
        val outContent = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outContent))
        try {
            globalExceptionHandler.handleUpdateConflictException(UpdateConflictException("test message"))

            val output = outContent.toString()
            assertTrue(output.contains("Update conflict occurred: test message"))
        } finally {
            System.setOut(originalOut)
        }
    }
}
