import React from "react";
import { connect } from "react-redux";

import { showings as showingActions } from "./store/reducers";

import { GreenButton, RedButton } from "./MainButton";
import buildUserComponent from "./UserComponentBuilder";

const UserItem = buildUserComponent(({ user }) =>
  <div>
    {user.nick || user.name} ({user.phone})
  </div>
);

const PendingShowing = ({
  showing,
  isParticipating,
  handleAttend,
  handleUnattend
}) =>
  <div>
    {!isParticipating &&
      <GreenButton onClick={handleAttend}>Jag hänger på!</GreenButton>}
    {isParticipating && <RedButton onClick={handleUnattend}>Avanmäl</RedButton>}
    <div>
      {showing.participants.map(userId =>
        <UserItem key={userId} userId={userId} />
      )}
    </div>
  </div>;

const mapDispatchToProps = (dispatch, props) => ({
  handleAttend: () => showingActions.actions.requestAttend(props.showing.id),
  handleUnattend: () => showingActions.actions.requestUnattend(props.showing.id)
});

export default connect(null, mapDispatchToProps)(PendingShowing);