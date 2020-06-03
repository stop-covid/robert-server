import React from 'react';
import { FormControl, FormGroup, FormLabel } from 'react-bootstrap';

export function ConfigurationNumberInput(props: any) {
    return (
        <FormGroup className="px-1" controlId={props.id}>
            <FormLabel>{props.label}</FormLabel>
            <FormControl
                className="form-control"
                type="number"
                size="sm"
                name={props.name}
                value={props.value}
                onChange={props.onChange}
                required
            />
            <FormControl.Feedback type="invalid">
                {`Please provide a valid ${props.name}.`}
            </FormControl.Feedback>
        </FormGroup>
    );
}
