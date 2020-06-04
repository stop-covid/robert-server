import React from 'react';
import { FormControl, FormGroup, FormLabel } from 'react-bootstrap';

export function TextInput(props: any) {
    return (
        <FormGroup className="px-1" controlId={props.id}>
            <FormLabel>{props.label}</FormLabel>
            <FormControl
                className="form-control"
                type="text"
                size="sm"
                name={props.name}
                value={props.value}
                onChange={props.onChange}
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
        </FormGroup>
    );
}
