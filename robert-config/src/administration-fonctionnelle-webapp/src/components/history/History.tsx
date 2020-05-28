import React, {useEffect, useState} from "react";
import {getHistory, HistoryDataProviderResponse} from "./data-provider/HistoryDataProvider";
import {Col, Row} from "react-bootstrap";
import {HistoryLine} from "./sub-components/history-line/HistoryLine";

export default function History() {
    console.log("rendering <History/>")

    const [history, setHistory] = useState([] as JSX.Element[]);


    // Load data
    useEffect(() => {
        console.log("useEffect is processing")
        const historyRendering = (hs : HistoryDataProviderResponse[] | undefined) => {
            var hJSX : JSX.Element[] = []
            if(!hs) {
                setHistory([])
            } else {
                console.log("hs :", hs)
                hs.forEach((h :HistoryDataProviderResponse ) => hJSX.push(
                    <Row>
                        <Col>
                            <HistoryLine historydata = {h} />
                        </Col>
                    </Row>
                ))

                setHistory(hJSX);
            }
        }

        getHistory().then(response => {
            console.log("response : ", response)
            if(response) historyRendering(response)
        })
            .catch(error => {
                console.error("error has been caught : ", error)
                historyRendering(undefined)
            })
            .finally(() =>
                console.log("finally get history data from ")
            )
    }, [])



    return (
        <div style={{height:"100%", overflowY:"auto", overflowX:"hidden"}}>
            {history}
        </div>
    );
}
