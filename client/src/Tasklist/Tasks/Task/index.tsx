/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */

import React from 'react';
import {
  Entry,
  TaskInfo,
  TaskName,
  ProcessName,
  TaskStatus,
  Assignee,
  CreationTime,
} from './styled';
import {useParams, Link} from 'react-router-dom';
import {Location} from 'history';
import {Pages} from 'modules/constants/pages';
import {formatDate} from 'modules/utils/formatDate';
import {Task as TaskType} from 'modules/types';
import {getAssigneeName} from 'modules/utils/getAssigneeName';

interface Props {
  taskId: TaskType['id'];
  name: TaskType['name'];
  processName: TaskType['processName'];
  assignee: TaskType['assignee'];
  creationTime: TaskType['creationTime'];
}

const Task = React.forwardRef<HTMLLIElement, Props>(
  ({taskId, name, processName, assignee, creationTime}, ref) => {
    const {id} = useParams<{id: string}>();

    return (
      <Link
        to={(location: Location) => ({
          ...location,
          pathname: Pages.TaskDetails(taskId),
        })}
      >
        <Entry
          ref={ref}
          isSelected={id === taskId}
          data-testid={`task-${taskId}`}
        >
          <TaskInfo>
            <TaskName>{name}</TaskName>
            <ProcessName>{processName}</ProcessName>
          </TaskInfo>
          <TaskStatus>
            <Assignee data-testid="assignee">
              {getAssigneeName(assignee)}
            </Assignee>

            <CreationTime data-testid="creation-time">
              {formatDate(creationTime)}
            </CreationTime>
          </TaskStatus>
        </Entry>
      </Link>
    );
  },
);

export {Task};
