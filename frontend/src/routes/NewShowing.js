import React, { Component } from "react";
import { orderBy } from "lodash";
import moment from "moment";
import cx from "classnames";
import Helmet from "react-helmet";
import styled from "styled-components";
import { withRouter } from "react-router";

import Header from "../Header";
import Movie, { movieFragment } from "../Movie";
import CreateShowingForm from "../CreateShowingForm";

import Field from "../Field";
import Input from "../Input";
import gql from "graphql-tag";
import { graphql } from "react-apollo";
import { compose } from "recompose";
import { fetchMovies } from "../fragments/movies";

const SearchField = styled(Field)`
  max-width: 100%;
`;

const RefreshButton = styled.button`
  -webkit-appearance: none;
  background: none;
  border: 0;
  color: #b71c1c;
  font-size: 16pt;
  padding: 0 0.5em;
  cursor: pointer;
`;

const FlexHeader = styled(Header)`
  display: flex;
  align-items: center;
  justify-content: center;
`;

const StyledMovie = styled(Movie)`
  padding: 0.5em;
  width: 100%;

  @media (min-width: 610px) {
    max-width: 50%;
  }

  @media (min-width: 910px) {
    max-width: 18em;
  }
`;

const MovieContainer = styled.div`
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
`;

class NewShowing extends Component {
  state = {
    searchTerm: ""
  };

  requestSFData = () => {
    this.setState({ requestingData: true });
    this.props.fetchMovies().then(() => {
      this.props.data.refetch();
      this.setState({ requestingData: false });
    });
  };

  renderRequestButton = () => {
    const { requestingData } = this.state;
    return (
      <RefreshButton role="button" onClick={this.requestSFData}>
        <i
          className={cx("fa fa-refresh", { "fa-spin": requestingData })}
          aria-hidden="true"
        />
      </RefreshButton>
    );
  };

  setSearchTerm = term => {
    this.setState({
      searchTerm: term.target.value
    });
  };

  searchFilter(m) {
    const { searchTerm } = this.state;

    const lowerCaseTerm = searchTerm.toLowerCase();

    if (searchTerm && searchTerm.length > 0) {
      if (m.title.toLowerCase().search(lowerCaseTerm) > -1) {
        return true;
      }

      return false;
    }

    return true;
  }

  renderSelectMovie = movies => {
    const { searchTerm } = this.state;

    return (
      <div>
        <Helmet title="Skapa besök" />
        <FlexHeader>Skapa besök {this.renderRequestButton()}</FlexHeader>
        <SearchField>
          <Input
            type="text"
            onChange={this.setSearchTerm}
            placeholder="Vilken film vill du se?"
            value={searchTerm}
          />
        </SearchField>
        <MovieContainer>
          {orderBy(movies, ["popularity", "releaseDate"], ["desc", "asc"])
            .filter(m => this.searchFilter(m))
            .map(m => (
              <StyledMovie
                key={m.id}
                movie={m}
                onClick={() => this.setMovie(m)}
              />
            ))}
        </MovieContainer>
      </div>
    );
  };

  setMovie = movie => {
    this.props.history.push(`/showings/new/${movie.id}`);
  };

  navigateToShowing = showingId => {
    this.props.history.push(`/showings/${showingId}`);
  };

  clearSelectedMovie = () => {
    this.props.history.push(`/showings/new`);
  };

  render() {
    const {
      data: { movies = [] },
      match: { params: { movieId } }
    } = this.props;

    if (movieId) {
      return (
        <CreateShowingForm
          movieId={movieId}
          navigateToShowing={this.navigateToShowing}
          clearSelectedMovie={this.clearSelectedMovie}
        />
      );
    } else {
      return this.renderSelectMovie(movies);
    }
  }
}

const data = graphql(gql`
  query NewShowingQuery {
    movies: allMovies {
      ...Movie
      id
      popularity
      releaseDate
    }
  }
  ${movieFragment}
`);

export default compose(withRouter, data, fetchMovies)(NewShowing);
