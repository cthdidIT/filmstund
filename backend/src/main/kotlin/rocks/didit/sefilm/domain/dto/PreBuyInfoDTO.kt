package rocks.didit.sefilm.domain.dto

import rocks.didit.sefilm.database.entities.ParticipantPaymentInfo
import rocks.didit.sefilm.domain.SfMembershipId
import rocks.didit.sefilm.domain.TicketNumber
import rocks.didit.sefilm.domain.UserID

data class PreBuyInfoDTO(val sfBuyLink: String? = null,
                         val sfData: List<UserAndSfData>,
                         val paymentInfo: Collection<ParticipantPaymentInfo> = emptyList())

data class UserAndSfData(val userId: UserID, val sfMembershipId: SfMembershipId?, val foretagsbiljett: TicketNumber?)