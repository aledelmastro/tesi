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

    console.log(props.pinned);

    return (
        <Card>
        <div
            className="resContainer"
            key={props.id} onClick={() => {
            let points = [];
            legs.forEach(leg => {
                points = points.concat(decodePolyline(leg.legGeometry.points).map(point => [point.lng, point.lat]));
            });
            props.plotResult(points, props.id);
        }}>
            <div>Durata: {duration.toISOString().substr(11, 8)}</div>
            <div>Inizio: {startTime.toLocaleString()}</div>
            <div>Fine: {endTime.toLocaleString()}</div>
            <div>Costo: {generalizedCost}</div>
        </div>
        <div style={{width: "fit-content", alignSelf: "end", margin: "0.3em"}}>
            <Button circular toggle icon={'pin'} active={props.pinned} onClick={() => {
                props.onClick(itin, !props.pinned);
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
