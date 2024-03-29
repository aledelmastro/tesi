import {requestItinerary} from "../calls/calls";
import * as PropTypes from "prop-types";
import {Button, Accordion, Form, Input, Table, Icon} from "semantic-ui-react"
import {useRef, useState} from "react";
import PruningPanel from "./PruningPanel";

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

async function submitRequest(event, props, features, scores, opMap, valMap, formula, variables/*, preMap*/) {
    event.preventDefault();

    const preF = [];
    const postF = [];
    const preS = [];
    const postS = [];

    scores.forEach(score => {
        if (valMap.has(score) && opMap.has(score)) {
            const s = newScore(score, valMap.get(score), opMap.get(score) === '<');
/*
            if (preMap.has(score))
*/
                preS.push(s);
/*            else
                postS.push(s);*/
        }
    });

    features.forEach(feature => {
        if (valMap.has(feature)) {
            const f = newFeature(feature, valMap.get(feature));
/*
            if (preMap.has(feature) && preMap.get(feature))
*/
                preF.push(f);
/*            else
                postF.push(f);*/
        }
    });

    const body = {
        "scores": postS,
        "features": postF,
        "preScores": preS,
        "preFeatures": preF
    }

    if (formula) {
        body["formula"] = formula;
        body["variables"] = variables;
    }

    props.submit();
    // TODO verificare formato correto
    const res = await requestItinerary(props.from, props.to, body);
    props.setRes(res, formula);
}


function SearchInputWidget(props) {
    const {setFrom, setTo, from, to, features, scores} = props;

    const [formula, setFormula] = useState("");
    const opMap = useRef(new Map());
    const valMap = useRef(new Map());
    const preMap = useRef(new Map());

    const [active, setActive] = useState(-1);

    const featuresAndScores = [];
    features.forEach(f => featuresAndScores.push(f));
    scores.forEach(s => featuresAndScores.push(s));

    return (
        <div id="widgetContainer">
            <Form onSubmit={event => submitRequest(event, props, features, scores, opMap.current, valMap.current, formula, featuresAndScores)}>
                <Form.Input label={"From"} placeholder={"From"} onChange={setFrom} value={from} width={16}/>
                <Form.Input label={"To"} placeholder={"To"} onChange={setTo} value={to} />
                <Form.Input label={"Formula"} placeholder={"Formula"} onChange={(e,v) => setFormula(v.value)} value={formula} />
                <Accordion>
                    <Accordion.Title
                        active={active === 0}
                        index={0}
                        onClick={(e, v) => setActive(s => s === v.index ? -1 : v.index)
                        }
                    >
                        <Icon name='dropdown' />
                        <label>Pruning</label>
                    </Accordion.Title>
                    <Accordion.Content active={active === 0}>
                        <PruningPanel scores={scores.sort()} features={features.sort()} opMap={opMap} valMap={valMap}/>
                    </Accordion.Content>
                </Accordion>
                <Button id={"submit"} color='olive' type="submit">
                    Search
                </Button>
            </Form>
        </div>

    );
}

SearchInputWidget.propTypes = {
    setFrom: PropTypes.func.isRequired,
    setTo: PropTypes.func.isRequired,
    from: PropTypes.string.isRequired,
    to: PropTypes.string.isRequired,
    operators: PropTypes.arrayOf(PropTypes.string).isRequired,
    scores: PropTypes.arrayOf(PropTypes.string).isRequired,
    features: PropTypes.arrayOf(PropTypes.string).isRequired,
    submit: PropTypes.func.isRequired,
    setRes: PropTypes.func.isRequired
}

export default SearchInputWidget;
