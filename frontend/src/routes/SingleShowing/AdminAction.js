import React, { Component } from "react";
import { connect } from "react-redux";
import PropTypes from "prop-types";

import { showings as showingActions } from "../../store/reducers";

import { getJson, jsonRequest } from "../../lib/fetch";
import { withBaseURL } from "../../lib/withBaseURL";

import MainButton, { GrayButton } from "../../MainButton";
import BuyModal from "../../BuyModal";

const oreToKr = price => {
  if (price === null) {
    return 0;
  } else {
    return Math.ceil(price / 100);
  }
};

class AdminAction extends Component {
  constructor(props) {
    super(props);

    this.state = {
      isCreatingEvent: false,
      adminMessage: null,
      ticketPrice: oreToKr(props.showing.price),
      buyData: null,
      showModal: false
    };
  }

  setPrice = price => {
    const int = parseInt(price, 10);

    this.setState({
      ticketPrice: isNaN(int) ? 0 : int
    });
  };

  handlePaidChange = info => {
    const data = {
      ...info,
      hasPaid: !info.hasPaid
    };
    jsonRequest(withBaseURL("/participantinfo"), data, "PUT").then(newInfo => {
      this.setState({
        buyData: {
          ...this.state.buyData,
          participantInfo: this.state.buyData.participantInfo.map(info => {
            if (info.id === newInfo.id) {
              return newInfo;
            } else {
              return info;
            }
          })
        }
      });
    });
  };

  handleStartBooking = () => {
    const { showing } = this.props;
    this.setState({
      showModal: true
    });

    getJson(`/showings/${showing.id}/buy`).then(buyData => {
      this.setState({
        showModal: true,
        buyData
      });
    });
  };

  handleMarkBought = event => {
    event.preventDefault();

    this.props.dispatch(
      showingActions.actions.requestUpdate({
        ...this.props.showing,
        price: this.state.ticketPrice * 100,
        ticketsBought: true
      })
    );
    setTimeout(() => {
      this.handleStartBooking();
    }, 2000);
  };

  handleDelete = () => {
    const proceed = window.confirm("Är du säker? Går ej att ångra!");

    if (proceed) {
      this.props.dispatch(
        showingActions.actions.requestDelete(this.props.showingId)
      );
    }
  };

  handleCreateGoogleEvent = () => {
    const { showing } = this.props;

    this.setState({
      isCreatingEvent: true
    });

    jsonRequest(
      withBaseURL(`/showings/${showing.id}/invite/googlecalendar`),
      showing.participants
    )
      .then(resp => {
        this.setState({
          isCreatingEvent: false,
          adminMessage: "Kalenderevent skapat"
        });
      })
      .catch(err => {
        this.setState({
          isCreatingEvent: false,
          adminMessage: "Misslyckades med att skapa kalenderevent"
        });
      });
  };

  render() {
    const { showing, loading } = this.props;

    const { ticketsBought } = showing;

    const {
      isCreatingEvent,
      ticketPrice,
      showModal,
      buyData,
      adminMessage
    } = this.state;

    return (
      <div>
        {showModal &&
          <BuyModal
            setPrice={this.setPrice}
            loading={loading}
            showing={showing}
            handleMarkBought={this.handleMarkBought}
            handlePaidChange={this.handlePaidChange}
            ticketPrice={ticketPrice}
            buyData={buyData}
            closeModal={() =>
              this.setState({ showModal: false, buyData: null })}
          />}
        {adminMessage &&
          <div>
            {adminMessage}
          </div>}
        <MainButton onClick={this.handleStartBooking}>
          {ticketsBought
            ? "Visa betalningsstatus"
            : "Alla är med, nu bokar vi!"}
        </MainButton>
        {ticketsBought &&
          <MainButton
            disabled={isCreatingEvent}
            onClick={this.handleCreateGoogleEvent}
          >
            Skapa google kalender event
          </MainButton>}
        <GrayButton onClick={this.handleDelete}>Ta bort Besök</GrayButton>
      </div>
    );
  }
}

AdminAction.propTypes = {
  showing: PropTypes.object.isRequired
};

export default connect()(AdminAction);