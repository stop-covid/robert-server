import React, { useEffect, useState } from 'react';
// @ts-ignore
import locale from 'react-json-editor-ajrm/locale/en';
import SubmitModal from '../submit-modal';
import {
    Container,
    Row,
    Col,
    Button,
    Form,
    FormGroup,
    FormLabel
} from 'react-bootstrap';
import {
    getActualConfiguration,
    putNewConfiguration
} from './data-provider/ActualConfigurationDataProvider';
import { DecimalInput } from './input/DecimalInput';
import { NumberInput } from './input/NumberInput';
import { TextInput } from './input/TextInput';

export default function ActualConfigurationWindow(): any {
    const minValues = {
        appAutonomy: 0,
        maxSimultaneousRegister: 1,
        checkStatusFrequency: 0,
        dataRetentionPeriod: 0,
        simultaneousContacts: 0,
        p0: -127,
        maxSampleSize: 0,
        riskThresholdLow: 0,
        riskThresholdHigh: 0,
        riskMin: 0,
        riskMax: 0,
        rssiThreshold: -127,
        tagPeak: 0,
        flagMode: '',
        tWin: 0,
        tOverlap: 0,
        dThreshold: 0,
        riskThreshold: 0,
        rssi1m: -127,
        r0: 0
    };

    const maxValues = {
        txGain: 0,
        rxGain: 0,
        p0: 0,
        maxSampleSize: 112,
        rssiThreshold: 0,
        rssi1m: 0,
        r0: 1
    };

    const accountManagement = {
        appAutonomy: 0,
        maxSimultaneousRegister: 0
    };

    const app = {
        checkStatusFrequency: 0,
        dataRetentionPeriod: 0
    };

    const ble = {
        simultaneousContacts: 0,
        signalCalibrationPerModel: [
            {
                txGain: 0,
                rxGain: 0,
                model_name: ''
            }
        ],
        delta: [],
        p0: 0,
        b: 0,
        maxSampleSize: 0,
        riskThresholdLow: 0,
        riskThresholdHigh: 0,
        riskMin: 0,
        riskMax: 0,
        rssiThreshold: 0,
        tagPeak: 0,
        flagCalib: false,
        flagMode: '',
        tWin: 0,
        tOverlap: 0,
        dThreshold: 0
    };

    const proximityTracing = {
        app: app,
        ble: ble,
        riskThreshold: 0,
        rssi1m: 0,
        mu0: 0,
        r0: 0
    };

    const functionalConfiguration = {
        accountManagement: accountManagement,
        proximityTracing: proximityTracing
    };

    const [config, setConfig] = useState(functionalConfiguration);
    const [editable, setEditable] = useState(true);

    const [show, setShow] = useState(false);
    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);

    const [submitResponse, setSubmitResponse] = useState();
    const [validated, setValidated] = useState(false);

    const defaultErrors = {
        model_name: '',
        riskThresholdLow: '',
        riskThresholdHigh: '',
        riskMin: '',
        riskMax: '',
        rssiThreshold: ''
    };
    const [errors, setErrors] = useState(defaultErrors);

    const hasErrors = () => Object.keys(errors).length > 0;

    const handleCancel = () => {
        setEditable(!editable);
        setValidated(false);
        loadConfiguration();
        setErrors(defaultErrors);
    };

    const handleSubmit = (event: any) => {
        if (validated) {
            handleShow();
            setSubmitResponse(
                putNewConfiguration(config)
                    .then(result => {
                        loadConfiguration();
                        return { isSubmitted: true, message: result };
                    })
                    .catch(err => ({ isSubmitted: false, message: err }))
            );
        }
    };

    const loadConfiguration = () => {
        getActualConfiguration()
            .then(configuration => {
                setConfig({ ...configuration });
            })
            .catch(error => {
                console.error(
                    'Fail to get actual configuration from api : ',
                    error
                );
            });
    };

    const handleAccountManagementChange = (event: any) => {
        const { name, value } = event.target;
        const _accountManagement = {
            ...config.accountManagement,
            [name]: value
        };
        const _configuration = { ...config };
        _configuration.accountManagement = _accountManagement;

        setConfig(_configuration);
        setValidated(true);
    };

    const handleProximityTracingChange = (event: any) => {
        const { name, value } = event.target;
        const _proximityTracing = {
            ...config.proximityTracing,
            [name]: value
        };
        const _configuration = { ...config };
        _configuration.proximityTracing = _proximityTracing;

        setConfig(_configuration);
        setValidated(true);
    };

    const handleAppChange = (event: any) => {
        const target = event.target;
        const _app = {
            ...config.proximityTracing.app,
            [target.name]: target.value
        };
        const _configuration = { ...config };
        _configuration.proximityTracing.app = _app;

        setConfig(_configuration);
        setValidated(true);
    };

    const handleBleChange = (event: any) => {
        const target = event.target;
        validateBle(target);
        const _ble = {
            ...config.proximityTracing.ble,
            [target.name]: target.value
        };
        const _configuration = { ...config };
        _configuration.proximityTracing.ble = _ble;

        setConfig(_configuration);
        setValidated(true);
    };

    const handleFlagCalibChange = (event: any) => {
        const _configuration = { ...config };
        _configuration.proximityTracing.ble.flagCalib = !_configuration
            .proximityTracing.ble.flagCalib;

        setConfig(_configuration);
        setValidated(true);
    };

    const validateBle = (target: any) => {
        switch (target.name) {
            case 'riskThresholdLow':
                errors.riskThresholdLow = initErrorIfGreater(
                    target,
                    config.proximityTracing.ble.riskThresholdHigh
                );
                break;

            case 'riskThresholdHigh':
                errors.riskThresholdHigh = initErrorIfLower(
                    target,
                    config.proximityTracing.ble.riskThresholdLow
                );
                break;
            case 'riskMin':
                errors.riskMin = initErrorIfGreater(
                    target,
                    config.proximityTracing.ble.riskMax
                );
                break;

            case 'riskMax':
                errors.riskMax = initErrorIfLower(
                    target,
                    config.proximityTracing.ble.riskMin
                );
                break;

            default:
                break;
        }
    };

    const initErrorIfGreater = (target: any, max: number) => {
        if (target.value > max) {
            return `This value can't be greater than ${max}.`;
        }
        return '';
    };

    const initErrorIfLower = (target: any, min: number) => {
        if (target.value < min) {
            return `This value can't be lower than ${min}.`;
        }
        return '';
    };

    const handleSignalCalibrationPerModelChange = (event: any) => {
        const target = event.target;
        validateModelName(target);
        const _signalCalibration = {
            ...config.proximityTracing.ble.signalCalibrationPerModel[0],
            [target.name]: target.value
        };
        const _configuration = { ...config };
        _configuration.proximityTracing.ble.signalCalibrationPerModel[0] = _signalCalibration;

        setConfig(_configuration);
        setValidated(true);
    };

    const validateModelName = (target: any) => {
        if (target.name === 'model_name') {
            const modelName = target.value.toUpperCase();
            errors.model_name = '';
            if (
                modelName.length > 0 &&
                modelName !== 'ANDROID' &&
                modelName !== 'IPHONE'
            ) {
                errors.model_name = 'Model name must be Android or Iphone.';
            }
        }
    };

    useEffect(() => {
        loadConfiguration();
    }, []);

    return (
        <React.Fragment>
            <Container fluid={'xl'}>
                <Row>
                    <Col>
                        <div>
                            <Form noValidate validated={validated}>
                                <Form.Row>
                                    <NumberInput
                                        as={4}
                                        px-5
                                        id="appAutonomy"
                                        label="Application autonomy"
                                        name="appAutonomy"
                                        value={
                                            config.accountManagement.appAutonomy
                                        }
                                        onChange={handleAccountManagementChange}
                                        min={minValues.appAutonomy}
                                        validated={validated}
                                    />
                                    <NumberInput
                                        id="maxSimultaneousRegister"
                                        label="maxSimultaneousRegister"
                                        name="maxSimultaneousRegister"
                                        value={
                                            config.accountManagement
                                                .maxSimultaneousRegister
                                        }
                                        onChange={handleAccountManagementChange}
                                        min={minValues.maxSimultaneousRegister}
                                        validated={validated}
                                    />
                                </Form.Row>

                                <Form.Row>
                                    <NumberInput
                                        id="checkStatusFrequency"
                                        label="checkStatusFrequency"
                                        name="checkStatusFrequency"
                                        value={
                                            config.proximityTracing.app
                                                .checkStatusFrequency
                                        }
                                        onChange={handleAppChange}
                                        min={minValues.checkStatusFrequency}
                                        validated={validated}
                                    />
                                    <NumberInput
                                        id="dataRetentionPeriod"
                                        label="dataRetentionPeriod"
                                        name="dataRetentionPeriod"
                                        value={
                                            config.proximityTracing.app
                                                .dataRetentionPeriod
                                        }
                                        onChange={handleAppChange}
                                        min={minValues.dataRetentionPeriod}
                                        validated={validated}
                                    />
                                </Form.Row>

                                <Form.Row>
                                    <NumberInput
                                        id="simultaneousContacts"
                                        label="simultaneousContacts"
                                        name="simultaneousContacts"
                                        value={
                                            config.proximityTracing.ble
                                                .simultaneousContacts
                                        }
                                        onChange={handleBleChange}
                                        min={minValues.simultaneousContacts}
                                        validated={validated}
                                    />
                                </Form.Row>

                                <Form.Row>
                                    <NumberInput
                                        id="txGain"
                                        label="txGain"
                                        name="txGain"
                                        value={
                                            config.proximityTracing.ble
                                                .signalCalibrationPerModel[0]
                                                .txGain
                                        }
                                        onChange={
                                            handleSignalCalibrationPerModelChange
                                        }
                                        max={maxValues.txGain}
                                    />
                                    <NumberInput
                                        id="rxGain"
                                        label="rxGain"
                                        name="rxGain"
                                        value={
                                            config.proximityTracing.ble
                                                .signalCalibrationPerModel[0]
                                                .rxGain
                                        }
                                        onChange={
                                            handleSignalCalibrationPerModelChange
                                        }
                                        max={maxValues.rxGain}
                                    />
                                    <TextInput
                                        id="model_name"
                                        label="model_name"
                                        name="model_name"
                                        value={
                                            config.proximityTracing.ble
                                                .signalCalibrationPerModel[0]
                                                .model_name
                                        }
                                        onChange={
                                            handleSignalCalibrationPerModelChange
                                        }
                                    />
                                </Form.Row>

                                <Form.Row>
                                    <NumberInput
                                        id="maxSampleSize"
                                        label="maxSampleSize"
                                        name="maxSampleSize"
                                        value={
                                            config.proximityTracing.ble
                                                .maxSampleSize
                                        }
                                        onChange={handleBleChange}
                                        max={maxValues.maxSampleSize}
                                    />
                                    <NumberInput
                                        id="p0"
                                        label="p0"
                                        name="p0"
                                        value={config.proximityTracing.ble.p0}
                                        onChange={handleBleChange}
                                        min={minValues.p0}
                                        max={maxValues.p0}
                                    />
                                    <NumberInput
                                        id="b"
                                        label="b"
                                        name="b"
                                        value={config.proximityTracing.ble.b}
                                        onChange={handleBleChange}
                                    />
                                </Form.Row>

                                <Form.Row>
                                    <NumberInput
                                        id="riskThresholdLow"
                                        label="riskThresholdLow"
                                        name="riskThresholdLow"
                                        value={
                                            config.proximityTracing.ble
                                                .riskThresholdLow
                                        }
                                        onChange={handleBleChange}
                                        min={minValues.riskThresholdLow}
                                        error={errors.riskThresholdLow}
                                        validated={validated}
                                    />
                                    <NumberInput
                                        as={2}
                                        id="riskThreshold"
                                        label="riskThreshold"
                                        name="riskThreshold"
                                        value={
                                            config.proximityTracing
                                                .riskThreshold
                                        }
                                        onChange={handleProximityTracingChange}
                                        min={minValues.riskThreshold}
                                    />
                                    <DecimalInput
                                        id="riskThresholdHigh"
                                        label="riskThresholdHigh"
                                        name="riskThresholdHigh"
                                        value={
                                            config.proximityTracing.ble
                                                .riskThresholdHigh
                                        }
                                        onChange={handleBleChange}
                                        min={minValues.riskThresholdHigh}
                                        error={errors.riskThresholdHigh}
                                        validated={validated}
                                    />
                                </Form.Row>

                                <Form.Row>
                                    <NumberInput
                                        id="riskMin"
                                        label="riskMin"
                                        name="riskMin"
                                        value={
                                            config.proximityTracing.ble.riskMin
                                        }
                                        onChange={handleBleChange}
                                        min={minValues.riskMin}
                                        error={errors.riskMin}
                                        validated={validated}
                                    />
                                    <TextInput
                                        id="tagPeak"
                                        label="tagPeak"
                                        name="tagPeak"
                                        value={
                                            config.proximityTracing.ble.tagPeak
                                        }
                                        onChange={handleBleChange}
                                    />
                                    <NumberInput
                                        id="riskMax"
                                        label="riskMax"
                                        name="riskMax"
                                        value={
                                            config.proximityTracing.ble.riskMax
                                        }
                                        onChange={handleBleChange}
                                        min={minValues.riskMax}
                                        error={errors.riskMax}
                                        validated={validated}
                                    />
                                    <FormGroup
                                        className="px-1"
                                        controlId="flagCalib"
                                    >
                                        <FormLabel />
                                        <Form.Check
                                            type="switch"
                                            id="flagCalib"
                                            label="flagCalib"
                                            checked={
                                                config.proximityTracing.ble
                                                    .flagCalib
                                            }
                                            onChange={handleFlagCalibChange}
                                        />
                                    </FormGroup>
                                    <TextInput
                                        id="flagMode"
                                        label="Flag mode"
                                        name="flagMode"
                                        value={
                                            config.proximityTracing.ble.flagMode
                                        }
                                        onChange={handleBleChange}
                                    />
                                </Form.Row>

                                <Form.Row>
                                    <DecimalInput
                                        id="rssiThreshold"
                                        label="rssiThreshold"
                                        name="rssiThreshold"
                                        value={
                                            config.proximityTracing.ble
                                                .rssiThreshold
                                        }
                                        onChange={handleBleChange}
                                        min={minValues.rssiThreshold}
                                        max={maxValues.rssiThreshold}
                                        validated={validated}
                                    />
                                    <NumberInput
                                        as={2}
                                        id="rssi1m"
                                        label="rssi1m"
                                        name="rssi1m"
                                        value={config.proximityTracing.rssi1m}
                                        onChange={handleProximityTracingChange}
                                        min={minValues.rssi1m}
                                        max={maxValues.rssi1m}
                                    />
                                    <NumberInput
                                        as={2}
                                        id="mu0"
                                        label="mu0"
                                        name="mu0"
                                        value={config.proximityTracing.mu0}
                                        onChange={handleProximityTracingChange}
                                    />
                                    <DecimalInput
                                        as={2}
                                        id="r0"
                                        label="r0"
                                        name="r0"
                                        value={config.proximityTracing.r0}
                                        onChange={handleProximityTracingChange}
                                        min={minValues.r0}
                                        max={maxValues.r0}
                                    />
                                </Form.Row>

                                <Button
                                    onClick={handleSubmit}
                                    variant={'outline-success'}
                                    disabled={!validated}
                                >
                                    Save
                                </Button>

                                <Button
                                    onClick={handleCancel}
                                    variant={'warning'}
                                    disabled={!validated}
                                >
                                    Cancel
                                </Button>
                            </Form>

                            <SubmitModal
                                showParam={show}
                                handleClose={handleClose}
                                submitResponse={submitResponse}
                                redirection={'/'}
                            />
                        </div>
                    </Col>
                </Row>
            </Container>
        </React.Fragment>
    );
}
