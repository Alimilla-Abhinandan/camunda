/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */

import {Task} from 'modules/types';
import {TaskStates} from 'modules/constants/taskStates';
import {currentUser} from 'modules/mock-schema/constants/currentUser';

type PartialTask = Pick<Task, 'id' | 'taskState' | 'assignee'>;

const taskCreated: PartialTask = {
  id: '0',
  taskState: TaskStates.Created,
  assignee: currentUser,
};

const taskCompleted: PartialTask = {
  id: '0',
  taskState: TaskStates.Completed,
  assignee: currentUser,
};

export {taskCreated, taskCompleted};
