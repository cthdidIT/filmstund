import React, { PropTypes } from 'react';
import loader from '../loader/';
import moment from 'moment';
import { connect } from 'react-redux';
import { withRouter } from 'react-router';
import { DateRange } from 'react-date-range';
import { postEndpoint } from '../../service/backend';
import { Bar } from 'react-chartjs';

import _ from 'lodash';

import ShowingHeader from '../showing-header';
import MovieInfo from '../movie-info';
import SlotPicker from '../slot-picker';

import styles from './style.css'
import {getUser} from "../../store/reducer/index";

const f = (date) => moment(date).format("DD/M HH:mm");

const Showing = React.createClass({
  propTypes: {
    showing: PropTypes.object.isRequired,
    currentUser: PropTypes.object.isRequired,
    selectedTimeSlots: PropTypes.object
  },

  getInitialState() {
    return {
      loading: false,
      slotsSaved: false
    }
  },

  calculateNewVotesFromPickedSlots(selectedIds) {
    const allTimeSlotsWithoutUsersVotes = this.props.showing.showing.time_slots.map(ts => ({
      ...ts,
      users: ts.users.filter(u => u.id != this.props.currentUser.id)
    }));

    const newTimeSlots = allTimeSlotsWithoutUsersVotes.map(ts => {
      if (selectedIds.includes(ts.sf_slot_id)) {
        ts.users.push(this.props.currentUser)
      }
      return ts;
    });


    return newTimeSlots;
  },

  submitSlotsPicked(selectedIds) {
    this.setState({
      loading: true,
      timeSlots: this.calculateNewVotesFromPickedSlots(selectedIds)
    });

    postEndpoint(`/showings/${this.props.params.id}/time_slots/votes`, {
      ids: selectedIds
    }).then(data => {
      this.setState({
        loading: false,
        timeSlots: data.time_slots
      });
    }).catch((err) => {
      err.json().then((x) => {
        console.log(x);
      })
    });
  },

  renderChart(barData) {
    const maxY = _.max(barData.map(d => d.y));
    const colors = barData.map(d => d.y === maxY ? 'goldenrod' : 'tomato');

    const data = {
      labels: barData.map(d => d.x),
      datasets: [{
        fillColor: colors,
        data: barData.map(d => d.y)
      }]
    };

    return (
      <Bar data={data} width="900" />
    )
  },

  renderSubmitTimeSlotButtons() {
    const { showing } = this.props.showing;


    return (
      <div className={styles.slotButtonContainer}>
        {
          showing.time_slots.map(ts => {
            const buttonClasses = [styles.timeSlotButton];
            if (showing.selected_time_slot && ts.sf_slot_id === showing.selected_time_slot.sf_slot_id) {
              buttonClasses.push(styles.selected)
            }
            return <div className={buttonClasses.join(' ')} key={ts.sf_slot_id}
                    onClick={() => this.submitTimeSlot(ts.sf_slot_id)}>{f(ts.start_time)}</div>
          })
        }
      </div>
    )
  },

  submitTimeSlot(sf_slot_id) {
    this.props.update('showing', postEndpoint(`/showings/${this.props.params.id}/complete`, { sf_slot_id }))
  },

  render() {
    const { showing: { showing }, currentUser } = this.props;
    const { time_slots:selectedTimeSlots } = this.props.selectedTimeSlots;
    if (!showing || !selectedTimeSlots) {
      return null;
    }

    const { loading } = showing;
    let { time_slots } = showing;
    if (this.state.timeSlots) {
      time_slots = this.state.timeSlots;
    }
    const barData = time_slots.map((ts) => ({
      x: f(ts.start_time),
      y: ts.users.length,
      id: ts.id
    }));

    return (
      <div className={styles.container}>
        <ShowingHeader showing={showing} />
        {showing.selected_time_slot && (
          <div>The selected date for this showing is {f(showing.selected_time_slot.start_time)}</div>
        )}
        <div className={styles.showingInfo}>
          Admin: {showing.owner.nick}
        </div>
        <div className={styles.timePicker}>
          {!showing.selected_time_slot && (
            time_slots && (
              <div>
                <SlotPicker timeSlots={time_slots}
                            initiallySelectedTimeSlots={selectedTimeSlots}
                            onChange={this.submitSlotsPicked}
                            getId={(slot) => slot.id}
                            userId={currentUser.id}
                            showUsers={true} />
              </div>
            )
          )}
          {this.renderChart(barData)}
          {showing.owner.id === currentUser.id && (this.renderSubmitTimeSlotButtons())}
        </div>
        <h3>Om filmen</h3>
        <MovieInfo movie={showing.movie} />
      </div>
    )
  }
});

export default withRouter(loader((props) => ({
    showing: `/showings/${props.params.id}`,
    selectedTimeSlots: `/showings/${props.params.id}/time_slots/votes`
}
))(connect(state => ({
  currentUser: getUser(state)
}))(Showing)))
