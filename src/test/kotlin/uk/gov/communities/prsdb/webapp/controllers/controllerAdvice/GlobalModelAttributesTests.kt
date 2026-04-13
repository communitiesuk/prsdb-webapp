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
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class GlobalModelAttributesTests {
    @Mock
    private lateinit var backUrlStorageService: BackUrlStorageService

    @Mock
    private lateinit var messageSource: MessageSource

    private val defaultServiceName = "Register your rental property"
    private val customServiceName = "Check a rental property or landlord"

    @Test
    fun `addGlobalModelAttributes sets serviceName to custom name for local council routes`() {
        whenever(messageSource.getMessage(eq("localCouncilServiceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(customServiceName)
        val globalModelAttributes = GlobalModelAttributes(backUrlStorageService, messageSource)
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/$LOCAL_COUNCIL_PATH_SEGMENT/start"

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertEquals(customServiceName, model["serviceName"])
        assertTrue(model["isCustomServiceName"] as Boolean)
    }

    @Test
    fun `addGlobalModelAttributes sets serviceName to custom name for system operator routes`() {
        whenever(messageSource.getMessage(eq("localCouncilServiceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(customServiceName)
        val globalModelAttributes = GlobalModelAttributes(backUrlStorageService, messageSource)
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/$SYSTEM_OPERATOR_PATH_SEGMENT/dashboard"

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertEquals(customServiceName, model["serviceName"])
        assertTrue(model["isCustomServiceName"] as Boolean)
    }

    @Test
    fun `addGlobalModelAttributes sets serviceName to default name for other routes`() {
        whenever(messageSource.getMessage(eq("serviceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(defaultServiceName)
        val globalModelAttributes = GlobalModelAttributes(backUrlStorageService, messageSource)
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/landlord/dashboard"

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertEquals(defaultServiceName, model["serviceName"])
        assertNull(model["isCustomServiceName"])
    }
}
