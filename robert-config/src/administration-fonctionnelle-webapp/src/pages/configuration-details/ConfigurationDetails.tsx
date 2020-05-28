import React, {Component} from "react";
import History from "../../components/history";
import ActualConfigurationWindow from "../../components/actual-configuration-window";
import {Col, Container, Row} from "react-bootstrap";


class ConfigurationDetails extends Component {

    render(): React.ReactElement<React.JSXElementConstructor<any>> {
        return (
            <Container fluid={"xl"}>
                <h1/>
                <Row>
                    <Col style={{maxHeight:'80vh'}}>
                        <ActualConfigurationWindow/>
                    </Col>
                    <Col style={{maxHeight:'80vh'}}>
                        <History/>
                    </Col>
                </Row>
            </Container>
        );
    }
}

export default ConfigurationDetails;