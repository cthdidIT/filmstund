import React from "react";
import styled from "@emotion/styled";
import { formatShowingDate, getTodaysDate } from "../../../lib/dateTools";
import PosterBox from "../ui/PosterBox";
import gql from "graphql-tag";

const VerticalPaddingContainer = styled.div`
  padding: 1em 0;
`;

const now = getTodaysDate();

const renderPremiereDate = releaseDate => {
  const formattedDate = formatShowingDate(releaseDate);

  if (releaseDate > now) {
    return "Premiär " + formattedDate;
  } else {
    return null;
  }
};

const Movie = ({
  movie: { poster, title, releaseDate },
  onClick,
  ...props
}) => (
  <div {...props}>
    <PosterBox headerText={title} poster={poster} onClick={onClick}>
      <VerticalPaddingContainer>
        {renderPremiereDate(releaseDate)}
      </VerticalPaddingContainer>
    </PosterBox>
  </div>
);

export const movieFragment = gql`
  fragment Movie on Movie {
    id
    poster
    title
    releaseDate
  }
`;

export default Movie;
