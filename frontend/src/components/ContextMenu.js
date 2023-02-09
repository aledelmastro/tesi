import React from "react";
import * as PropTypes from "prop-types";
import {Button} from "semantic-ui-react";

function ContextMenu(props) {
    const {setFrom, setTo, getInfo, visible, top, left, showInfo} = props;

    const style = {
        display: visible ? "block" : "none",
        left: left,
        top: top
    }

    return (
        <div id={"contextMenu"} style={style}>
            <Button.Group vertical>
                <Button className={'item'} onClick={setFrom}>Partenza</Button>
                <Button className={'item'} onClick={setTo}>Arrivo</Button>
                {showInfo ? <Button className={'item'} onClick={getInfo}>Info</Button> : <></>}
            </Button.Group>
        </div>
    )
}

ContextMenu.propTypes = {
    setFrom: PropTypes.func.isRequired,
    setTo: PropTypes.func.isRequired,
    getInfo: PropTypes.func.isRequired,
    visible: PropTypes.bool.isRequired,
    left: PropTypes.number.isRequired,
    top: PropTypes.number.isRequired
}

export default ContextMenu;
