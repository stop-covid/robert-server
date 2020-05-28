import {Alert, Modal, Spinner} from "react-bootstrap";
import React from "react";

/**
 * Modal body Succeeded response of api call
 */
export const FailedBody = (props : any) => (
    <Modal.Body>
        <Alert variant={"danger"}>
            <Alert.Heading>Failed !</Alert.Heading>
            <p>{props.children}</p>
        </Alert>
    </Modal.Body>
)

/**
 * Modal body Succeeded response of api call
 */
export const SucceededBody = (props : any) => {
    return <Modal.Body>
        <Alert variant={"success"}>
            <Alert.Heading></Alert.Heading>
            <p>{props.children}</p>
        </Alert>
    </Modal.Body>
}

/**
 * Modal body waiting the response of api call
 */
export const WaitingBody = () => (
    <Modal.Body>
        <Alert variant={"info"}>
            <Alert.Heading>Loading...</Alert.Heading>
            <Spinner  style={{display:"block", margin: "auto"}} animation="grow" variant="primary"/>
        </Alert>
    </Modal.Body>
)
