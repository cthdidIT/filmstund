import React, { Component } from "react";
import { graphql } from "react-apollo";
import gql from "graphql-tag";
import isAfter from "date-fns/is_after";
import isBefore from "date-fns/is_before";
import { compose } from "recompose";
import orderBy from "lodash/orderBy";
import Helmet from "react-helmet";

import { Link } from "../common/ui/MainButton";
import { Jumbotron, JumbotronBackground } from "./Jumbotron";
import { showingFragment, ShowingNeue } from "../common/showing/ShowingNeue";
import { RedHeader } from "../common/ui/RedHeader";
import { formatYMD, getTodaysDate } from "../../lib/dateTools";
import {
  navigateToShowing,
  navigateToShowingTickets
} from "../common/navigators/index";
import { PageWidthWrapper } from "../common/ui/PageWidthWrapper";
import { ShowingsGrid } from "../common/ui/ShowingsGrid";
import { ItsHappeningTitle } from "./ItsHappeningTitle";
import { EmptyList } from "../common/ui/EmptyList";

const showingDate = showing => showing.date + " " + showing.time;

const today = getTodaysDate();

class Home extends Component {
  navigateToShowing = showing => {
    navigateToShowing(this.props.history, showing);
  };

  navigateToTickets = showing => {
    navigateToShowingTickets(this.props.history, showing);
  };

  renderShowings = (showings, order) => {
    if (showings.length === 0) {
      return <EmptyList />;
    }
    return (
      <ShowingsGrid>
        {orderBy(showings, [showingDate], [order]).map(showing => (
          <ShowingNeue
            showing={showing}
            onClick={() => this.navigateToShowing(showing)}
            onClickTickets={() => this.navigateToTickets(showing)}
            disabled={isBefore(showingDate(showing), today)}
            key={showing.id}
          />
        ))}
      </ShowingsGrid>
    );
  };

  renderCreatedByMe = showings => {
    const {
      data: { me }
    } = this.props;
    const myShowings = showings.filter(s => s.admin.id === me.id);

    return this.renderShowings(myShowings, "desc");
  };

  renderParticipatedByMe = showings => {
    const {
      data: { me }
    } = this.props;
    const myShowings = showings.filter(
      s =>
        s.participants.some(p => p.user.id === me.id) &&
        isAfter(showingDate(s), today)
    );

    return this.renderShowings(myShowings, "asc");
  };

  renderPrevParticipatedByMe = showings => {
    const {
      data: { me }
    } = this.props;
    const myShowings = showings.filter(
      s =>
        s.participants.some(p => p.user.id === me.id) &&
        isBefore(showingDate(s), today)
    );

    return this.renderShowings(myShowings, "desc");
  };

  render() {
    const {
      data: { showings = [] }
    } = this.props;

    const todayShowing = showings.filter(
      s => formatYMD(showingDate(s)) === formatYMD(today)
    );

    return (
      <React.Fragment>
        <Helmet title="Mina Besök" />
        {todayShowing.length > 0 && (
          <JumbotronBackground>
            <PageWidthWrapper>
              <Jumbotron>
                <ShowingNeue
                  showing={todayShowing[0]}
                  onClick={() => this.navigateToShowing(todayShowing[0])}
                />
                <ItsHappeningTitle>
                  It's happening!{" "}
                  <span role="img" aria-label="heart eyes emoji">
                    😍
                  </span>
                </ItsHappeningTitle>
              </Jumbotron>
            </PageWidthWrapper>
          </JumbotronBackground>
        )}
        <PageWidthWrapper>
          <Link to="/showings/new">Skapa nytt besök</Link>
          <RedHeader>Mina kommande besök</RedHeader>

          {this.renderParticipatedByMe(showings)}
          <RedHeader>Mina tidigare besök</RedHeader>
          {this.renderPrevParticipatedByMe(showings)}
          <RedHeader>Besök jag har skapat</RedHeader>
          {this.renderCreatedByMe(showings)}
        </PageWidthWrapper>
      </React.Fragment>
    );
  }
}

const data = graphql(
  gql`
    query HomeQuery {
      showings: publicShowings {
        ...ShowingNeue
        id
        webId
        slug
        date
        time
        admin {
          id
        }
        participants {
          user {
            id
          }
        }
      }
      me: currentUser {
        id
      }
    }
    ${showingFragment}
  `,
  {
    options: { fetchPolicy: "cache-and-network" }
  }
);

export default compose(data)(Home);