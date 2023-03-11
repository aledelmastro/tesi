import React, {useState} from "react";
import {decodePolyline} from "../mapUtils/MapUtils";
import * as PropTypes from "prop-types";
import {Button, Card} from "semantic-ui-react";

function ResContainer(props) {
    const itin = props.itin;
    const duration = new Date(itin.duration*1000);
    const startTime = new Date(itin.startTime);
    const endTime = new Date(itin.endTime);
    const walkTime = itin.walkTime;
    const generalizedCost = itin.generalizedCost;
    const legs = itin.legs;
    const color = props.color;
    const formula = props.formula;

    return (
        <Card style={{borderLeft: "10px solid "+color}}>
        <div className="resContainer" key={props.id}>
            <div><b>Duration</b>: {duration.toISOString().substr(11, 8)}</div>
            <div><b>From</b>: {startTime.toLocaleString()}</div>
            <div><b>To</b>: {endTime.toLocaleString()}</div>
            <div><b>Total cost</b>: {generalizedCost}</div>
            <div><b>Formula</b>: {formula}</div>
        </div>
        <div style={{width: "fit-content", alignSelf: "end", margin: "0.3em"}}>
            <Button circular toggle icon={props.displayed ? 'close' : 'map outline'} active={props.displayed} onClick={() => {
                props.onDisplay(!props.displayed);
            }}/>
            <Button circular toggle icon={props.pinned ? 'unlock' : 'lock'} active={props.pinned} onClick={() => {
                props.onPin(!props.pinned);
            }}/>
        </div>
        </Card>
    );
}

ResContainer.propTypes = {
    collapsed: PropTypes.bool,
    plotResult: PropTypes.func,
    id: PropTypes.number
}

ResContainer.defaultProps = {
    collapsed: true,
    pinned: false
}

export default ResContainer;
