package uk.gov.communities.prsdb.webapp.constants

/* Incoming requests are limited to 8kb by default by the WAF, but that rule isn't applied to requests where the url
 * contains this substring. The details of which requests are allowed through the WAF if they're larger than 8kb
 * is specified in this module https://github.com/communitiesuk/prsdb-infra/blob/main/terraform/modules/frontdoor/waf.tf
 *
 * The scope_down_statements of "aws-managed-rules-common-rule-set-for-file-uploads" AND
 * "aws-managed-rules-common-rule-set" must be updated to ensure the remainder of the AWS managed rules are applied.
 */
const val FILE_UPLOAD_URL_SUBSTRING = "file-upload"
