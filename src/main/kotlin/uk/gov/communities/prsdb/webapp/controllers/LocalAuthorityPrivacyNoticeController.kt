package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PRIVACY_NOTICE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityPrivacyNoticeController.Companion.LOCAL_AUTHORITY_PRIVACY_NOTICE_ROUTE

@PrsdbController
@RequestMapping(LOCAL_AUTHORITY_PRIVACY_NOTICE_ROUTE)
class LocalAuthorityPrivacyNoticeController {
    // TODO PRSD-1424 Add privacy notice page
    companion object {
        const val LOCAL_AUTHORITY_PRIVACY_NOTICE_ROUTE = "/$LOCAL_AUTHORITY_PATH_SEGMENT/$PRIVACY_NOTICE_PATH_SEGMENT"
    }
}
