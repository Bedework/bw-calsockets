/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calsockets.common.requests;

import org.bedework.calsockets.common.MessageBase;

/**
 * User: mike Date: 2/8/18 Time: 17:40
 */
public class InitRequest extends MessageBase {
  public InitRequest() {
    super(initAction);
  }
}
