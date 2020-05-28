import React, {Component} from 'react';
import {Container} from "react-bootstrap";

export default class MainContainer extends Component {
  render(): React.ReactElement {
    return (
        <Container fluid={"lg"} style={{maxHeight:"100%", overflow:"hidden", display:"flex", paddingTop:"2em"}}>
          <div style={{margin:"auto", width:"100%",  maxHeight:"100%"}}>
            {this.props.children}
          </div>
        </Container>
    );
  }
}
