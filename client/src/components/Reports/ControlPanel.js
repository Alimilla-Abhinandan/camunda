import React from 'react';
import {Select} from 'components';

import {Filter} from './filter';
import {loadProcessDefinitions} from './service';
import {reportLabelMap} from 'services';

import './ControlPanel.css';

export default class ControlPanel extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      availableDefinitions: [],
      loaded: false
    };

    this.loadAvailableDefinitions();
  }

  loadAvailableDefinitions = async () => {
    const definitions = await loadProcessDefinitions();

    const arr = definitions.map((d) => {
      return ([d.key, d.versions.map((v) => {
        return v;
      })]);
    });

    this.setState({
      availableDefinitions: this.flatten(arr),
      loaded: true
    });
  }

  flatten = arr => {
    return Array.prototype.concat(...Array.prototype.concat(...arr))
  }

  changeDefinition = evt => {
    this.props.onChange('processDefinitionId', evt.target.value);
  }
  changeView = evt => {
    const viewKey = evt.target.value;
    this.props.onChange('view', reportLabelMap.keyToObject(viewKey, reportLabelMap.view));
  }
  changeGroup = evt => {
    const groupByKey = evt.target.value;
    this.props.onChange('groupBy', reportLabelMap.keyToObject(groupByKey, reportLabelMap.groupBy));
  }

  changeVisualization = evt => {
    this.props.onChange('visualization', evt.target.value);
  }

  render() {
    return <div className='ControlPanel'>
      <ul className='ControlPanel__list'>
        <li className='ControlPanel__item ControlPanel__item--select'>
          <label htmlFor='ControlPanel__process-definition' className='ControlPanel__label'>Process definition</label>
          <Select className='ControlPanel__select' name='ControlPanel__process-definition' value={this.props.processDefinitionId} onChange={this.changeDefinition}>
            {addSelectionOption()}
            {this.state.availableDefinitions.map(definition => {
              let key;
              const isObject = typeof(definition) === 'object';
              (isObject) ? key = definition.id : key = definition;
              return <Select.Option value={key} key={key} disabled={!isObject}>{(isObject) ? key : '--'}</Select.Option>
            })}
          </Select>
        </li>
        <li className='ControlPanel__item ControlPanel__item--select'>
          <label htmlFor='ControlPanel__view' className='ControlPanel__label'>View</label>
          <Select className='ControlPanel__select' name='ControlPanel__view' value={reportLabelMap.objectToKey(this.props.view, reportLabelMap.view)} onChange={this.changeView}>
            {addSelectionOption()}
            {renderOptions(reportLabelMap.getOptions('view'))}
          </Select>
        </li>
        <li className='ControlPanel__item ControlPanel__item--select'>
          <label htmlFor='ControlPanel__group-by' className='ControlPanel__label'>Group by</label>
          <Select className='ControlPanel__select' name='ControlPanel__group-by' value={reportLabelMap.objectToKey(this.props.groupBy, reportLabelMap.groupBy)} onChange={this.changeGroup}>
            {addSelectionOption()}
            {renderOptions(reportLabelMap.getOptions('groupBy'))}
          </Select>
        </li>
        <li className='ControlPanel__item ControlPanel__item--select'>
          <label htmlFor='ControlPanel__visualize-as' className='ControlPanel__label'>Visualize as</label>
          <Select className='ControlPanel__select' name='ControlPanel__visualize-as' value={this.props.visualization} onChange={this.changeVisualization}>
            {addSelectionOption()}
            {renderOptions(reportLabelMap.getOptions('visualizeAs'))}
          </Select>
        </li>
        <li className='ControlPanel__item ControlPanel__item--filter'>
          <Filter data={this.props.filter} onChange={this.props.onChange} processDefinitionId={this.props.processDefinitionId} />
        </li>
      </ul>
    </div>
  }
}

function addSelectionOption() {
  return <Select.Option value=''>Please select...</Select.Option>;
}

function renderOptions(options) {
  return options.map(({key, label}) => <Select.Option key={key} value={key}>{label}</Select.Option>);
}
