import React, { Component } from 'react';
import History from '../../components/history';
import ActualConfigurationWindow from '../../components/actual-configuration-window';
import { Col, Container, Row } from 'react-bootstrap';

class ConfigurationDetails extends Component {
    render(): React.ReactElement<React.JSXElementConstructor<any>> {
        return (
            <Container fluid={'xl'}>
                <h1 />
                <ActualConfigurationWindow />
                <History />
            </Container>
        );
    }
}

export default ConfigurationDetails;
