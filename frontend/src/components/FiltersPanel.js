import {Menu} from "semantic-ui-react";
import * as PropTypes from "prop-types";
import {useRef, useState} from "react";
import ListWithLegend from "./ListWithLegend";
import CustomFilterGenerator from "./CustomFilterGenerator";

const OPERATORS = ['==', '!=', '>', '>=', '<', '<='];

function FiltersPanel(props) {
    const {scores, features, disabled, onChange, onClick} = props;

    const [activeItem, setActiveItem] = useState('Semplice');
    const activeLayer = useRef("");

    const handleItemClick = (e, {name}) => {
        /*if(name === 'Semplice') {
            opMap.current.clear()
        }*/
        setActiveItem(name);
    }

    return <div id={"layersCard"}>
        <Menu tabular secondary pointing>
            <Menu.Item
                name='Semplice'
                active={activeItem === 'Semplice'}
                onClick={handleItemClick}
            />
            <Menu.Item
                name='Custom'
                active={activeItem === 'Custom'}
                onClick={handleItemClick}
            />
        </Menu>
        {activeItem === 'Semplice' ?
            <ListWithLegend disabled={disabled} onChange={onChange} items={scores.concat(features)}/> :
            <CustomFilterGenerator scores={scores.sort()} features={features.sort()} onClick={onClick} operators={OPERATORS}/>}
    </div>
}

FiltersPanel.propTypes = {
    scores: PropTypes.arrayOf(PropTypes.string).isRequired,
    features: PropTypes.arrayOf(PropTypes.string).isRequired,
    disabled: PropTypes.bool.isRequired,
    onChange: PropTypes.func.isRequired,
}

export default FiltersPanel
