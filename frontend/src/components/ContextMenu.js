import React from "react";
import * as PropTypes from "prop-types";

function ContextMenu(props) {
    const {setFrom, setTo, visible, top, left} = props;

    const style = {
        display: visible ? "block" : "none",
        left: left,
        top: top
    }

    return(
        <div id={"pointer"} style={style}>
            <button onClick={() => setFrom(top, left)}>FROM</button>
            <button onClick={() => setTo(top, left)}>TO</button>
        </div>
    )
}

ContextMenu.propTypes = {
    setFrom: PropTypes.func.isRequired,
    setTo: PropTypes.func.isRequired,
    visible: PropTypes.bool.isRequired,
    left: PropTypes.number.isRequired,
    top: PropTypes.number.isRequired
}

export default ContextMenu;
