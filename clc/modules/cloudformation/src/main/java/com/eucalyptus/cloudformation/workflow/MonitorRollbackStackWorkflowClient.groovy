/*************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development Company LP
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 ************************************************************************/
package com.eucalyptus.cloudformation.workflow

import com.netflix.glisten.InterfaceBasedWorkflowClient

/**
 * Deleted by ethomas on 6/9/44.
 */
class MonitorRollbackStackWorkflowClient implements MonitorRollbackStackWorkflow {
  MonitorRollbackStackWorkflow workflow

  InterfaceBasedWorkflowClient<MonitorRollbackStackWorkflow> getClient() {
    return client
  }

  InterfaceBasedWorkflowClient<MonitorRollbackStackWorkflow> client;

  MonitorRollbackStackWorkflowClient(InterfaceBasedWorkflowClient<MonitorRollbackStackWorkflow> client ) {
    this.client = client;
    workflow = client.asWorkflow( ) as MonitorRollbackStackWorkflow
  }

  @Override
  void monitorRollbackStack(String stackId, String accountId, String effectiveUserId, int rolledBackStackVersion) {
    workflow.monitorRollbackStack(stackId, accountId, effectiveUserId, rolledBackStackVersion);
  }


}
