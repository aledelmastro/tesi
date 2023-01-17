import {requestItinerary} from "../calls/calls";
import * as PropTypes from "prop-types";
import LabelledInput from "./LabelledInput";
import {Button, Form, Radio} from "semantic-ui-react"

async function submitRequest(event, props) {
    event.preventDefault();

    props.submit();
    // TODO verificare formato correto
    const res = await requestItinerary(props.from, props.to);
    props.setRes(res);
}

function SearchInputWidget(props) {
    const {setFrom, setTo, from, to} = props;

    return (
        <div id="widgetContainer">
            <Form onSubmit={event => submitRequest(event, props)}>
                <Form.Field>
                    <label>{"Partenza"}</label>
                    <input placeholder='' onChange={setFrom} value={from}/>
                </Form.Field>
                <Form.Field>
                    <label>{"Arrivo"}</label>
                    <input placeholder='' onChange={setTo} value={to}/>
                </Form.Field>
                <Button color='olive' type="submit">
                    Cerca
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
    submit: PropTypes.func.isRequired,
    setRes: PropTypes.func.isRequired
}

export default SearchInputWidget;
