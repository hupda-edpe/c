/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.persistence.entity;


import org.camunda.bpm.engine.impl.cep.CepInterface;

/**
 * @author Lucas Mann
 */
public class CepEventSubscriptionEntity extends EventSubscriptionEntity {

  private static final long serialVersionUID = 1L;

  public CepEventSubscriptionEntity(ExecutionEntity executionEntity) {
    super(executionEntity);
    eventType = "cep";
  }

  public CepEventSubscriptionEntity() {
    eventType = "cep";
  }

  @Override
  public void eventReceived(Object payload, boolean processASync) {
    if (condition == null) {
      condition = CepInterface.getCondition(eventName, activityId);
    }
    super.eventReceived(payload, processASync);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + "[id=" + id
        + ", eventType=" + eventType
        + ", eventName=" + eventName
        + ", executionId=" + executionId
        + ", processInstanceId=" + processInstanceId
        + ", activityId=" + activityId
        + ", configuration=" + configuration
        + ", revision=" + revision
        + ", created=" + created
        + "]";
  }
}
