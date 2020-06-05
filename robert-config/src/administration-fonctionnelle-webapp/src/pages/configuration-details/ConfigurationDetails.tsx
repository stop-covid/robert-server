import React, { useEffect } from 'react';
import History from '../../components/history';
import ActualConfigurationWindow from '../../components/actual-configuration-window';
import { Tabs, Tab } from 'react-bootstrap';

function ConfigurationDetails(props: any) {
    return (
        <div>
            <Tabs defaultActiveKey="configuration" id="configuration-details">
                <Tab eventKey="configuration" title="Configuration">
                    <ActualConfigurationWindow token={props.token} />
                </Tab>
                <Tab eventKey="history" title="Change history">
                    <History token={props.token} />
                </Tab>
            </Tabs>
        </div>
    );
}

export default ConfigurationDetails;
