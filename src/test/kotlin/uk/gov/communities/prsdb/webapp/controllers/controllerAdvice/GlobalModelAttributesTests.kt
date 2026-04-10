package uk.gov.communities.prsdb.webapp.controllers.controllerAdvice

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.ui.ExtendedModelMap
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.services.BackUrlStorageService
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class GlobalModelAttributesTests {
    @Mock
    private lateinit var backUrlStorageService: BackUrlStorageService

    @Mock
    private lateinit var messageSource: MessageSource

    private val testServiceName = "Check a rental property or landlord"

    @Test
    fun `addGlobalModelAttributes sets customServiceName for local council routes`() {
        whenever(messageSource.getMessage(eq("localCouncilServiceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(testServiceName)
        val globalModelAttributes = GlobalModelAttributes(backUrlStorageService, messageSource)
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/$LOCAL_COUNCIL_PATH_SEGMENT/start"

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertEquals(testServiceName, model["customServiceName"])
    }

    @Test
    fun `addGlobalModelAttributes sets customServiceName for system operator routes`() {
        whenever(messageSource.getMessage(eq("localCouncilServiceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(testServiceName)
        val globalModelAttributes = GlobalModelAttributes(backUrlStorageService, messageSource)
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/$SYSTEM_OPERATOR_PATH_SEGMENT/dashboard"

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertEquals(testServiceName, model["customServiceName"])
    }

    @Test
    fun `addGlobalModelAttributes does not set customServiceName for other routes`() {
        val globalModelAttributes = GlobalModelAttributes(backUrlStorageService, messageSource)
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/landlord/dashboard"

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertNull(model["customServiceName"])
    }
}
