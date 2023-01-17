import React from "react";
import * as PropTypes from "prop-types";
import {Button, TextArea, Label, Input} from 'semantic-ui-react'

function handleChange(event, callback) {
    callback(event.target.value);
}

function LabelledInput(props) {
    return (
        <div>
            <label>{props.label}</label>
            <Input
                placeholder={props.placeholder}
                onChange={event => handleChange(event, props.onChange)}
                value={props.value}
            />
        </div>
    );
}

LabelledInput.propTypes = {
    onChange: PropTypes.func.isRequired,
    value: PropTypes.any,
    placeholder: PropTypes.string,
    label: PropTypes.string,
};

export default LabelledInput;
