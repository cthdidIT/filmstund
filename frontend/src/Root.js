
import React from 'react';
import Header from './components/header';
import MovieList from './components/movie-list';
import UserProfile from './components/user-profile';

import styles from './rootStyle.css';

const Root = React.createClass({
  render() {
    return (
      <div className={styles.main}>
        { this.props.children }
      </div>
    );
  }
})

export default Root
