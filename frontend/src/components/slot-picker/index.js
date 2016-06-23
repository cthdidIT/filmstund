import React from 'react';
import _ from 'lodash';
import moment from '../../lib/moment';

import styles from './style.css';

const SlotPicker = React.createClass({
  getInitialState() {
    return {
      selectedIds: []
    }
  },
  handleSelectId(slotId) {
    if (this.state.selectedIds.includes(slotId)) {
      this.setState({
        selectedIds: this.state.selectedIds.filter(id => id !== slotId)
      })
    } else {
      this.setState({
        selectedIds: [...this.state.selectedIds, slotId]
      })
    }
  },
  renderSlot(slot) {
    const isSelected = this.state.selectedIds.includes(slot.id)

    return (
      <div key={slot.id}
           className={styles.doodleSlot + ' ' + (isSelected ? styles.selected : '')}
           onClick={() => this.handleSelectId(slot.id)}>
        <div className={styles.time}>
          {moment(slot.start_time).format('LT')}
        </div>
        <div className={styles.tags}>
          {slot.is_vip && <span className={styles.is_vip}>VIP</span>}
          {slot.is_3d && <span className={styles.is_3d} title="Filmen visas i 3D :(">3D</span>}
          {!slot.sf_slot_id && <span className={styles.is_3d} title="Approximativ tid (kan komma att ändras)">≈</span>}
        </div>
        <small title={slot.auditorium_name}>
          {slot.theatre.replace('Fs ', '').substring(0, 4)}…
        </small>
      </div>
    )
  },
  renderDay(date, slots) {
    return (
      <div key={date} className={styles.doodleDay}>
        <div className={styles.doodleDate} title={moment(date).format('L')}>{moment(date).format('D MMM')}</div>
        <div>{slots.map(this.renderSlot)}</div>
      </div>
    )
  },
  render() {
    const { time_slots } = this.props;
    const slotsByDate = _.groupBy(time_slots, s => s.start_time);
    const keys = Object.keys(slotsByDate)

    return (
      <div className={styles.daysContainer}>
        <div className={styles.daysRow}>
          {keys.map(key => this.renderDay(key, slotsByDate[key]))}
        </div>
      </div>
    )
  }
})

export default SlotPicker
