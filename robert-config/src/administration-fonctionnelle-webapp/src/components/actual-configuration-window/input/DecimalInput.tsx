import React from 'react';
import { FormControl, FormGroup, FormLabel } from 'react-bootstrap';

export function DecimalInput(props: any) {
    return (
        <FormGroup className="px-1" controlId={props.id}>
            <FormLabel>{props.label}</FormLabel>
            <FormControl
                className="form-control"
                type="number"
                size="sm"
                step="0.1"
                name={props.name}
                value={props.value}
                onChange={props.onChange}
                min={props.min}
                max={props.max}
                isInvalid={props.error && props.error.length > 0}
                required
            />

            <FormControl.Feedback type="invalid">
                {props.error && props.error.length > 0 ? (
                    props.error
                ) : (
                    `${props.name} is required.`
                )}
            </FormControl.Feedback>

            {props.max &&
            props.max == props.value &&
            props.validated && (
                <span>
                    {`${props.name} can't be greater than ${props.max}.`}
                </span>
            )}
        </FormGroup>
    );
}
