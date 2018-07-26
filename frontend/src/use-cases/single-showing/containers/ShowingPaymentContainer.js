import React from "react";
import BoughtShowing from "../BoughtShowing";
import PendingShowing from "../PendingShowing";

const ShowingPaymentContainer = ({
  showing,
  isAdmin,
  onClickTickets,
  openSwish,
  isParticipating
}) => {
  if (showing.ticketsBought) {
    if (isParticipating) {
      return (
        <BoughtShowing
          myTickets={showing.myTickets}
          isAdmin={isAdmin}
          onClickTickets={this.navigateToTickets}
          openSwish={this.openSwish}
          attendeePaymentDetails={showing.attendeePaymentDetails}
        />
      );
    }
  } else {
    return (
      <PendingShowing
        showingId={showing.id}
        isParticipating={isParticipating}
      />
    );
  }
};

export default ShowingPaymentContainer;
