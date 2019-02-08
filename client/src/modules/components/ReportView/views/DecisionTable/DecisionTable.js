import React from 'react';

import './DecisionTable.scss';
import 'dmn-js/dist/assets/dmn-js-decision-table.css';

import Viewer from 'dmn-js';
import createHitsColumnAddon from './HitsColumnAddon';
import DmnJsPortal from './DmnJsPortal';

export default class DecisionTable extends React.Component {
  state = {
    entryPoints: {rules: {}}
  };

  container = React.createRef();

  async componentDidMount() {
    const {
      configuration: {xml},
      decisionDefinitionKey
    } = this.props.report.data;

    const {entryPoints, Addon: HitsColumn} = createHitsColumnAddon();

    this.viewer = new Viewer({
      container: this.container.current,
      decisionTable: {additionalModules: [HitsColumn]}
    });

    this.viewer.importXML(xml, () =>
      this.viewer.open(
        this.viewer
          .getViews()
          .find(
            ({type, element: {id}}) => type === 'decisionTable' && id === decisionDefinitionKey
          ),
        () => this.setState({entryPoints})
      )
    );
  }

  renderRuleCell = ruleId => {
    const {result, decisionInstanceCount} = this.props.report;

    const resultNumber = result[ruleId] || 0;
    const percentage = Math.round((resultNumber / decisionInstanceCount) * 1000) / 10;

    return (
      <DmnJsPortal key={ruleId} renderIn={this.state.entryPoints.rules[ruleId]}>
        <i>
          {resultNumber} ({percentage}%)
        </i>
      </DmnJsPortal>
    );
  };

  render() {
    const {rules, summary} = this.state.entryPoints;
    const {decisionInstanceCount, result} = this.props.report;

    const hitCount = Object.values(result).reduce((sum, current) => sum + current, 0);

    return (
      <div ref={this.container} className="DecisionTable">
        {Object.keys(rules).map(this.renderRuleCell)}
        <DmnJsPortal renderIn={summary}>
          <b>
            {decisionInstanceCount} Evaluations
            {hitCount > decisionInstanceCount && ` / ${hitCount} Hits`}
          </b>
        </DmnJsPortal>
      </div>
    );
  }
}
