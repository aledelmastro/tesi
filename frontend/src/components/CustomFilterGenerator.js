import {Button, Dropdown, Input, Label, Table} from "semantic-ui-react";
import * as PropTypes from "prop-types";
import {useRef} from "react";

const updateMap = (map, key, val) => {
    if (val) map.set(key, val);
    else map.delete(key);
}

const toSuitableValue = (val) => {
    if (val === "Sì") return "1";
    if (val === "No") return "0";
    return val;
}

function CustomFilterGenerator(props) {
    const {operators, scores, features, onClick} = props;

    const opMap = useRef(new Map());
    const valMap = useRef(new Map());

    const opOptions = operators.map(op => ({key: op, text: op, value: op}));
    const yesNoOptions = [
        {key: "Sì", text: "Sì", value: "Sì"},
        {key: "No", text: "No", value: "No"}
    ];

    const onClickLocal = (e,v) => {
        const filters = [];

        scores.forEach(score => {
            if (valMap.current.has(score) && opMap.current.has(score))
                filters.push([opMap.current.get(score), ['get', score], Number.parseFloat(valMap.current.get(score))]);
        });

        features.forEach(feature => {
            if (valMap.current.has(feature))
                filters.push(['==', ['get', feature], Number.parseInt(valMap.current.get(feature))]);
        });

        console.log(filters);
        onClick(filters);
    }

    return <>
        <Table basic='very' compact>
            <Table.Body>
                {
                    scores.map(name =>
                        <Table.Row>
                            <Table.Cell collapsing>
                                <Label color='olive'>{name.replace(/_/g, ' ')}</Label>
                            </Table.Cell>
                            <Table.Cell collapsing>
                                <Dropdown compact clearable selection
                                          placeholder=''
                                          options={opOptions}
                                          id={name + "OpSelector"}
                                          onChange={(e, v) => updateMap(opMap.current, name, v.value)}
                                          style={{width: "5em"}}
                                />
                            </Table.Cell>
                            <Table.Cell>
                                <Input style={{width: "5em"}}
                                       type={"number"}
                                       onChange={(e, v) => updateMap(valMap.current, name, v.value)}
                                />
                            </Table.Cell>
                        </Table.Row>
                    )
                }
                {
                    features.map(name =>
                        <Table.Row>
                            <Table.Cell colSpan='2' collapsing>
                                <Label color='olive'>{name.replace(/_/g, ' ')}</Label>
                            </Table.Cell>
                            <Table.Cell collapsing>
                                <Dropdown compact clearable selection
                                          placeholder=''
                                          options={yesNoOptions}
                                          id={name + "OpSelector"}
                                          onChange={(e, v) => updateMap(valMap.current, name, toSuitableValue(v.value))}
                                          style={{width: "5em"}}
                                />
                            </Table.Cell>
                        </Table.Row>
                    )
                }
            </Table.Body>
        </Table>
        <Button color='yellow' onClick={onClickLocal}>
            {"Conferma"}
        </Button>
    </>
}

CustomFilterGenerator.propTypes = {
    operators: PropTypes.arrayOf(PropTypes.string).isRequired,
    scores: PropTypes.arrayOf(PropTypes.string).isRequired,
    features: PropTypes.arrayOf(PropTypes.string).isRequired,
    onClick: PropTypes.func.isRequired,
}


export default CustomFilterGenerator
