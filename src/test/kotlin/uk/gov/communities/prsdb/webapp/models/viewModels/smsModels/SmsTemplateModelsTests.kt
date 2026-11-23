package uk.gov.communities.prsdb.webapp.models.viewModels.smsModels

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.testHelpers.NotifyTemplateMetadataFactory
import kotlin.test.assertEquals

class SmsTemplateModelsTests {
    @Test
    fun `passphrase personalisation matches the placeholders in its Notify template`() {
        // Arrange
        val sms = PassphraseSms("time anything costume")
        val metadata =
            NotifyTemplateMetadataFactory(null, "/sms/smsTemplates.json").metadataList.single {
                it.enumName == sms.template.name
            }
        val body = javaClass.getResource(metadata.bodyLocation)!!.readText()
        val placeholders = Regex("\\(\\((.*?)\\)\\)").findAll(body).map { it.groupValues[1] }.toSet()

        // Act & Assert
        assertEquals(placeholders, sms.toHashMap().keys)
    }
}
