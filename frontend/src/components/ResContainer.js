import React from "react";
import {decodePolyline} from "../mapUtils/MapUtils";
import * as PropTypes from "prop-types";
import {Card} from "semantic-ui-react";

function ResContainer(props) {
    const itin = props.itin;
    const duration = new Date(itin.duration*1000);
    const startTime = new Date(itin.startTime);
    const endTime = new Date(itin.endTime);
    const walkTime = itin.walkTime;
    const generalizedCost = itin.generalizedCost;
    const legs = itin.legs;
    return (
        <Card style={style}>
        <div className="resContainer" key={props.id} onClick={() => {
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
        </Card>
    );
}

const style = {
    width: "100%",
    padding: 10
}

ResContainer.propTypes = {
    collapsed: PropTypes.bool,
    plotResult: PropTypes.func,
    id: PropTypes.number
}

ResContainer.defaultProps = {
    collapsed: true
}

export default ResContainer;
