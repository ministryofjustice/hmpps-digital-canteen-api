package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto

import java.time.LocalDate

data class PrisonerAdjudicationsDto(
  val punishments: List<Punishment>? = null,
)

data class Punishment(
  val chargeNumber: String,
  val punishmentType: String?,
  val privilegeType: String?,
  val otherPrivilegeType: String?,
  val duration: Int?,
  val measurement: String?,
  val startDate: LocalDate?,
  val lastDay: LocalDate?,
  val amount: Double?,
  val stoppagePercentage: Int?,
  val activatedFrom: String?,
)
