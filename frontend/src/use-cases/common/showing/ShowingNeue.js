import React from "react";
import { Header } from "../ui/RedHeader";
import faQrcode from "@fortawesome/fontawesome-free-solid/faQrcode";
import faChevronRight from "@fortawesome/fontawesome-free-solid/faChevronRight";
import { formatShowingDateTime } from "../../../lib/dateTools";
import gql from "graphql-tag";
import SeatRange from "../../showing-tickets/SeatRange";
import {
  Box,
  ButtonText,
  CenterColumn,
  Column,
  Content,
  Description,
  FaIcon,
  Poster,
  RedButton,
  TicketRangeContainer
} from "./style";
import { UserHeads } from "./UserHeads";
import { ScreenName } from "./ScreenName";

export const ShowingNeue = ({ showing, onClick, onClickTickets }) => {
  const showingHasTickets = showing.myTickets.length > 0;

  return (
    <Box onClick={onClick}>
      <Poster src={showing.movie.poster} />
      <CenterColumn>
        <Content>
          <Header>{showing.movie.title}</Header>
          <Description>
            {formatShowingDateTime(showing.date + " " + showing.time)}
          </Description>
          <UserHeads users={showing.participants.map(p => p.user)} />
          <ScreenName ticket={showing.myTickets[0]} />
          <TicketRangeContainer>
            <SeatRange ticketRange={showing.ticketRange} />
          </TicketRangeContainer>
        </Content>
        {showingHasTickets && (
          <RedButton
            disabled={!onClickTickets}
            onClick={e => {
              e.stopPropagation();
              if (onClickTickets) {
                onClickTickets();
              }
            }}
          >
            <FaIcon color="#fff" icon={faQrcode} />
            <ButtonText>Visa biljett</ButtonText>
          </RedButton>
        )}
      </CenterColumn>
      <Column>
        <FaIcon color="#9b9b9b" icon={faChevronRight} />
      </Column>
    </Box>
  );
};

export const showingFragment = gql`
  fragment ShowingNeue on Showing {
    id
    webId
    slug
    date
    time
    movie {
      id
      poster
      title
    }
    ticketRange {
      rows
      seatings {
        row
        numbers
      }
    }
    myTickets {
      id
      cinema
      screen
    }
    participants {
      user {
        id
        avatar
      }
    }
    location {
      name
    }
  }
`;