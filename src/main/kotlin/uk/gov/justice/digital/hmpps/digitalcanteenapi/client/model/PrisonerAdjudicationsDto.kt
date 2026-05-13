package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model

import java.time.LocalDate

data class PrisonerHasAdjudicationsDto(
  val hasAdjudications: Boolean? = null,
)

data class PrisonerAdjudicationsDto(
  val punishments: List<Punishment>? = null
)

data class Punishment(
  val id: Long,
  val type: String,
  val typeDescription: String?,
  val privilegeType: String?,
  val privilegeTypeDescription: String?,
  val otherPrivilege: String?,
  val stoppagePercentage: Int?,
  val schedule: PunishmentSchedule?,
  val damagesOwedAmount: Double?,
  val canRemove: Boolean?,
  val canEdit: Boolean?
)

data class PunishmentSchedule(
  val duration: Int?,
  val measurement: String?,
  val startDate: LocalDate?,
  val endDate: LocalDate?,
  val suspendedUntil: LocalDate?
)