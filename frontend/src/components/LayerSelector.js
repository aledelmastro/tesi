import {Card, CardContent, CardHeader, Form, FormField, Radio} from "semantic-ui-react";
import * as PropTypes from "prop-types";

function LayerSelector(props) {
    const {debugLayersName, disabled, onChange, active} = props;

    return <div id={"layersCard"}>
        <Card>
            <CardContent>
                <CardHeader>Layers</CardHeader>
                <CardContent>
                    <Form>
                        <FormField><Radio
                            disabled={disabled}
                            value={'score'}
                            onChange={onChange}
                            label={'score'}
                            checked={active === 'score'}
                        /></FormField>
                        {debugLayersName.map(dbn => <FormField><Radio
                            disabled={disabled}
                            value={dbn}
                            onChange={onChange}
                            label={dbn.replace(/_/g, ' ')}
                            checked={active === dbn}
                            /></FormField>
                        )}
                    </Form>
                </CardContent>
            </CardContent>
        </Card>
    </div>
}

LayerSelector.propTypes = {
    debugLayersName: PropTypes.arrayOf(PropTypes.string).isRequired,
    disabled: PropTypes.bool.isRequired,
    onChange: PropTypes.func.isRequired,
    active: PropTypes.string.isRequired
}

export default LayerSelector
