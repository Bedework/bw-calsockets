/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.bedework.calsockets.common;

import org.bedework.calsockets.common.responses.Response;
import org.bedework.util.misc.ToString;

import java.io.Serializable;

/** Message base object forrequests and responses.
 * 
 * <p>The action is used by the json routines to enable deserialization
 * of the json into a Java object.</p>
 * 
 * <p>The id is not required but clients may use it to identify a
 * response. The id from the request will be copied into responses.</p>
 *
 * <p>The validate method may be overridden and will be called by
 * the api to validate fields.</p>
 * 
 * douglm: Bedework Commercial Services
 */
public class MessageBase implements Serializable {
  /** action should be one of the below + this for a response
   */
  public final static String responseSuffix = "-response";

  /** init
   */
  public final static String initAction = "init";

  /** get items
   */
  public final static String getItemsAction = "get-items";

  /** get item
   */
  public final static String getItemAction = "get-item";

  /** sync collection
   */
  public final static String syncCollectionAction = "sync-collection";

  /** init response
   */
  public final static String initResponse = initAction + responseSuffix;

  /** get items response
   */
  public final static String getItemsResponse =
          getItemsAction + responseSuffix;

  /** get item response
   */
  public final static String getItemResponse =
          getItemAction  + responseSuffix;

  /** sync collection response
   */
  public final static String syncCollectionResponse =
          syncCollectionAction + responseSuffix;

  private String action;

  /* Copied into the response */
  private int id;

  private String changeToken;

  public MessageBase(final String action) {
    this.action = action;
  }

  /**
   * @return the action
   */
  public String getAction() {
    return action;
  }

  /**
   * @param val an id to identify the request
   */
  public void setId(final int val) {
    id = val;
  }

  /**
   * @return an id to identify the request
   */
  public int getId() {
    return id;
  }

  /**
   * @param val a token for client collection status
   */
  public void setChangeToken(final String val) {
    changeToken = val;
  }

  /**
   * @return a token for client collection status
   */
  public String getChangeToken() {
    return changeToken;
  }

  /** May clean up the data in the request.
   *
   * @param resp for failed status and message
   * @return true for ok request
   */
  public boolean validate(final Response resp) {
    return true;
  }

  public <T extends MessageBase> void copyTo(final T target) {
    ((MessageBase)target).action = action;
    target.setId(getId());
    target.setChangeToken(getChangeToken());
  }

  /** Add information to the ToString builder
   * 
   * @param ts ToString builder
   */
  public void toStringSegment(final ToString ts) {
    ts.append("action", getAction());
    ts.append("id", getId());
    ts.append("changeToken", getChangeToken());
  }
  
  public String toString() {
    final ToString ts = new ToString(this);
    
    toStringSegment(ts);
    
    return ts.toString();
  }
}
