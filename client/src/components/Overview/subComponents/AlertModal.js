/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */

import React from 'react';
import update from 'immutability-helper';

import {
  Modal,
  InfoMessage,
  Button,
  Labeled,
  Input,
  LabeledInput,
  Select,
  Typeahead,
  Message,
  Switch,
  Form
} from 'components';
import {emailNotificationIsEnabled} from './service';
import {getOptimizeVersion} from 'services';

import ThresholdInput from './ThresholdInput';

import {formatters, isDurationReport} from 'services';

const newAlert = {
  name: 'New Alert',
  email: '',
  reportId: '',
  thresholdOperator: '>',
  threshold: '100',
  checkInterval: {
    value: '10',
    unit: 'minutes'
  },
  reminder: null,
  fixNotification: false
};

const defaultReminder = {
  value: '2',
  unit: 'hours'
};

export default function AlertModal(reports) {
  return class AlertModal extends React.Component {
    constructor(props) {
      super(props);

      this.state = {
        ...newAlert,
        invalid: false
      };
    }

    componentDidMount = async () => {
      const alert = this.props.entity;
      if (alert && Object.keys(alert).length) {
        this.updateAlert();
      }

      const version = (await getOptimizeVersion()).split('.');
      version.length = 2;

      this.setState({
        emailNotificationIsEnabled: await emailNotificationIsEnabled(),
        optimizeVersion: version.join('.')
      });
    };

    updateAlert() {
      const alert = this.props.entity;

      this.setState(
        (alert &&
          alert.id && {
            ...alert,
            threshold:
              this.getReportType(alert.reportId) === 'duration'
                ? formatters.convertDurationToObject(alert.threshold)
                : alert.threshold.toString(),
            checkInterval: {
              value: alert.checkInterval.value.toString(),
              unit: alert.checkInterval.unit
            },
            reminder: alert.reminder
              ? {
                  value: alert.reminder.value.toString(),
                  unit: alert.reminder.unit
                }
              : null
          }) ||
          newAlert
      );
    }

    updateReminder = ({target: {checked}}) => {
      if (checked) {
        this.setState({
          reminder: defaultReminder
        });
      } else {
        this.setState({
          reminder: null
        });
      }
    };

    setInvalid = isInvalid => {
      if (this.state.invalid !== isInvalid) {
        this.setState({
          invalid: isInvalid
        });
      }
    };

    confirm = () => {
      this.props.onConfirm({
        ...this.state,
        threshold: formatters.convertDurationToSingleNumber(this.state.threshold)
      });
    };

    isInEditingMode = () => {
      return this.props.entity && this.props.entity.id;
    };

    isThresholdValid = () => {
      const value = this.getThresholdValue();
      return value.trim() && !isNaN(value);
    };

    componentDidUpdate({entity}) {
      if (this.props.entity !== entity) {
        this.updateAlert();
      }
      if (!this.state.name.trim()) {
        this.setInvalid(true);
        return;
      }
      if (
        !this.state.email.match(
          /^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/
        )
      ) {
        // taken from https://www.w3.org/TR/2012/WD-html-markup-20120320/input.email.html#input.email.attrs.value.single
        this.setInvalid(true);
        return;
      }
      if (!this.state.reportId) {
        this.setInvalid(true);
        return;
      }
      if (!this.isThresholdValid()) {
        this.setInvalid(true);
        return;
      }
      if (
        !this.state.checkInterval.value.trim() ||
        isNaN(this.state.checkInterval.value.trim()) ||
        !(this.state.checkInterval.value > 0)
      ) {
        this.setInvalid(true);
        return;
      }
      if (
        this.state.reminder !== null &&
        (!this.state.reminder.value.trim() ||
          isNaN(this.state.reminder.value.trim()) ||
          !this.state.reminder.value > 0)
      ) {
        this.setInvalid(true);
        return;
      }
      this.setInvalid(false);
    }

    getReportType = reportId => {
      const report = reports.find(({id}) => id === reportId);

      if (report) {
        if (isDurationReport(report)) {
          return 'duration';
        }
        return report.data.view.property;
      }
    };

    getThresholdValue = () =>
      typeof this.state.threshold.value !== 'undefined'
        ? this.state.threshold.value
        : this.state.threshold;

    updateReport = ({id}) => {
      const reportType = this.getReportType(id);
      const currentValue = this.getThresholdValue();

      this.setState({
        reportId: id,
        threshold: reportType === 'duration' ? {value: currentValue, unit: 'days'} : currentValue
      });
    };

    render() {
      const {
        name,
        email,
        reportId,
        thresholdOperator,
        threshold,
        checkInterval,
        reminder,
        fixNotification,
        emailNotificationIsEnabled,
        optimizeVersion
      } = this.state;
      return (
        <Modal open={this.props.entity} onClose={this.props.onClose}>
          <Modal.Header>{this.isInEditingMode() ? 'Edit Alert' : 'Create New Alert'}</Modal.Header>
          <Modal.Content>
            <Form horizontal>
              {!emailNotificationIsEnabled && (
                <Message type="warning">
                  The email notification service is not configured. Optimize won't be able to inform
                  you about critical values. Please check out the{' '}
                  <a
                    href={`https://docs.camunda.org/optimize/${optimizeVersion}/technical-guide/setup/configuration/#email`}
                  >
                    Optimize documentation
                  </a>{' '}
                  on how to enable the notification service.
                </Message>
              )}
              <Form.Group>
                <LabeledInput
                  id="name-input"
                  label="Alert Name"
                  value={name}
                  onChange={({target: {value}}) => this.setState({name: value})}
                  autoComplete="off"
                />
              </Form.Group>
              <Form.Group>
                <Labeled label="When Report">
                  <Typeahead
                    initialValue={reports.find(report => report.id === reportId)}
                    placeholder="Select a Report"
                    values={reports}
                    onSelect={this.updateReport}
                    formatter={({name}) => name}
                    noValuesMessage="No number reports have been created"
                  />
                </Labeled>
                <InfoMessage>Alerts only available for reports visualised as numbers</InfoMessage>
              </Form.Group>
              <Form.Group>
                <span>Has a Value</span>
                <Form.InputGroup>
                  <Select
                    value={thresholdOperator}
                    onChange={value => this.setState({thresholdOperator: value})}
                  >
                    <Select.Option value=">">above</Select.Option>
                    <Select.Option value="<">below</Select.Option>
                  </Select>
                  <ThresholdInput
                    id="value-input"
                    value={threshold}
                    onChange={threshold => this.setState({threshold})}
                    type={this.getReportType(reportId)}
                  />
                </Form.InputGroup>
              </Form.Group>
              <Form.Group>
                <Labeled label="Check Report Every">
                  <Form.InputGroup>
                    <Input
                      id="checkInterval-input"
                      value={checkInterval.value}
                      onChange={({target: {value}}) =>
                        this.setState(update(this.state, {checkInterval: {value: {$set: value}}}))
                      }
                    />
                    <Select
                      value={checkInterval.unit}
                      onChange={value =>
                        this.setState(update(this.state, {checkInterval: {unit: {$set: value}}}))
                      }
                    >
                      <Select.Option value="seconds">Seconds</Select.Option>
                      <Select.Option value="minutes">Minutes</Select.Option>
                      <Select.Option value="hours">Hours</Select.Option>
                      <Select.Option value="days">Days</Select.Option>
                      <Select.Option value="weeks">Weeks</Select.Option>
                      <Select.Option value="months">Months</Select.Option>
                    </Select>
                  </Form.InputGroup>
                </Labeled>
              </Form.Group>
              <Form.Group>
                <LabeledInput
                  id="email-input"
                  label="Send Email to"
                  placeholder="Email address"
                  value={email}
                  onChange={({target: {value}}) => this.setState({email: value})}
                />
              </Form.Group>
              <Form.Group noSpacing>
                <label>
                  <Input
                    type="checkbox"
                    checked={fixNotification}
                    onChange={({target: {checked}}) => this.setState({fixNotification: checked})}
                  />
                  Send notification when resolved
                </label>
              </Form.Group>
              <Form.Group noSpacing>
                <fieldset>
                  <legend>
                    <Switch checked={!!reminder} onChange={this.updateReminder} />
                    Send reminder notification
                  </legend>
                  <Labeled label="every">
                    <Form.InputGroup>
                      <Input
                        id="reminder-input"
                        disabled={!reminder}
                        value={reminder ? reminder.value : defaultReminder.value}
                        onChange={({target: {value}}) =>
                          this.setState(update(this.state, {reminder: {value: {$set: value}}}))
                        }
                      />
                      <Select
                        value={reminder ? reminder.unit : defaultReminder.unit}
                        disabled={!reminder}
                        onChange={value =>
                          this.setState(update(this.state, {reminder: {unit: {$set: value}}}))
                        }
                      >
                        <Select.Option value="minutes">Minutes</Select.Option>
                        <Select.Option value="hours">Hours</Select.Option>
                        <Select.Option value="days">Days</Select.Option>
                        <Select.Option value="weeks">Weeks</Select.Option>
                        <Select.Option value="months">Months</Select.Option>
                      </Select>
                    </Form.InputGroup>
                  </Labeled>
                </fieldset>
              </Form.Group>
            </Form>
          </Modal.Content>
          <Modal.Actions>
            <Button onClick={this.props.onClose}>Cancel</Button>
            <Button
              variant="primary"
              color="blue"
              onClick={this.confirm}
              disabled={this.state.invalid}
            >
              {this.isInEditingMode() ? 'Apply Changes' : 'Create Alert'}
            </Button>
          </Modal.Actions>
        </Modal>
      );
    }
  };
}
