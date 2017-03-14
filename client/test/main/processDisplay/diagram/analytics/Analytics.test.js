import {expect} from 'chai';
import sinon from 'sinon';
import {createMockComponent, mountTemplate} from 'testHelpers';
import {createAnalyticsRenderer,
        __set__, __ResetDependency__} from 'main/processDisplay/diagram/analytics/Analytics';

describe('<Analytics>', () => {
  let createModal;
  let viewer;
  let Modal;
  let update;
  let setEndEvent;
  let setGateway;
  let resetStatisticData;
  let leaveGatewayAnalysisMode;
  let $document;

  let diagramElement;
  let endEvent;
  let gateway;
  let initialState;
  let gatewayAnalysisState;
  let isBpmnType;
  let Socket;

  const GATEWAY_ANALYSIS_MODE = 'GATEWAY_ANALYSIS_MODE';

  beforeEach(() => {
    const heatmapData = {
      piCount: 7,
      flowNodes: {
        act1: 1,
        act2: 2,
        act3: 3
      }
    };

    diagramElement = {businessObject: {
      type: 'bpmn:Task',
      name: 'Some Task',
      id: 'act2'
    }};

    endEvent = {businessObject: {
      type: 'bpmn:EndEvent',
      name: 'Some End Event',
      id: 'act1'
    }};

    gateway = {businessObject: {
      type: 'bpmn:Gateway',
      name: 'Some Gateway',
      id: 'act3'
    }};

    initialState = {state: {
      heatmap: {
        data: heatmapData
      },
      mode: null,
      selection: {}
    }, diagramRendered: true};

    gatewayAnalysisState = {state: {
      heatmap: {
        data: heatmapData
      },
      mode: GATEWAY_ANALYSIS_MODE,
      selection: {
        endEvent: 'act1'
      }
    }, diagramRendered: true};

    $document = {
      addEventListener: sinon.spy(),
      removeEventListener: sinon.spy()
    };
    __set__('$document', $document);

    setEndEvent = sinon.spy();
    __set__('setEndEvent', setEndEvent);

    setGateway = sinon.spy();
    __set__('setGateway', setGateway);

    resetStatisticData = sinon.spy();
    __set__('resetStatisticData', resetStatisticData);

    leaveGatewayAnalysisMode = sinon.spy();
    __set__('leaveGatewayAnalysisMode', leaveGatewayAnalysisMode);

    __set__('GATEWAY_ANALYSIS_MODE', GATEWAY_ANALYSIS_MODE);

    Modal = createMockComponent('Modal', true);
    Modal.open = sinon.spy();
    createModal = sinon.stub().returns(Modal);
    __set__('createModal', createModal);

    Socket = createMockComponent('Socket', true);
    __set__('Socket', Socket);

    isBpmnType = sinon.stub().returns(false);
    isBpmnType.withArgs(endEvent, 'EndEvent').returns(true);
    isBpmnType.withArgs(gateway, 'Gateway').returns(true);
    __set__('isBpmnType', isBpmnType);

    viewer = {
      get: sinon.stub().returnsThis(),
      on: sinon.spy(),
      addMarker: sinon.spy(),
      removeMarker: sinon.spy(),
      forEach: sinon.stub(),
      getGraphics: sinon.stub().returns({
        querySelector: sinon.stub().returns({
          setAttribute: sinon.spy()
        })
      })
    };

    ({update} = mountTemplate((node, eventsBus) => createAnalyticsRenderer({viewer, node, eventsBus})));
  });

  afterEach(() => {
    __ResetDependency__('createModal');
    __ResetDependency__('isBpmnType');
    __ResetDependency__('setEndEvent');
    __ResetDependency__('setGateway');
    __ResetDependency__('leaveGatewayAnalysisMode');
    __ResetDependency__('GATEWAY_ANALYSIS_MODE');
    __ResetDependency__('$document');
    __ResetDependency__('resetStatisticData');
    __ResetDependency__('Socket');
  });

  it('should do nothing when a non end event is clicked', () => {
    update(initialState);
    viewer.on.firstCall.args[1]({element: diagramElement});

    expect(Modal.open.called).to.eql(false);
  });

  it('should set the end event and open modal when an end event is clicked', () => {
    update(initialState);
    viewer.on.firstCall.args[1]({element: endEvent});

    expect(setEndEvent.calledWith(endEvent)).to.eql(true);
    expect(Modal.open.called).to.eql(true);
  });

  it('should set the piCount property to the counterAll node', () => {
    update(initialState);
    viewer.on.firstCall.args[1]({element: endEvent});

    const counterAll = Socket.getChildrenNode({name: 'body'}).querySelector('td');

    expect(counterAll.textContent).to.eql('7');
  });

  it('should not set an end event in gateway analysis mode', () => {
    update(gatewayAnalysisState);
    viewer.on.lastCall.args[1]({element: endEvent});

    expect(setEndEvent.called).to.eql(false);
  });

  it('should set a gateway when a gateway is clicked', () => {
    update(gatewayAnalysisState);
    viewer.on.lastCall.args[1]({element: gateway});

    expect(setGateway.calledWith(gateway)).to.eql(true);
  });

  it('should reset potentially existing statistics data when a gateway is selected', () => {
    update(gatewayAnalysisState);
    viewer.on.lastCall.args[1]({element: gateway});

    expect(resetStatisticData.called).to.eql(true);
  });

  it('should not set a gateway outside of gateway analysis mode', () => {
    update(initialState);
    viewer.on.firstCall.args[1]({element: gateway});

    expect(setGateway.called).to.eql(false);
  });

  it('should leave gateway analysis mode when esc is pressed', () => {
    update(gatewayAnalysisState);
    $document.addEventListener.lastCall.args[1]({key: 'Escape'});

    expect(leaveGatewayAnalysisMode.called).to.eql(true);
  });

  it('should not leave gateway analysis mode when a gateway is already selected', () => {
    gatewayAnalysisState.state.selection.gateway = 'act3';
    update(gatewayAnalysisState);
    $document.addEventListener.lastCall.args[1]({key: 'Escape'});

    expect(leaveGatewayAnalysisMode.called).to.eql(false);
  });

  it('should highlight end events', () => {
    viewer.forEach.callsArgWith(0, endEvent);
    update(initialState);

    expect(viewer.addMarker.calledWith(endEvent, 'highlight')).to.eql(true);
  });

  it('should highlight gateways in gateway analysis mode', () => {
    viewer.forEach.callsArgWith(0, gateway);
    update(gatewayAnalysisState);

    expect(viewer.addMarker.calledWith(gateway, 'highlight')).to.eql(true);
  });

  it('should highlight the selected elements with a different class', () => {
    gatewayAnalysisState.state.selection.gateway = 'act3';
    update(gatewayAnalysisState);

    expect(viewer.addMarker.calledWith('act3', 'highlight_selected')).to.eql(true);
    expect(viewer.addMarker.calledWith('act1', 'highlight_selected')).to.eql(true);
  });
});
