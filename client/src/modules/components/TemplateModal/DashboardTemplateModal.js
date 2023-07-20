/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */

import React, {useState, useEffect} from 'react';

import {t} from 'translation';
import {getOptimizeProfile} from 'config';

import TemplateModal from './TemplateModal';

import processPerformance from './images/processPerformance.png';
import humanPerformance from './images/humanPerformance.png';
import humanBottleneckAnalysis from './images/humanBottleneckAnalysis.png';
import portfolioPerformance from './images/portfolioPerformance.png';
import operationsMonitoring from './images/operationsMonitoring.png';
import instantPreviewDashboard from './images/instantPreviewDashboard.png';

export default function DashboardTemplateModal({onClose}) {
  const [optimizeProfile, setOptimizeProfile] = useState();
  const [optimizeProfileLoaded, setOptimizeProfileLoaded] = useState(false);

  useEffect(() => {
    (async () => {
      setOptimizeProfile(await getOptimizeProfile());
      setOptimizeProfileLoaded(true);
    })();
  }, []);

  let templateGroups = [
    {
      name: 'blankGroup',
      templates: [{name: 'blank', disableDescription: true}],
    },
    {
      name: 'singleProcessGroup',
      templates: [
        {
          name: 'instantPreviewDashboard',
          disableDescription: true,
          img: instantPreviewDashboard,
          config: [
            {
              position: {
                x: 3,
                y: 2,
              },
              dimensions: {
                width: 5,
                height: 2,
              },
              type: 'optimize_report',
              report: {
                name: t('instantDashboard.report.IP_TileText_BusinessOperations_Report1'),
                data: {
                  configuration: {
                    aggregationTypes: [
                      {
                        type: 'avg',
                        value: null,
                      },
                    ],
                    userTaskDurationTimes: ['total'],
                    sorting: {
                      by: 'key',
                      order: 'desc',
                    },
                  },
                  filter: [
                    {
                      type: 'runningInstancesOnly',
                      data: null,
                      filterLevel: 'instance',
                      appliedTo: ['all'],
                    },
                  ],
                  view: {
                    entity: 'processInstance',
                    properties: ['frequency'],
                  },
                  groupBy: {
                    type: 'none',
                    value: null,
                  },
                  visualization: 'number',
                },
              },
            },
            {
              position: {
                x: 8,
                y: 2,
              },
              dimensions: {
                width: 5,
                height: 2,
              },
              type: 'optimize_report',
              report: {
                name: t('instantDashboard.report.IP_TileText_BusinessOperations_Report2'),
                data: {
                  configuration: {
                    color: '#1991c8',
                    aggregationTypes: [
                      {
                        type: 'avg',
                        value: null,
                      },
                    ],
                    userTaskDurationTimes: ['total'],
                  },
                  filter: [
                    {
                      type: 'instanceStartDate',
                      data: {
                        type: 'rolling',
                        start: {
                          value: 7,
                          unit: 'days',
                        },
                        end: null,
                        includeUndefined: false,
                        excludeUndefined: false,
                      },
                      filterLevel: 'instance',
                      appliedTo: ['all'],
                    },
                  ],
                  view: {
                    entity: 'processInstance',
                    properties: ['frequency'],
                  },
                  groupBy: {
                    type: 'none',
                    value: null,
                  },
                  visualization: 'number',
                },
              },
            },
            {
              position: {
                x: 13,
                y: 2,
              },
              dimensions: {
                width: 5,
                height: 2,
              },
              type: 'optimize_report',
              report: {
                name: t('instantDashboard.report.IP_TileText_BusinessOperations_Report3'),
                data: {
                  configuration: {
                    aggregationTypes: [
                      {
                        type: 'sum',
                        value: null,
                      },
                      {
                        type: 'percentile',
                        value: 75.0,
                      },
                    ],
                    userTaskDurationTimes: ['total'],
                    precision: 1,
                  },
                  filter: [
                    {
                      type: 'instanceEndDate',
                      data: {
                        type: 'rolling',
                        start: {
                          value: 7,
                          unit: 'days',
                        },
                        end: null,
                        includeUndefined: false,
                        excludeUndefined: false,
                      },
                      filterLevel: 'instance',
                      appliedTo: ['all'],
                    },
                  ],
                  view: {
                    entity: 'processInstance',
                    properties: ['frequency'],
                  },
                  groupBy: {
                    type: 'none',
                    value: null,
                  },
                  visualization: 'number',
                },
              },
            },
            {
              position: {
                x: 13,
                y: 4,
              },
              dimensions: {
                width: 5,
                height: 2,
              },
              type: 'optimize_report',
              report: {
                name: t('instantDashboard.report.IP_TileText_BusinessOperations_Report5'),
                data: {
                  configuration: {
                    color: '#1991c8',
                    aggregationTypes: [
                      {
                        type: 'avg',
                        value: null,
                      },
                    ],
                    userTaskDurationTimes: ['total'],
                  },
                  filter: [
                    {
                      type: 'runningInstancesOnly',
                      data: null,
                      filterLevel: 'instance',
                      appliedTo: ['all'],
                    },
                  ],
                  view: {
                    entity: 'incident',
                    properties: ['frequency'],
                  },
                  groupBy: {
                    type: 'none',
                    value: null,
                  },
                  visualization: 'number',
                },
              },
            },
            {
              position: {
                x: 3,
                y: 4,
              },
              dimensions: {
                width: 10,
                height: 3,
              },
              type: 'optimize_report',
              report: {
                name: t('instantDashboard.report.IP_TileText_BusinessOperations_Report4'),
                data: {
                  configuration: {
                    aggregationTypes: [
                      {
                        type: 'avg',
                        value: null,
                      },
                    ],
                    userTaskDurationTimes: ['total'],
                  },
                  view: {
                    entity: 'incident',
                    properties: ['frequency'],
                  },
                  groupBy: {
                    type: 'flowNodes',
                    value: null,
                  },
                  visualization: 'heat',
                },
              },
            },
            {
              position: {
                x: 3,
                y: 7,
              },
              dimensions: {
                width: 10,
                height: 2,
              },
              type: 'optimize_report',
              report: {
                name: t('instantDashboard.report.IP_TileText_BusinessOperations_Report6'),
                data: {
                  configuration: {
                    color: '#DB3E00',
                    aggregationTypes: [
                      {
                        type: 'avg',
                        value: null,
                      },
                    ],
                    userTaskDurationTimes: ['total'],
                  },
                  filter: [
                    {
                      type: 'instanceStartDate',
                      data: {
                        type: 'rolling',
                        start: {
                          value: 7,
                          unit: 'days',
                        },
                        end: null,
                        includeUndefined: false,
                        excludeUndefined: false,
                      },
                      filterLevel: 'instance',
                      appliedTo: ['all'],
                    },
                  ],
                  view: {
                    entity: 'processInstance',
                    properties: ['frequency'],
                  },
                  groupBy: {
                    type: 'startDate',
                    value: {
                      unit: 'day',
                    },
                  },
                  visualization: 'line',
                },
              },
            },
            {
              position: {
                x: 3,
                y: 10,
              },
              dimensions: {
                width: 10,
                height: 4,
              },
              type: 'optimize_report',
              report: {
                name: t('instantDashboard.report.IP_TileText_BusinessReporting_Report1'),
                data: {
                  configuration: {
                    color: '#1991c8',
                    aggregationTypes: [
                      {
                        type: 'avg',
                        value: null,
                      },
                    ],
                    userTaskDurationTimes: ['total'],
                    sorting: {
                      by: 'key',
                      order: 'asc',
                    },
                  },
                  filter: [
                    {
                      type: 'instanceEndDate',
                      data: {
                        type: 'rolling',
                        start: {
                          value: 1,
                          unit: 'years',
                        },
                        end: null,
                        includeUndefined: false,
                        excludeUndefined: false,
                      },
                      filterLevel: 'instance',
                      appliedTo: ['all'],
                    },
                  ],
                  view: {
                    entity: 'processInstance',
                    properties: ['frequency'],
                  },
                  groupBy: {
                    type: 'endDate',
                    value: {
                      unit: 'month',
                    },
                  },
                  visualization: 'bar',
                },
              },
            },
            {
              position: {
                x: 3,
                y: 15,
              },
              dimensions: {
                width: 10,
                height: 4,
              },
              type: 'optimize_report',
              report: {
                name: t('instantDashboard.report.IP_TileText_ProcessImprovement_Report1'),
                data: {
                  configuration: {
                    aggregationTypes: [
                      {
                        type: 'percentile',
                        value: 75.0,
                      },
                    ],
                    userTaskDurationTimes: ['total'],
                  },
                  view: {
                    entity: 'flowNode',
                    properties: ['duration'],
                  },
                  groupBy: {
                    type: 'flowNodes',
                    value: null,
                  },
                  visualization: 'heat',
                },
              },
            },
            {
              position: {
                x: 0,
                y: 0,
              },
              dimensions: {
                width: 18,
                height: 1,
              },
              type: 'text',
              configuration: {
                text: {
                  root: {
                    children: [
                      {
                        children: [
                          {
                            altText: 'https://i.postimg.cc/jSwfwDKW/OPT_Logo.png',
                            src: 'https://i.postimg.cc/jSwfwDKW/OPT_Logo.png',
                            width: 0,
                            caption: {
                              editorState: {
                                root: {
                                  children: [],
                                  indent: 0,
                                  format: '',
                                  type: 'root',
                                  version: 1,
                                  direction: null,
                                },
                              },
                            },
                            showCaption: false,
                            type: 'image',
                            version: 1,
                            height: 0,
                            maxWidth: 500,
                          },
                        ],
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: null,
                      },
                    ],
                    indent: 0,
                    format: '',
                    type: 'root',
                    version: 1,
                    direction: null,
                  },
                },
              },
            },
            {
              position: {
                x: 0,
                y: 1,
              },
              dimensions: {
                width: 18,
                height: 1,
              },
              type: 'text',
              configuration: {
                text: {
                  root: {
                    children: [
                      {
                        children: [
                          {
                            altText: 'https://i.postimg.cc/vZNphSpq/OPT_Business_Operations.png',
                            src: 'https://i.postimg.cc/vZNphSpq/OPT_Business_Operations.png',
                            width: 0,
                            caption: {
                              editorState: {
                                root: {
                                  children: [],
                                  indent: 0,
                                  format: '',
                                  type: 'root',
                                  version: 1,
                                  direction: null,
                                },
                              },
                            },
                            showCaption: false,
                            type: 'image',
                            version: 1,
                            height: 0,
                            maxWidth: 500,
                          },
                        ],
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: null,
                      },
                    ],
                    indent: 0,
                    format: '',
                    type: 'root',
                    version: 1,
                    direction: null,
                  },
                },
              },
            },
            {
              position: {
                x: 0,
                y: 2,
              },
              dimensions: {
                width: 3,
                height: 7,
              },
              type: 'text',
              configuration: {
                text: {
                  root: {
                    children: [
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 0,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_BusinessOperations'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [
                          {
                            altText:
                              'https://i.postimg.cc/QC6JkvH0/OPT_Logo_Business_Operations.png',
                            src: 'https://i.postimg.cc/QC6JkvH0/OPT_Logo_Business_Operations.png',
                            width: 0,
                            caption: {
                              editorState: {
                                root: {
                                  children: [],
                                  indent: 0,
                                  format: '',
                                  type: 'root',
                                  version: 1,
                                  direction: null,
                                },
                              },
                            },
                            showCaption: false,
                            type: 'image',
                            version: 1,
                            height: 0,
                            maxWidth: 500,
                          },
                        ],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: null,
                      },
                    ],
                    indent: 0,
                    format: '',
                    type: 'root',
                    version: 1,
                    direction: 'ltr',
                  },
                },
              },
            },
            {
              position: {
                x: 13,
                y: 6,
              },
              dimensions: {
                width: 5,
                height: 3,
              },
              type: 'text',
              configuration: {
                text: {
                  root: {
                    children: [
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 0,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_BusinessOperations_Details_1'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 1,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_BusinessOperations_Details_2'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 0,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_BusinessOperations_Details_3'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                    ],
                    indent: 0,
                    format: '',
                    type: 'root',
                    version: 1,
                    direction: 'ltr',
                  },
                },
              },
            },
            {
              position: {
                x: 0,
                y: 10,
              },
              dimensions: {
                width: 3,
                height: 4,
              },
              type: 'text',
              configuration: {
                text: {
                  root: {
                    children: [
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 0,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_BusinessReporting'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [
                          {
                            altText:
                              'https://i.postimg.cc/g0Cpk7YM/OPT_Logo_Business_Reporting.png',
                            src: 'https://i.postimg.cc/g0Cpk7YM/OPT_Logo_Business_Reporting.png',
                            width: 0,
                            caption: {
                              editorState: {
                                root: {
                                  children: [],
                                  indent: 0,
                                  format: '',
                                  type: 'root',
                                  version: 1,
                                  direction: null,
                                },
                              },
                            },
                            showCaption: false,
                            type: 'image',
                            version: 1,
                            height: 0,
                            maxWidth: 500,
                          },
                        ],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: null,
                      },
                    ],
                    indent: 0,
                    format: '',
                    type: 'root',
                    version: 1,
                    direction: 'ltr',
                  },
                },
              },
            },
            {
              position: {
                x: 0,
                y: 9,
              },
              dimensions: {
                width: 18,
                height: 1,
              },
              type: 'text',
              configuration: {
                text: {
                  root: {
                    children: [
                      {
                        children: [
                          {
                            altText: 'https://i.postimg.cc/prph7MzS/OPT_Business_Reporting.png',
                            src: 'https://i.postimg.cc/prph7MzS/OPT_Business_Reporting.png',
                            width: 0,
                            caption: {
                              editorState: {
                                root: {
                                  children: [],
                                  indent: 0,
                                  format: '',
                                  type: 'root',
                                  version: 1,
                                  direction: null,
                                },
                              },
                            },
                            showCaption: false,
                            type: 'image',
                            version: 1,
                            height: 0,
                            maxWidth: 500,
                          },
                        ],
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: null,
                      },
                    ],
                    indent: 0,
                    format: '',
                    type: 'root',
                    version: 1,
                    direction: null,
                  },
                },
              },
            },
            {
              position: {
                x: 13,
                y: 10,
              },
              dimensions: {
                width: 5,
                height: 4,
              },
              type: 'text',
              configuration: {
                text: {
                  root: {
                    children: [
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 0,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_BusinessReporting_Details_1'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 1,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_BusinessReporting_Details_2'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 0,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_BusinessReporting_Details_3'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 0,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_BusinessReporting_Details_4'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 0,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_BusinessReporting_Details_5'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                    ],
                    indent: 0,
                    format: '',
                    type: 'root',
                    version: 1,
                    direction: 'ltr',
                  },
                },
              },
            },
            {
              position: {
                x: 0,
                y: 14,
              },
              dimensions: {
                width: 18,
                height: 1,
              },
              type: 'text',
              configuration: {
                text: {
                  root: {
                    children: [
                      {
                        children: [
                          {
                            altText: 'https://i.postimg.cc/pT05qLh8/OPT_Process_Improvement.png',
                            src: 'https://i.postimg.cc/pT05qLh8/OPT_Process_Improvement.png',
                            width: 0,
                            caption: {
                              editorState: {
                                root: {
                                  children: [],
                                  indent: 0,
                                  format: '',
                                  type: 'root',
                                  version: 1,
                                  direction: null,
                                },
                              },
                            },
                            showCaption: false,
                            type: 'image',
                            version: 1,
                            height: 0,
                            maxWidth: 500,
                          },
                        ],
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: null,
                      },
                    ],
                    indent: 0,
                    format: '',
                    type: 'root',
                    version: 1,
                    direction: null,
                  },
                },
              },
            },
            {
              position: {
                x: 0,
                y: 15,
              },
              dimensions: {
                width: 3,
                height: 4,
              },
              type: 'text',
              configuration: {
                text: {
                  root: {
                    children: [
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 0,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_ProcessImprovement'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [
                          {
                            altText:
                              'https://i.postimg.cc/j2qhfRdD/OPT_Logo_Process_Improvement.png',
                            src: 'https://i.postimg.cc/j2qhfRdD/OPT_Logo_Process_Improvement.png',
                            width: 0,
                            caption: {
                              editorState: {
                                root: {
                                  children: [],
                                  indent: 0,
                                  format: '',
                                  type: 'root',
                                  version: 1,
                                  direction: null,
                                },
                              },
                            },
                            showCaption: false,
                            type: 'image',
                            version: 1,
                            height: 0,
                            maxWidth: 500,
                          },
                        ],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: null,
                      },
                    ],
                    indent: 0,
                    format: '',
                    type: 'root',
                    version: 1,
                    direction: 'ltr',
                  },
                },
              },
            },
            {
              position: {
                x: 13,
                y: 15,
              },
              dimensions: {
                width: 5,
                height: 4,
              },
              type: 'text',
              configuration: {
                text: {
                  root: {
                    children: [
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 0,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_ProcessImprovement_Details_1'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 1,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_ProcessImprovement_Details_2'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 0,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_ProcessImprovement_Details_3'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 0,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_ProcessImprovement_Details_4'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                      {
                        children: [
                          {
                            mode: 'normal',
                            format: 0,
                            style: '',
                            detail: 0,
                            text: t('instantDashboard.IP_TileText_ProcessImprovement_Details_5'),
                            type: 'text',
                            version: 1,
                          },
                        ],
                        indent: 0,
                        format: '',
                        type: 'paragraph',
                        version: 1,
                        direction: 'ltr',
                      },
                    ],
                    indent: 0,
                    format: '',
                    type: 'root',
                    version: 1,
                    direction: 'ltr',
                  },
                },
              },
            },
          ],
        },
        {
          name: 'processPerformance',
          disableDescription: true,
          hasSubtitle: true,
          img: processPerformance,
          disabled: (definitions) => definitions.length > 1,
          config: [
            {
              position: {x: 0, y: 0},
              dimensions: {height: 2, width: 3},
              report: {
                name: t('dashboard.templates.30DayThroughput'),
                disableDescription: true,
                data: {
                  view: {entity: 'processInstance', properties: ['frequency']},
                  groupBy: {type: 'none', value: null},
                  visualization: 'number',
                  filter: [
                    {
                      type: 'instanceEndDate',
                      data: {
                        type: 'rolling',
                        start: {
                          value: 30,
                          unit: 'days',
                        },
                      },
                      filterLevel: 'instance',
                      appliedTo: ['all'],
                    },
                  ],
                  configuration: {
                    targetValue: {
                      active: true,
                      isKpi: true,
                      countProgress: {
                        target: '200',
                      },
                    },
                  },
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 3, y: 0},
              dimensions: {height: 2, width: 4},
              report: {
                name: t('dashboard.templates.p75Duration'),
                disableDescription: true,
                data: {
                  view: {entity: 'processInstance', properties: ['duration']},
                  groupBy: {type: 'none', value: null},
                  visualization: 'number',
                  configuration: {
                    aggregationTypes: [
                      {
                        type: 'percentile',
                        value: 75,
                      },
                    ],
                    precision: 1,
                    targetValue: {
                      active: true,
                      isKpi: true,
                      durationProgress: {
                        target: {
                          unit: 'hours',
                          value: '24',
                          isBelow: true,
                        },
                      },
                    },
                  },
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 7, y: 0},
              dimensions: {height: 2, width: 4},
              report: {
                name: t('dashboard.templates.p99Duration'),
                disableDescription: true,
                data: {
                  view: {entity: 'processInstance', properties: ['duration']},
                  groupBy: {type: 'none', value: null},
                  visualization: 'number',
                  configuration: {
                    aggregationTypes: [
                      {
                        type: 'percentile',
                        value: 99,
                      },
                    ],
                    precision: 1,
                    targetValue: {
                      active: true,
                      isKpi: true,
                      durationProgress: {
                        target: {
                          unit: 'days',
                          value: '7',
                          isBelow: true,
                        },
                      },
                    },
                  },
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 11, y: 0},
              dimensions: {height: 2, width: 3},
              report: {
                name: t('dashboard.templates.percentSLAMet'),
                disableDescription: true,
                data: {
                  view: {entity: 'processInstance', properties: ['percentage']},
                  groupBy: {type: 'none', value: null},
                  visualization: 'number',
                  configuration: {
                    targetValue: {
                      active: true,
                      isKpi: true,
                      countProgress: {
                        baseline: '0',
                        target: '99',
                      },
                    },
                  },
                  filter: [
                    {
                      type: 'processInstanceDuration',
                      data: {
                        value: 7,
                        unit: 'days',
                        operator: '<',
                        includeNull: false,
                      },
                      filterLevel: 'instance',
                      appliedTo: ['all'],
                    },
                  ],
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 14, y: 0},
              dimensions: {height: 2, width: 4},
              report: {
                name: t('dashboard.templates.percentNoIncidents'),
                disableDescription: true,
                data: {
                  view: {entity: 'processInstance', properties: ['percentage']},
                  groupBy: {type: 'none', value: null},
                  visualization: 'number',
                  configuration: {
                    targetValue: {
                      active: true,
                      isKpi: true,
                      countProgress: {
                        baseline: '0',
                        target: '99',
                      },
                    },
                  },
                  filter: [
                    {
                      type: 'doesNotIncludeIncident',
                      data: null,
                      filterLevel: 'instance',
                      appliedTo: ['all'],
                    },
                  ],
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 0, y: 2},
              dimensions: {height: 5, width: 9},
              report: {
                name: t('dashboard.templates.flownodeDuration'),
                disableDescription: true,
                data: {
                  view: {entity: 'flowNode', properties: ['duration']},
                  groupBy: {type: 'flowNodes', value: null},
                  visualization: 'heat',
                  configuration: {
                    aggregationTypes: [
                      {type: 'avg', value: null},
                      {type: 'percentile', value: 50},
                      {type: 'max', value: null},
                    ],
                  },
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 9, y: 2},
              dimensions: {height: 5, width: 9},
              report: {
                name: t('dashboard.templates.controlChart'),
                disableDescription: true,
                data: {
                  view: {entity: 'processInstance', properties: ['duration']},
                  groupBy: {type: 'startDate', value: {unit: 'week'}},
                  visualization: 'line',
                  configuration: {
                    aggregationTypes: [
                      {type: 'percentile', value: 99},
                      {type: 'percentile', value: 90},
                      {type: 'percentile', value: 75},
                      {type: 'percentile', value: 50},
                    ],
                  },
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 0, y: 7},
              dimensions: {height: 5, width: 9},
              report: {
                name: t('dashboard.templates.flownodeFrequency'),
                disableDescription: true,
                data: {
                  view: {entity: 'flowNode', properties: ['frequency']},
                  groupBy: {type: 'flowNodes', value: null},
                  visualization: 'heat',
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 9, y: 7},
              dimensions: {height: 5, width: 9},
              report: {
                name: t('dashboard.templates.instanceTrends'),
                disableDescription: true,
                data: {
                  view: {entity: 'processInstance', properties: ['frequency']},
                  groupBy: {type: 'startDate', value: {unit: 'week'}},
                  visualization: 'bar',
                  configuration: {
                    xLabel: t('report.groupBy.startDate'),
                    yLabel: t('report.view.pi') + ' ' + t('report.view.count'),
                  },
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 0, y: 12},
              dimensions: {height: 5, width: 9},
              report: {
                name: t('dashboard.templates.activeIncidentsHeatmap'),
                disableDescription: true,
                data: {
                  view: {entity: 'incident', properties: ['frequency']},
                  groupBy: {type: 'flowNodes', value: null},
                  visualization: 'heat',
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 9, y: 12},
              dimensions: {height: 5, width: 9},
              report: {
                name: t('dashboard.templates.incidentDurationHeatmap'),
                disableDescription: true,
                data: {
                  view: {entity: 'incident', properties: ['duration']},
                  groupBy: {type: 'flowNodes', value: null},
                  visualization: 'heat',
                  configuration: {
                    aggregationTypes: [
                      {type: 'avg', value: null},
                      {type: 'percentile', value: 50},
                      {type: 'max', value: null},
                    ],
                  },
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 0, y: 17},
              dimensions: {height: 5, width: 18},
              report: {
                name: t('dashboard.templates.incidentDurationTrend'),
                disableDescription: true,
                data: {
                  view: {entity: 'processInstance', properties: ['frequency', 'duration']},
                  groupBy: {type: 'startDate', value: {unit: 'week'}},
                  visualization: 'barLine',
                  filter: [
                    {
                      appliedTo: ['all'],
                      filterLevel: 'instance',
                      type: 'includesResolvedIncident',
                    },
                  ],
                },
              },
              type: 'optimize_report',
            },
          ],
        },
      ],
    },
    {
      name: 'multiProcessGroup',
      templates: [
        {
          name: 'operationsMonitoring',
          disableDescription: true,
          hasSubtitle: true,
          img: operationsMonitoring,
          disabled: (definitions) => definitions.length < 2,
          config: [
            {
              position: {x: 0, y: 0},
              dimensions: {height: 3, width: 4},
              report: {
                name: t('dashboard.templates.completedProcesses'),
                disableDescription: true,
                data: {
                  view: {entity: 'processInstance', properties: ['frequency']},
                  groupBy: {type: 'none', value: null},
                  visualization: 'number',
                  filter: [
                    {
                      appliedTo: ['all'],
                      data: null,
                      filterLevel: 'instance',
                      type: 'completedInstancesOnly',
                    },
                  ],
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 4, y: 0},
              dimensions: {height: 3, width: 5},
              report: {
                name: t('dashboard.templates.longRunningProcesses'),
                disableDescription: true,
                data: {
                  view: {entity: 'processInstance', properties: ['frequency']},
                  groupBy: {type: 'none', value: null},
                  distributedBy: {type: 'none', value: null},
                  visualization: 'number',
                  filter: [
                    {
                      appliedTo: ['all'],
                      data: null,
                      filterLevel: 'instance',
                      type: 'runningInstancesOnly',
                    },
                    {
                      appliedTo: ['all'],
                      data: {value: 1, unit: 'days', operator: '>', includeNull: false},
                      filterLevel: 'instance',
                      type: 'processInstanceDuration',
                    },
                  ],
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 9, y: 0},
              dimensions: {height: 3, width: 4},
              report: {
                name: t('dashboard.templates.activeIncidents'),
                disableDescription: true,
                data: {
                  view: {entity: 'incident', properties: ['frequency']},
                  groupBy: {type: 'none', value: null},
                  visualization: 'number',
                  filter: [
                    {
                      appliedTo: ['all'],
                      data: null,
                      filterLevel: 'instance',
                      type: 'includesOpenIncident',
                    },
                  ],
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 13, y: 0},
              dimensions: {height: 3, width: 5},
              report: {
                name: t('dashboard.templates.activeIncidentsByProcess'),
                disableDescription: true,
                data: {
                  view: {entity: 'processInstance', properties: ['frequency']},
                  groupBy: {type: 'none', value: null},
                  distributedBy: {type: 'process', value: null},
                  visualization: 'pie',
                  filter: [
                    {
                      appliedTo: ['all'],
                      data: null,
                      filterLevel: 'instance',
                      type: 'includesOpenIncident',
                    },
                  ],
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 0, y: 3},
              dimensions: {height: 5, width: 9},
              report: {
                name: t('dashboard.templates.processSnapshot'),
                disableDescription: true,
                data: {
                  view: {entity: 'flowNode', properties: ['frequency', 'duration']},
                  groupBy: {type: 'flowNodes', value: null},
                  distributedBy: {type: 'none', value: null},
                  visualization: 'barLine',
                  filter: [
                    {
                      appliedTo: ['all'],
                      data: null,
                      filterLevel: 'view',
                      type: 'runningFlowNodesOnly',
                    },
                  ],
                  configuration: {
                    measureVisualizations: {frequency: 'bar', duration: 'line'},
                    showInstanceCount: true,
                    stackedBar: true,
                  },
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 9, y: 3},
              dimensions: {height: 5, width: 9},
              report: {
                name: t('dashboard.templates.incidentSnapshot'),
                disableDescription: true,
                data: {
                  view: {entity: 'incident', properties: ['frequency', 'duration']},
                  groupBy: {type: 'flowNodes', value: null},
                  distributedBy: {type: 'none', value: null},
                  visualization: 'barLine',
                  filter: [
                    {
                      appliedTo: ['all'],
                      data: null,
                      filterLevel: 'instance',
                      type: 'includesOpenIncident',
                    },
                    {
                      appliedTo: ['all'],
                      data: null,
                      filterLevel: 'view',
                      type: 'includesOpenIncident',
                    },
                  ],
                  configuration: {
                    measureVisualizations: {frequency: 'bar', duration: 'line'},
                    showInstanceCount: true,
                  },
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 0, y: 8},
              dimensions: {height: 5, width: 9},
              report: {
                name: t('dashboard.templates.processHistory'),
                disableDescription: true,
                data: {
                  view: {entity: 'processInstance', properties: ['frequency', 'duration']},
                  groupBy: {type: 'startDate', value: {unit: 'week'}},
                  distributedBy: {type: 'process', value: null},
                  visualization: 'barLine',
                  filter: [
                    {
                      appliedTo: ['all'],
                      data: null,
                      filterLevel: 'instance',
                      type: 'completedInstancesOnly',
                    },
                  ],
                  configuration: {
                    measureVisualizations: {frequency: 'bar', duration: 'line'},
                    stackedBar: true,
                  },
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 9, y: 8},
              dimensions: {height: 5, width: 9},
              report: {
                name: t('dashboard.templates.incidentHistory'),
                disableDescription: true,
                data: {
                  view: {entity: 'processInstance', properties: ['frequency', 'duration']},
                  groupBy: {type: 'startDate', value: {unit: 'week'}},
                  distributedBy: {type: 'process', value: null},
                  visualization: 'barLine',
                  filter: [
                    {
                      appliedTo: ['all'],
                      data: null,
                      filterLevel: 'instance',
                      type: 'includesResolvedIncident',
                    },
                  ],
                  configuration: {
                    measureVisualizations: {frequency: 'bar', duration: 'line'},
                    stackedBar: true,
                    aggregationTypes: [
                      {type: 'avg', value: null},
                      {type: 'max', value: null},
                    ],
                  },
                },
              },
              type: 'optimize_report',
            },
            {
              position: {x: 0, y: 13},
              dimensions: {height: 5, width: 18},
              report: {
                name: t('dashboard.templates.durationSLI'),
                disableDescription: true,
                data: {
                  view: {entity: 'processInstance', properties: ['duration']},
                  groupBy: {type: 'startDate', value: {unit: 'week'}},
                  distributedBy: {type: 'process', value: null},
                  visualization: 'line',
                  configuration: {
                    aggregationTypes: [{type: 'max', value: null}],
                    targetValue: {
                      active: true,
                      isKpi: true,
                      durationChart: {unit: 'hours', isBelow: true, value: '4'},
                    },
                  },
                },
              },
              type: 'optimize_report',
            },
          ],
        },
      ],
    },
  ];

  if (optimizeProfile === 'platform') {
    templateGroups[1].templates.push(
      {
        name: 'humanPerformance',
        disableDescription: true,
        hasSubtitle: true,
        img: humanPerformance,
        disabled: (definitions) => definitions.length > 1,
        config: [
          {
            position: {x: 0, y: 0},
            dimensions: {height: 5, width: 9},
            report: {
              name: t('dashboard.templates.idleTime'),
              disableDescription: true,
              data: {
                view: {entity: 'userTask', properties: ['duration']},
                groupBy: {type: 'userTasks', value: null},
                visualization: 'heat',
                configuration: {userTaskDurationTimes: ['idle']},
              },
            },
            type: 'optimize_report',
          },
          {
            position: {x: 9, y: 0},
            dimensions: {height: 5, width: 9},
            report: {
              name: t('dashboard.templates.tasksStarted'),
              disableDescription: true,
              data: {
                view: {entity: 'userTask', properties: ['frequency']},
                groupBy: {type: 'startDate', value: {unit: 'month'}},
                visualization: 'bar',
                distributedBy: {type: 'assignee', value: null},
                configuration: {
                  xLabel: t('report.groupBy.startDate'),
                  yLabel: t('report.view.userTask') + ' ' + t('report.view.count'),
                },
              },
            },
            type: 'optimize_report',
          },
          {
            position: {x: 0, y: 5},
            dimensions: {height: 5, width: 9},
            report: {
              name: t('dashboard.templates.workTime'),
              disableDescription: true,
              data: {
                view: {entity: 'userTask', properties: ['duration']},
                groupBy: {type: 'userTasks', value: null},
                visualization: 'heat',
                configuration: {userTaskDurationTimes: ['work']},
              },
            },
            type: 'optimize_report',
          },
          {
            position: {x: 9, y: 5},
            dimensions: {height: 5, width: 9},
            report: {
              name: t('dashboard.templates.tasksCompleted'),
              disableDescription: true,
              data: {
                view: {entity: 'userTask', properties: ['frequency']},
                groupBy: {type: 'endDate', value: {unit: 'month'}},
                visualization: 'bar',
                distributedBy: {type: 'assignee', value: null},
                configuration: {
                  xLabel: t('report.groupBy.endDate'),
                  yLabel: t('report.view.userTask') + ' ' + t('report.view.count'),
                },
              },
            },
            type: 'optimize_report',
          },
        ],
      },
      {
        name: 'humanBottleneckAnalysis',
        disableDescription: true,
        hasSubtitle: true,
        img: humanBottleneckAnalysis,
        disabled: (definitions) => definitions.length > 1,
        config: [
          {
            position: {x: 0, y: 0},
            dimensions: {height: 4, width: 9},
            report: {
              name: t('dashboard.templates.bottleneckLocation'),
              disableDescription: true,
              data: {
                view: {entity: 'userTask', properties: ['frequency']},
                groupBy: {type: 'userTasks', value: null},
                visualization: 'heat',
                filter: [
                  {
                    appliedTo: ['definition'],
                    data: {operator: 'in', values: [null]},
                    filterLevel: 'view',
                    type: 'assignee',
                  },
                ],
              },
            },
            type: 'optimize_report',
          },
          {
            position: {x: 9, y: 0},
            dimensions: {height: 4, width: 9},
            report: {
              name: t('dashboard.templates.bottleneckSeverity'),
              disableDescription: true,
              data: {
                view: {entity: 'userTask', properties: ['duration']},
                groupBy: {type: 'userTasks', value: null},
                visualization: 'heat',
                configuration: {
                  aggregationTypes: [{type: 'avg', value: null}],
                  userTaskDurationTimes: ['total', 'work', 'idle'],
                },
              },
            },
            type: 'optimize_report',
          },
          {
            position: {x: 0, y: 4},
            dimensions: {height: 4, width: 6},
            report: {
              name: t('dashboard.templates.assigneeVariation'),
              disableDescription: true,
              data: {
                view: {entity: 'userTask', properties: ['duration']},
                groupBy: {type: 'assignee', value: null},
                distributedBy: {type: 'userTask', value: null},
                filter: [
                  {
                    appliedTo: ['definition'],
                    data: {operator: 'not in', values: [null]},
                    filterLevel: 'view',
                    type: 'assignee',
                  },
                ],
                visualization: 'bar',
                configuration: {
                  aggregationTypes: [{type: 'avg', value: null}],
                  userTaskDurationTimes: ['total'],
                  stackedBar: true,
                },
              },
            },
            type: 'optimize_report',
          },
          {
            position: {x: 6, y: 4},
            dimensions: {height: 4, width: 6},
            report: {
              name: t('dashboard.templates.userTaskImprovement'),
              disableDescription: true,
              data: {
                view: {entity: 'userTask', properties: ['duration']},
                groupBy: {type: 'endDate', value: {unit: 'week'}},
                distributedBy: {type: 'userTask', value: null},
                visualization: 'bar',
                configuration: {
                  aggregationTypes: [{type: 'avg', value: null}],
                  userTaskDurationTimes: ['work', 'idle'],
                  stackedBar: true,
                },
              },
            },
            type: 'optimize_report',
          },
          {
            position: {x: 12, y: 4},
            dimensions: {height: 4, width: 3},
            report: {
              name: t('dashboard.templates.upstreamWork'),
              disableDescription: true,
              data: {
                view: {entity: 'processInstance', properties: ['frequency']},
                groupBy: {type: 'none', value: null},
                visualization: 'number',
                filter: [
                  {
                    appliedTo: ['definition'],
                    data: {values: ['StartEvent_1']},
                    filterLevel: 'instance',
                    type: 'executingFlowNodes',
                  },
                  {
                    appliedTo: ['all'],
                    data: null,
                    filterLevel: 'instance',
                    type: 'runningInstancesOnly',
                  },
                ],
                configuration: {
                  aggregationTypes: [{type: 'avg', value: null}],
                  userTaskDurationTimes: ['work', 'idle'],
                  stackedBar: true,
                },
              },
            },
            type: 'optimize_report',
          },
          {
            position: {x: 15, y: 4},
            dimensions: {height: 4, width: 3},
            report: {
              name: t('dashboard.templates.bottleneckQueue'),
              disableDescription: true,
              data: {
                view: {entity: 'processInstance', properties: ['frequency']},
                groupBy: {type: 'none', value: null},
                visualization: 'number',
                filter: [
                  {
                    appliedTo: ['definition'],
                    data: {values: ['StartEvent_1']},
                    filterLevel: 'instance',
                    type: 'executingFlowNodes',
                  },
                  {
                    appliedTo: ['definition'],
                    data: {operator: 'in', values: [null]},
                    filterLevel: 'view',
                    type: 'assignee',
                  },
                ],
                configuration: {
                  aggregationTypes: [{type: 'avg', value: null}],
                  userTaskDurationTimes: ['work', 'idle'],
                  stackedBar: true,
                },
              },
            },
            type: 'optimize_report',
          },
          {
            position: {x: 0, y: 8},
            dimensions: {height: 4, width: 6},
            report: {
              name: t('dashboard.templates.durationImprovement'),
              disableDescription: true,
              data: {
                view: {entity: 'processInstance', properties: ['duration']},
                groupBy: {type: 'endDate', value: {unit: 'week'}},
                visualization: 'line',
                configuration: {
                  aggregationTypes: [
                    {type: 'avg', value: null},
                    {type: 'percentile', value: 50},
                    {type: 'max', value: null},
                  ],
                  userTaskDurationTimes: ['total'],
                },
              },
            },
            type: 'optimize_report',
          },
          {
            position: {x: 6, y: 8},
            dimensions: {height: 4, width: 6},
            report: {
              name: t('dashboard.templates.workerProductivity'),
              disableDescription: true,
              data: {
                view: {entity: 'userTask', properties: ['frequency']},
                groupBy: {type: 'assignee', value: null},
                distributedBy: {type: 'userTask', value: null},
                visualization: 'bar',
                filter: [
                  {
                    appliedTo: ['all'],
                    data: null,
                    filterLevel: 'view',
                    type: 'completedFlowNodesOnly',
                  },
                ],
                configuration: {
                  stackedBar: true,
                },
              },
            },
            type: 'optimize_report',
          },
          {
            position: {x: 12, y: 8},
            dimensions: {height: 4, width: 6},
            report: {
              name: t('dashboard.templates.workDuration'),
              disableDescription: true,
              data: {
                view: {entity: 'userTask', properties: ['duration']},
                groupBy: {type: 'endDate', value: {unit: 'week'}},
                distributedBy: {type: 'userTask', value: null},
                visualization: 'line',
                filter: [
                  {
                    appliedTo: ['all'],
                    data: null,
                    filterLevel: 'view',
                    type: 'completedFlowNodesOnly',
                  },
                ],
                configuration: {
                  aggregationTypes: [{type: 'avg', value: null}],
                  userTaskDurationTimes: ['work'],
                },
              },
            },
            type: 'optimize_report',
          },
        ],
      }
    );
    templateGroups[2].templates.unshift({
      name: 'portfolioPerformance',
      disableDescription: true,
      hasSubtitle: true,
      img: portfolioPerformance,
      disabled: (definitions) => definitions.length < 2,
      config: [
        {
          position: {x: 0, y: 0},
          dimensions: {height: 3, width: 3},
          report: {
            name: t('dashboard.templates.completedProcesses'),
            disableDescription: true,
            data: {
              view: {entity: 'processInstance', properties: ['frequency']},
              groupBy: {type: 'none', value: null},
              visualization: 'number',
              filter: [
                {
                  appliedTo: ['all'],
                  data: null,
                  filterLevel: 'instance',
                  type: 'completedInstancesOnly',
                },
              ],
            },
          },
          type: 'optimize_report',
        },
        {
          position: {x: 3, y: 0},
          dimensions: {height: 3, width: 3},
          report: {
            name: t('dashboard.templates.activeIncidents'),
            disableDescription: true,
            data: {
              view: {entity: 'incident', properties: ['frequency']},
              groupBy: {type: 'none', value: null},
              visualization: 'number',
              filter: [
                {
                  appliedTo: ['all'],
                  data: null,
                  filterLevel: 'instance',
                  type: 'includesOpenIncident',
                },
              ],
            },
          },
          type: 'optimize_report',
        },
        {
          position: {x: 6, y: 0},
          dimensions: {height: 3, width: 6},
          report: {
            name: t('dashboard.templates.runningProcesses'),
            disableDescription: true,
            data: {
              view: {entity: 'processInstance', properties: ['frequency']},
              groupBy: {type: 'none', value: null},
              distributedBy: {type: 'process', value: null},
              visualization: 'bar',
              filter: [
                {
                  appliedTo: ['all'],
                  data: null,
                  filterLevel: 'instance',
                  type: 'runningInstancesOnly',
                },
              ],
            },
          },
          type: 'optimize_report',
        },
        {
          position: {x: 12, y: 0},
          dimensions: {height: 3, width: 6},
          report: {
            name: t('dashboard.templates.runningTasks'),
            disableDescription: true,
            data: {
              view: {entity: 'userTask', properties: ['frequency']},
              groupBy: {type: 'userTasks', value: null},
              visualization: 'pie',
              filter: [
                {
                  appliedTo: ['all'],
                  data: null,
                  filterLevel: 'view',
                  type: 'runningFlowNodesOnly',
                },
              ],
            },
          },
          type: 'optimize_report',
        },
        {
          position: {x: 0, y: 3},
          dimensions: {height: 5, width: 9},
          report: {
            name: t('dashboard.templates.processTotal'),
            disableDescription: true,
            data: {
              view: {entity: 'processInstance', properties: ['frequency']},
              groupBy: {type: 'startDate', value: {unit: 'month'}},
              distributedBy: {type: 'process', value: null},
              visualization: 'bar',
              configuration: {
                stackedBar: true,
              },
            },
          },
          type: 'optimize_report',
        },
        {
          position: {x: 9, y: 3},
          dimensions: {height: 5, width: 9},
          report: {
            name: t('dashboard.templates.laborSavings'),
            disableDescription: true,
            data: {
              view: {entity: 'userTask', properties: ['duration']},
              groupBy: {type: 'startDate', value: {unit: 'month'}},
              distributedBy: {type: 'process', value: null},
              visualization: 'bar',
              filter: [
                {
                  appliedTo: ['all'],
                  data: null,
                  filterLevel: 'instance',
                  type: 'completedInstancesOnly',
                },
              ],
              configuration: {
                aggregationTypes: [{type: 'sum', value: null}],
                userTaskDurationTimes: ['work'],
                stackedBar: true,
              },
            },
          },
          type: 'optimize_report',
        },
        {
          position: {x: 0, y: 8},
          dimensions: {height: 5, width: 9},
          report: {
            name: t('dashboard.templates.processAcceleration'),
            disableDescription: true,
            data: {
              view: {entity: 'processInstance', properties: ['duration']},
              groupBy: {type: 'startDate', value: {unit: 'month'}},
              distributedBy: {type: 'process', value: null},
              visualization: 'line',
            },
          },
          type: 'optimize_report',
        },
        {
          position: {x: 9, y: 8},
          dimensions: {height: 5, width: 9},
          report: {
            name: t('dashboard.templates.taskAutomation'),
            disableDescription: true,
            data: {
              view: {entity: 'userTask', properties: ['frequency']},
              groupBy: {type: 'startDate', value: {unit: 'month'}},
              distributedBy: {type: 'process', value: null},
              visualization: 'bar',
              configuration: {
                aggregationTypes: [{type: 'sum', value: null}],
                userTaskDurationTimes: ['work'],
                stackedBar: true,
              },
            },
          },
          type: 'optimize_report',
        },
        {
          position: {x: 0, y: 13},
          dimensions: {height: 5, width: 9},
          report: {
            name: t('dashboard.templates.incidentHandling'),
            disableDescription: true,
            data: {
              view: {entity: 'incident', properties: ['duration']},
              groupBy: {type: 'flowNodes', value: null},
              visualization: 'bar',
              filter: [
                {
                  appliedTo: ['all'],
                  data: null,
                  filterLevel: 'view',
                  type: 'includesResolvedIncident',
                },
              ],
              configuration: {
                aggregationTypes: [{type: 'avg', value: null}],
                targetValue: {
                  active: true,
                  isKpi: true,
                  durationChart: {unit: 'hours', isBelow: true, value: '1'},
                },
              },
            },
          },
          type: 'optimize_report',
        },
        {
          position: {x: 9, y: 13},
          dimensions: {height: 5, width: 9},
          report: {
            name: t('dashboard.templates.taskLifecycle'),
            disableDescription: true,
            data: {
              view: {entity: 'userTask', properties: ['duration']},
              groupBy: {type: 'userTasks', value: null},
              visualization: 'bar',
              configuration: {
                userTaskDurationTimes: ['work', 'idle'],
              },
            },
          },
          type: 'optimize_report',
        },
      ],
    });
  }

  if (!optimizeProfileLoaded) {
    return null;
  }

  return (
    <TemplateModal
      onClose={onClose}
      templateGroups={templateGroups}
      entity="dashboard"
      blankSlate={
        <ol>
          <li>{t('templates.blankSlate.selectProcess')}</li>
          <li>{t('templates.blankSlate.selectTemplate')}</li>
          <li>{t('templates.blankSlate.review')}</li>
          <li>{t('templates.blankSlate.refine')}</li>
        </ol>
      }
      templateToState={({template, ...props}) => ({
        ...props,
        data: template || [],
      })}
    />
  );
}
