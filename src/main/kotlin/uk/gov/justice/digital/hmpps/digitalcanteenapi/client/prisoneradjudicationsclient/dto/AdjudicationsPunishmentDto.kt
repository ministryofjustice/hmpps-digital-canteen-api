package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisoneradjudicationsclient.dto

import java.time.LocalDate

data class AdjudicationsPunishmentDto(
  val chargeNumber: String,
  val punishmentType: String?,
  val privilegeType: String?,
  val otherPrivilege: String?,
  val duration: Int?,
  val measurement: String?,
  val startDate: LocalDate?,
  val lastDay: LocalDate?,
  val amount: Double?,
  val stoppagePercentage: Int?,
  val activatedFrom: String?,
)
