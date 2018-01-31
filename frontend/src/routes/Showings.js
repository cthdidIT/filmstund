import React, { Component } from "react";
import { graphql } from "react-apollo";
import gql from "graphql-tag";
import { orderBy, groupBy } from "lodash";
import { compose } from "recompose";
import moment from "moment";
import Helmet from "react-helmet";

import { getTodaysDate } from "../lib/dateTools";

import { RedHeader } from "../RedHeader";
import { ShowingNeue, showingFragment } from "../ShowingNeue";
import { Link } from "../MainButton";
import styled from "styled-components";
import {
  navigateToShowing,
  navigateToShowingTickets
} from "../navigators/index";
import { PageWidthWrapper } from "../PageWidthWrapper";
import { ShowingsGrid } from "../ShowingsGrid";

const showingDate = showing => showing.date + " " + showing.time;

const today = getTodaysDate();

const EmptyList = styled.div`
  display: flex;
  font-size: 15px;
  font-family: Roboto, sans-serif;
  justify-content: center;
  align-items: center;
  color: #9b9b9b;
  height: 50px;
`;

class Showings extends Component {
  navigateToShowing = showing => {
    navigateToShowing(this.props.history, showing);
  };

  navigateToTickets = showing => {
    navigateToShowingTickets(this.props.history, showing);
  };

  renderShowings = (showings, disabled) => {
    if (showings.length === 0) {
      return <EmptyList>Inga besök</EmptyList>;
    }
    return (
      <ShowingsGrid>
        {orderBy(showings, [showingDate], ["asc"]).map(showing => (
          <ShowingNeue
            key={showing.id}
            showing={showing}
            onClick={() => this.navigateToShowing(showing)}
            onClickTickets={() => this.navigateToTickets(showing)}
            disabled={disabled}
          />
        ))}
      </ShowingsGrid>
    );
  };

  render() {
    const { data: { showings = [] } } = this.props;

    const { previous = [], upcoming = [] } = groupBy(
      showings,
      s => (moment(showingDate(s)).isBefore(today) ? "previous" : "upcoming")
    );

    return (
      <PageWidthWrapper>
        <Helmet title="Alla besök" />
        <Link to="/showings/new">Skapa nytt besök</Link>
        <RedHeader>Aktuella besök</RedHeader>
        {this.renderShowings(upcoming, false)}
        <RedHeader>Tidigare besök</RedHeader>
        {this.renderShowings(previous, true)}
      </PageWidthWrapper>
    );
  }
}

const data = graphql(
  gql`
    query ShowingsQuery {
      showings: publicShowings {
        ...Showing
        id
        date
        time
      }
    }
    ${showingFragment}
  `,
  {
    options: { fetchPolicy: "cache-and-network" }
  }
);

export default compose(data)(Showings);
