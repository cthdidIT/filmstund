import React from "react";
import { cleanup, render } from "@testing-library/react";

import { TicketContainer } from "./TicketContainer";
import { createMockSeat, createMockTicket } from "./__mocks__/ticket";
import { MemoryRouter as Router } from "react-router";
import { ApolloProvider } from "react-apollo";
import { ApolloClient } from "apollo-client";
import { ApolloLink } from "apollo-link";
import { InMemoryCache } from "apollo-cache-inmemory";

const mockUserData = {
  id: "this-is-mock-user-id"
};

const minimalMockData = {
  id: "this-is-mock-showing-id",
  admin: mockUserData,
  ticketRange: {
    rows: [],
    seatings: []
  },
  filmstadenSeatMap: [],
  myTickets: [createMockTicket(1, 2, 3)]
};

const mockData = {
  id: "this-is-mock-showing-id",
  admin: mockUserData,
  ticketRange: {
    rows: [2, 3],
    seatings: [
      {
        row: 2,
        numbers: [3, 4]
      },
      {
        row: 3,
        numbers: [5, 6]
      }
    ]
  },
  filmstadenSeatMap: [
    createMockSeat(2, 3, 5, 0),
    createMockSeat(2, 4, 10, 0),
    createMockSeat(3, 5, 5, 5),
    createMockSeat(3, 6, 10, 5)
  ],
  myTickets: [
    createMockTicket(1, 2, 3),
    createMockTicket(2, 2, 4),
    createMockTicket(3, 3, 5),
    createMockTicket(4, 3, 6)
  ]
};

afterEach(cleanup);

const createEmptyApolloClient = () =>
  new ApolloClient({ link: ApolloLink.empty(), cache: new InMemoryCache() });

describe("<TicketContainer />", () => {
  it("renders without crashing", () => {
    const { container } = render(
      <ApolloProvider client={createEmptyApolloClient()}>
        <Router>
          <TicketContainer me={mockUserData} showing={minimalMockData} />
        </Router>
      </ApolloProvider>
    );
    expect(container).toMatchSnapshot();
  });

  it("renders the correct amount of tickets", () => {
    const { queryAllByText } = render(
      <ApolloProvider client={createEmptyApolloClient()}>
        <Router>
          <TicketContainer me={mockUserData} showing={mockData} />
        </Router>
      </ApolloProvider>
    );
    expect(queryAllByText("mock-cinema")).toHaveLength(4);
  });
});
