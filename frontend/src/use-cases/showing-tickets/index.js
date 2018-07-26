import { compose, withProps, branch, renderComponent } from "recompose";
import { withRouter } from "react-router";
import { graphql } from "react-apollo";
import gql from "graphql-tag";
import TicketContainer from "./TicketContainer";
import Loader from "../../use-cases/common/utils/ProjectorLoader";
import { ticketFragment, addTickets } from "../../apollo/mutations/tickets";

const routerParamsToShowingId = ({ match, history }) => {
  const { webId } = match.params;

  return { webId };
};

const data = graphql(
  gql`
    query TicketQuery($webId: Base64ID!) {
      me: currentUser {
        id
      }
      showing(webId: $webId) {
        ...Ticket
      }
    }
    ${ticketFragment}
  `,
  {
    options: {
      fetchPolicy: "cache-and-network"
    }
  }
);

const isLoading = branch(
  ({ data: { showing } }) => !showing,
  renderComponent(Loader)
);

export default compose(
  withRouter,
  withProps(routerParamsToShowingId),
  data,
  addTickets,
  isLoading
)(TicketContainer);