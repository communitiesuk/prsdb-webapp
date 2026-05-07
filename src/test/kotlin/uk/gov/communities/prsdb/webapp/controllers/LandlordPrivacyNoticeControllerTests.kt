package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS

@WebMvcTest(LandlordPrivacyNoticeController::class)
class LandlordPrivacyNoticeControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    lateinit var featureFlagManager: FeatureFlagManager

    @Test
    fun `LandlordPrivacyNoticeController returns 200 for unauthenticated user`() {
        mvc.get(LandlordPrivacyNoticeController.LANDLORD_PRIVACY_NOTICE_ROUTE).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `LandlordPrivacyNoticeController returns 308 for unauthenticated user with trailing slash`() {
        mvc.get("${LandlordPrivacyNoticeController.LANDLORD_PRIVACY_NOTICE_ROUTE}/").andExpect {
            status { isPermanentRedirect() }
        }
    }

    @Test
    @WithMockUser
    fun `LandlordPrivacyNoticeController returns 200 for authenticated user`() {
        mvc.get(LandlordPrivacyNoticeController.LANDLORD_PRIVACY_NOTICE_ROUTE).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `LandlordPrivacyNoticeController sets jointLandlordsEnabled to true when JOINT_LANDLORDS is enabled`() {
        whenever(featureFlagManager.checkFeature(JOINT_LANDLORDS)).thenReturn(true)
        mvc.get(LandlordPrivacyNoticeController.LANDLORD_PRIVACY_NOTICE_ROUTE).andExpect {
            status { isOk() }
            model { attribute("jointLandlordsEnabled", true) }
        }
    }

    @Test
    fun `LandlordPrivacyNoticeController sets jointLandlordsEnabled to false when JOINT_LANDLORDS is disabled`() {
        whenever(featureFlagManager.checkFeature(JOINT_LANDLORDS)).thenReturn(false)
        mvc.get(LandlordPrivacyNoticeController.LANDLORD_PRIVACY_NOTICE_ROUTE).andExpect {
            status { isOk() }
            model { attribute("jointLandlordsEnabled", false) }
        }
    }
}
