import {Dropdown, Label} from "semantic-ui-react";
import {paint} from "../mapUtils/MapUtils";
import * as PropTypes from "prop-types";

function ListWithLegend(props) {
    const {items, disabled, onChange} = props;

    const p = paint('');
    const conditions = [];
    for (let i = 1; i < p.length; i++) {
        if (i % 2 !== 0) {
            if (p[i] instanceof Array) {
                if (p[i][2] === undefined)
                    conditions.push({cond: "Not available ", color: p[i + 1]});
                else
                    conditions.push({cond: p[i][0] + p[i][2], color: p[i + 1]});
            } else {
                conditions.push({cond: "Other ", color: p[i]});
            }
        }
    }

    const options = items.map(dbn => ({
        key: dbn,
        text: dbn.replace(/_/g, ' '),
        value: dbn
    }));

    return <>
        <Dropdown clearable search selection
                  placeholder='Layer'
                  options={options}
                  disabled={disabled}
                  id={"layerSelector"}
                  onChange={onChange}
        />
        {
            conditions.map(c =>
                <Label circular style={{backgroundColor: c["color"]}} key={c["cond"]}>
                    {c["cond"]}
                </Label>
            )
        }
    </>
}

ListWithLegend.propTypes = {
    items: PropTypes.arrayOf(PropTypes.string).isRequired,
    disabled: PropTypes.bool.isRequired,
    onChange: PropTypes.func.isRequired
}

export default ListWithLegend
