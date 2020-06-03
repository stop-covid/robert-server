import React, { Component } from 'react';
import History from '../../components/history';
import ActualConfigurationWindow from '../../components/actual-configuration-window';
import { Tabs, Tab } from 'react-bootstrap';

class ConfigurationDetails extends Component {
    render(): React.ReactElement<React.JSXElementConstructor<any>> {
        return (
            <div>
                <Tabs
                    defaultActiveKey="configuration"
                    id="configuration-details"
                >
                    <Tab eventKey="configuration" title="Configuration">
                        <ActualConfigurationWindow />
                    </Tab>
                    <Tab eventKey="history" title="Change history">
                        <History />
                    </Tab>
                </Tabs>
            </div>
        );
    }
}

export default ConfigurationDetails;
