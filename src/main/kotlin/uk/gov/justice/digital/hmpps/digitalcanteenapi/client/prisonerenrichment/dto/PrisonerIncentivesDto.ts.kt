package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PrisonerIncentivesDto(
  val id: Long,
  val iepCode: String,
  val iepLevel: String,
  val prisonerNumber: String,
  val bookingId: Long,
  val iepDate: String,
  val iepTime: String,
  val iepDetails: List<IepDetail>,
  val nextReviewDate: String?,
  val daysSinceReview: Int?,
)

data class IepDetail(
  val id: Long,
  val iepLevel: String,
  val iepCode: String,
  val comments: String?,
  val prisonerNumber: String,
  val bookingId: Long,
  val iepDate: String,
  val iepTime: String,
  val agencyId: String,
  val userId: String,
  val reviewType: String,
  val auditModuleName: String?,
  @JsonProperty("isRealReview")
  val isRealReview: Boolean,
)
