import gql from "graphql-tag";
import { useMutation } from "react-apollo";
import {
  MarkShowingAsBought,
  MarkShowingAsBoughtVariables
} from "../__generated__/MarkShowingAsBought";
import { UpdateShowingInput } from "../../../../__generated__/globalTypes";

const markAsBoughtMutation = gql`
  mutation MarkShowingAsBought(
    $showingId: UUID!
    $showing: UpdateShowingInput
  ) {
    updateShowing(showingId: $showingId, newValues: $showing) {
      id
    }

    markAsBought(showingId: $showingId) {
      id
      ticketsBought
      price
      private
      payToUser {
        id
      }
      expectedBuyDate
      time
      myTickets {
        id
      }
      attendeePaymentDetails {
        payTo {
          id
          nick
          firstName
          lastName
          phone
        }
        swishLink
        hasPaid
        amountOwed
      }
      adminPaymentDetails {
        participantPaymentInfos {
          id
          hasPaid
          amountOwed
          user {
            id
            nick
            name
            phone
          }
        }
      }
    }
  }
`;

export const useMarkAsBought = () => {
  const [mutate] = useMutation<MarkShowingAsBought, MarkShowingAsBoughtVariables>(
    markAsBoughtMutation
  );

  return (showingId: string, showing: UpdateShowingInput) =>
    mutate({
      variables: { showing, showingId },
      refetchQueries: ["ShowingsQuery"]
    });
};