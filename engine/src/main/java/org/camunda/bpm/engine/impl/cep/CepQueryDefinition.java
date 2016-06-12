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

package org.camunda.bpm.engine.impl.cep;

import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;

import java.io.Serializable;

/**
 * Represents a BPMN extension CEP query definition.
 *
 * @author Lucas Mann
 */
public class CepQueryDefinition implements Serializable {

  private static final long serialVersionUID = 1L;

  //private String id;
  private String name;
  private String code;

  public CepQueryDefinition(String name) {
    this.name = name;
    code = null;
  }

  public CepQueryDefinition(ProcessDefinitionEntity processDefinition, String id, String code) {
    name = processDefinition.getId() + "_" + id;
    this.code = code;
  }


  /*public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }*/

  public String getName() {
    return name;
  }

  public void register() {
    if (code != null) {
      // TODO: create query
    }
    CepInterface.registerQuery(name, "");
  }

  public void unregister() {
    if (code != null) {
      // TODO: delete query
    }
    CepInterface.unregisterQuery(name);
  }

}
