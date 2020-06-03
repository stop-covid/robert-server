import React, { useEffect, useState } from 'react';
// @ts-ignore
import JSONInput from 'react-json-editor-ajrm';
// @ts-ignore
import locale from 'react-json-editor-ajrm/locale/en';
import SubmitModal from '../submit-modal';
import { Button, Form } from 'react-bootstrap';
import {
    getActualConfiguration,
    putNewConfiguration
} from './data-provider/ActualConfigurationDataProvider';
import { ConfigurationNumberInput } from './input/ConfigurationNumberInput';
import { ConfigurationTextInput } from './input/ConfigurationTextInput';

export default function ActualConfigurationWindow(): any {
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
        minSampling: 0,
        b: 0,
        maxSampleSize: 0,
        riskThresholdLow: 0,
        riskThresholdMax: 0,
        riskMin: 0,
        riskMax: 0,
        rssiThreshold: 0,
        tagPeak: 0,
        flagCalib: 0,
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
    const [errors, setErrors] = useState({
        model_name: ''
    });

    const hasErrors = () => Object.keys(errors).length > 0;

    // const handleSubmit = (event: any) => {
    //     const form = event.currentTarget;
    //     if (form.checkValidity() === false) {
    //         // submit();
    //     } else {
    //         event.preventDefault();
    //     }
    //     setValidated(true);
    // };

    const handleCancel = () => {
        setEditable(!editable);
        setValidated(false);
        loadConfiguration();
        errors.model_name = '';
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

    // const handleAppAutonomyChange = (event: any) => {
    //     console.log('event', event);
    //     if (event.target.value == null) {
    //         const _errors = new Map(errors);
    //         _errors.set(
    //             'appAutonomy',
    //             `Please provide a valid ${event.target.name}.`
    //         );
    //         setErrors(_errors);
    //         return;
    //     }

    //     const updateConfiguration = { ...config };
    //     updateConfiguration.accountManagement.appAutonomy = event.target.value;

    //     setConfig(updateConfiguration);
    //     setValidated(true);
    // };

    // const handleChange = (event: any) => {
    //     switch (event.target.name) {
    //         case '':
    //             break;
    //         case '':
    //             break;
    //         case '':
    //             break;
    //         default:
    //             break;
    //     }
    // };

    const handleAccountManagementChange = (event: any) => {
        const target = event.target;
        const _accountManagement = {
            ...config.accountManagement,
            [target.name]: target.value
        };
        const _configuration = { ...config };
        _configuration.accountManagement = _accountManagement;

        setConfig(_configuration);
        setValidated(true);
    };

    const handleProximityTracingChange = (event: any) => {
        const target = event.target;
        const _proximityTracing = {
            ...config.proximityTracing,
            [target.name]: target.value
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
        const _ble = {
            ...config.proximityTracing.ble,
            [target.name]: target.value
        };
        const _configuration = { ...config };
        _configuration.proximityTracing.ble = _ble;

        setConfig(_configuration);
        setValidated(true);
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
            <div>
                <Form noValidate validated={validated}>
                    <Form.Row>
                        <ConfigurationNumberInput
                            as={4}
                            px-5
                            id="appAutonomy"
                            label="Application autonomy"
                            name="appAutonomy"
                            value={config.accountManagement.appAutonomy}
                            onChange={handleAccountManagementChange}
                            error={`Please provide a valid number.`}
                        />
                        <ConfigurationNumberInput
                            id="maxSimultaneousRegister"
                            label="maxSimultaneousRegister"
                            name="maxSimultaneousRegister"
                            value={
                                config.accountManagement.maxSimultaneousRegister
                            }
                            onChange={handleAccountManagementChange}
                        />
                    </Form.Row>

                    <Form.Row>
                        <ConfigurationNumberInput
                            id="checkStatusFrequency"
                            label="checkStatusFrequency"
                            name="checkStatusFrequency"
                            value={
                                config.proximityTracing.app.checkStatusFrequency
                            }
                            onChange={handleAppChange}
                        />
                        <ConfigurationNumberInput
                            id="dataRetentionPeriod"
                            label="dataRetentionPeriod"
                            name="dataRetentionPeriod"
                            value={
                                config.proximityTracing.app.dataRetentionPeriod
                            }
                            onChange={handleAppChange}
                        />
                    </Form.Row>

                    <Form.Row>
                        <ConfigurationNumberInput
                            id="simultaneousContacts"
                            label="simultaneousContacts"
                            name="simultaneousContacts"
                            value={
                                config.proximityTracing.ble.simultaneousContacts
                            }
                            onChange={handleBleChange}
                        />
                    </Form.Row>

                    <Form.Row>
                        <ConfigurationNumberInput
                            id="txGain"
                            label="txGain"
                            name="txGain"
                            value={
                                config.proximityTracing.ble
                                    .signalCalibrationPerModel[0].txGain
                            }
                            onChange={handleSignalCalibrationPerModelChange}
                        />
                        <ConfigurationNumberInput
                            id="rxGain"
                            label="rxGain"
                            name="rxGain"
                            value={
                                config.proximityTracing.ble
                                    .signalCalibrationPerModel[0].rxGain
                            }
                            onChange={handleSignalCalibrationPerModelChange}
                        />
                        <ConfigurationTextInput
                            id="model_name"
                            label="model_name"
                            name="model_name"
                            value={
                                config.proximityTracing.ble
                                    .signalCalibrationPerModel[0].model_name
                            }
                            onChange={handleSignalCalibrationPerModelChange}
                            error={errors.model_name}
                        />
                    </Form.Row>

                    <Form.Row>
                        <ConfigurationNumberInput
                            id="minSampling"
                            label="minSampling"
                            name="minSampling"
                            value={config.proximityTracing.ble.minSampling}
                            onChange={handleBleChange}
                        />
                        <ConfigurationNumberInput
                            id="maxSampleSize"
                            label="maxSampleSize"
                            name="maxSampleSize"
                            value={config.proximityTracing.ble.maxSampleSize}
                            onChange={handleBleChange}
                        />
                        <ConfigurationNumberInput
                            id="p0"
                            label="p0"
                            name="p0"
                            value={config.proximityTracing.ble.p0}
                            onChange={handleBleChange}
                        />
                        <ConfigurationNumberInput
                            id="b"
                            label="b"
                            name="b"
                            value={config.proximityTracing.ble.b}
                            onChange={handleBleChange}
                        />
                    </Form.Row>

                    <Form.Row>
                        <ConfigurationNumberInput
                            id="riskThresholdLow"
                            label="riskThresholdLow"
                            name="riskThresholdLow"
                            value={config.proximityTracing.ble.riskThresholdLow}
                            onChange={handleBleChange}
                        />
                        <ConfigurationNumberInput
                            id="rssiThreshold"
                            label="rssiThreshold"
                            name="rssiThreshold"
                            value={config.proximityTracing.ble.rssiThreshold}
                            onChange={handleBleChange}
                        />
                        <ConfigurationNumberInput
                            id="riskThresholdMax"
                            label="riskThresholdMax"
                            name="riskThresholdMax"
                            value={config.proximityTracing.ble.riskThresholdMax}
                            onChange={handleBleChange}
                        />
                    </Form.Row>

                    <Form.Row>
                        <ConfigurationNumberInput
                            id="riskMin"
                            label="riskMin"
                            name="riskMin"
                            value={config.proximityTracing.ble.riskMin}
                            onChange={handleBleChange}
                        />
                        <ConfigurationNumberInput
                            id="riskMax"
                            label="riskMax"
                            name="riskMax"
                            value={config.proximityTracing.ble.riskMax}
                            onChange={handleBleChange}
                        />
                        <ConfigurationNumberInput
                            id="flagCalib"
                            label="flagCalib"
                            name="flagCalib"
                            value={config.proximityTracing.ble.flagCalib}
                            onChange={handleBleChange}
                        />
                        <ConfigurationTextInput
                            id="flagMode"
                            label="Flag mode"
                            name="flagMode"
                            value={config.proximityTracing.ble.flagMode}
                            onChange={handleBleChange}
                        />
                    </Form.Row>

                    <Form.Row>
                        <ConfigurationNumberInput
                            as={2}
                            id="riskThreshold"
                            label="riskThreshold"
                            name="riskThreshold"
                            value={config.proximityTracing.riskThreshold}
                            onChange={handleProximityTracingChange}
                        />
                        <ConfigurationNumberInput
                            as={2}
                            id="rssi1m"
                            label="rssi1m"
                            name="rssi1m"
                            value={config.proximityTracing.rssi1m}
                            onChange={handleProximityTracingChange}
                        />
                        <ConfigurationNumberInput
                            as={2}
                            id="mu0"
                            label="mu0"
                            name="mu0"
                            value={config.proximityTracing.mu0}
                            onChange={handleProximityTracingChange}
                        />
                        <ConfigurationNumberInput
                            as={2}
                            id="r0"
                            label="r0"
                            name="r0"
                            value={config.proximityTracing.r0}
                            onChange={handleProximityTracingChange}
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
        </React.Fragment>
    );
}
