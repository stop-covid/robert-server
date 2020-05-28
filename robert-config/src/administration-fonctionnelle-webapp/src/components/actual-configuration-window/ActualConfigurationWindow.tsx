import React, {useEffect, useState} from 'react'
import {getActualConfiguration, putNewConfiguration} from "./data-provider/ActualConfigurationDataProvider";
// @ts-ignore
import JSONInput from 'react-json-editor-ajrm';
// @ts-ignore
import locale from 'react-json-editor-ajrm/locale/en';
import SubmitModal from "../submit-modal";
import {Button} from "react-bootstrap";


export default function ActualConfigurationWindow() : any {

    const [config, setConfig] = useState({});
    const [editable, setEditable] = useState(true);

    const [show, setShow] = useState(false);
    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);

    const [submitResponse, setSubmitResponse] = useState();

    const  submitEdit = () => {
        console.log("submit edition")
        handleShow();
        setSubmitResponse(
            putNewConfiguration(config)
                .then(result => {
                    load();
                    return ({isSubmitted: true, message:result })
                })
                .catch(err => ({isSubmitted: false, message:err }))
        );
    }

    const load = () => {
        getActualConfiguration()
            .then(actualConfiguration => {
                console.log("actual configuration from api : ", actualConfiguration)
                setConfig(actualConfiguration)
            })
            .catch(error => {
                console.error("Fail to get actual configuration from api : ", error)
            })
    }

    useEffect(() => {
        load()
    }, [])

    return <>

        <SubmitModal
            showParam={show}
            handleClose={handleClose}
            submitResponse={submitResponse}
            redirection={"/"}
        />

        <Button
            onClick={submitEdit}
            variant={"outline-success"}
            hidden={editable}
        >
            SAVE
        </Button>

        <Button
            onClick={() => setEditable(!editable)}
            variant={"warning"}
            hidden={editable}
        >
            CANCEL
        </Button>


        <Button
            onClick={() => setEditable(!editable)}
            variant={"primary"}
            hidden={!editable}
        >
            EDIT
        </Button>

        <JSONInput
            id          = 'a_unique_id'
            placeholder = { config }
            locale      = { locale }
            height      = '100%'
            colors      = {{string:"orange", background:"white", keys:"black", number:"red"}}
            onChange    = {(e : any) => setConfig(e.jsObject)}
            viewOnly    = {editable}
        />

    </>
}