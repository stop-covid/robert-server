import React, { useEffect, useState } from 'react';
import {
    getHistory,
    HistoryDataProviderResponse
} from './data-provider/HistoryDataProvider';
import { Container, Col, Row } from 'react-bootstrap';
import { HistoryLine } from './sub-components/history-line/HistoryLine';

export default function History(props: any) {
    const [history, setHistory] = useState([] as JSX.Element[]);

    // Load data
    useEffect(() => {
        const historyRendering = (
            hs: HistoryDataProviderResponse[] | undefined
        ) => {
            var hJSX: JSX.Element[] = [];
            if (!hs) {
                setHistory([]);
            } else {
                hs.map((_history: HistoryDataProviderResponse, index: number) =>
                    hJSX.push(
                        <Row key={index}>
                            <Col>
                                <HistoryLine historydata={_history} />
                            </Col>
                        </Row>
                    )
                );

                setHistory(hJSX);
            }
        };

        getHistory(props.token)
            .then(response => {
                if (response) {
                    historyRendering(response);
                }
            })
            .catch(error => {
                console.error('error has been caught : ', error);
                historyRendering(undefined);
            });
    }, []);

    return (
        <Container fluid={'xl'}>
            <h1 />
            <Row>
                <Col>
                    <div
                        style={{
                            height: '100%',
                            overflowY: 'auto',
                            overflowX: 'hidden'
                        }}
                    >
                        {history}
                    </div>
                </Col>
            </Row>
        </Container>
    );
}
