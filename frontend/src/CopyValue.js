import React, { Component } from "react";
import styled from "styled-components";

import copy from "./lib/copy";

const Hover = styled.div`
  &:hover {
    cursor: pointer;
    color: gray;
    &:after {
      padding-left: 1em;
      color: #222;
      content: '(Kopiera)';
      font-size: 0.8em;
    }
  }
`

class CopyValue extends Component {
  onClick = () => {
    copy(this.props.text)
  }

  render() {
    return <Hover onClick={this.onClick}>{this.props.text}</Hover>
  }
}

export default CopyValue