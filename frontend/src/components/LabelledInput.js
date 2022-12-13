import React from "react";
import * as PropTypes from "prop-types";

function handleChange(event, callback) {
    callback(event.target.value);
}

function LabelledInput(props) {
    return (
        <div>
            <label>{props.label}</label>
            <input
                name={props.name}
                type={props.type}
                onChange={event => handleChange(event, props.onChange)}
                value={props.value}
                placeholder={props.placeholder}
            />
        </div>
    );
}

LabelledInput.propTypes = {
    onChange: PropTypes.func.isRequired,
    value: PropTypes.any,
    name: PropTypes.string.isRequired,
    type: PropTypes.string,
    placeholder: PropTypes.string,
    label: PropTypes.string,
};

export default LabelledInput;
