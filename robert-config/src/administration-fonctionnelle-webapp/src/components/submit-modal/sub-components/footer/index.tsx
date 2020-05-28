import {Button, Modal} from "react-bootstrap";
import React from "react";
import {Link} from "react-router-dom";

/**
 * Modal footer of a failed Api submission
 * @param setResponse set the response to undefined
 * @param handleClose close the modal
 */
export const FailedFooter = ({setResponse, handleClose} : any) => (
    <Modal.Footer>
        <Button variant={"danger"} onClick={() => {
            // remove response
            setResponse(undefined as any);
            // then close modal
            handleClose()
        }}>Close</Button>

    </Modal.Footer>
)

/**
 * Modal footer of a succeeded Api submission redirection included
 * @param redirect to a certain path, default "/"
 */
export const SucceededFooter = ({redirection = "/"} : any) => (
    <Modal.Footer>
        <Link to={redirection}>
            <Button variant={"outline-success"}>OK !</Button>
        </Link>
    </Modal.Footer>
);