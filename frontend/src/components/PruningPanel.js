import {Dropdown, Form, Input} from "semantic-ui-react";
import * as PropTypes from "prop-types";

const newScore = (name, threshold, below) => {
    return {
        "name": name,
        "threshold": threshold,
        "below": below
    }
}

const newFeature = (name, presence) => {
    return {
        "name": name,
        "presence": presence
    }
}

const updateBoolMap = (map, key, val) => {
    if (val !== undefined && val !== null && val !== "") map.set(key, val);
    else map.delete(key);
}

const updateMap = (map, key, val) => {
    if (val) map.set(key, val);
    else map.delete(key);
}

const operators = ['>', '<'];

function PruningPanel({scores, features, opMap, valMap}) {
    const opOptions = operators.map(op => ({key: op, text: op, value: op}));
    const yesNoOptions = [
        {key: "S", text: "Sì", value: true},
        {key: "N", text: "No", value: false}
    ];

    return <>
        <div id={"scoreGrid"}>
            {
                scores.map(name =>
                    <>
                    <label>{name.replace(/_/g, ' ')}</label>
                    <Dropdown compact clearable selection
                        options={opOptions}
                        onChange={(e, v) => updateMap(opMap.current, name, v.value)}/>
                    <Input onChange={(e,v) => updateMap(valMap.current, name, v.value)} placeholder={0.001}/>
                    </>
                )
            }
            {
                features.map(name =>
                    <>
                    <label>{name.replace(/_/g, ' ')}</label>
                    <Dropdown compact clearable selection
                         options={yesNoOptions}
                         onChange={(e, v) => updateBoolMap(valMap.current, name, v.value)}/>
                    <div/>
                    </>
                )
            }
        </div>
    </>
}

PruningPanel.propTypes = {
    scores: PropTypes.arrayOf(PropTypes.string).isRequired,
    features: PropTypes.arrayOf(PropTypes.string).isRequired
}

export default PruningPanel;
