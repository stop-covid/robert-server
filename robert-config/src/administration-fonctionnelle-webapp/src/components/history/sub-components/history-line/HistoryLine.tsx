import {Button, Collapse} from "react-bootstrap";
import React, {useState} from "react";
import DatePrinter from "../../../../toolbox/date/date-printer";


export function HistoryLine({historydata} : any) {

    const [open, setOpen] = useState(false);

    return  <>
        <Button
            onClick={() => setOpen(!open)}
            aria-controls="example-collapse-text"
            aria-expanded={open}
            variant={"outline-info"}
            active={open}
        >
            {DatePrinter.stringify(historydata.date)}
        </Button>
        <Collapse in={open}>
            <div id="example-collapse-text">
                <pre>{historydata.message}</pre>
            </div>
        </Collapse>
    </>
}