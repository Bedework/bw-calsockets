/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calsockets.common.responses;

import org.bedework.calsockets.common.artifacts.ResourceCollection;
import org.bedework.base.ToString;

import java.util.List;

/**
 * User: mike Date: 2/8/18 Time: 17:51
 */
public class InitResponse extends Response {
  private List<ResourceCollection> collections;

  public InitResponse() {
    super(initResponse);
  }

  /**
   * @param val a list of collections
   */
  public void setCollections(final List<ResourceCollection> val) {
    collections = val;
  }

  /**
   * @return list of collections
   */
  public List<ResourceCollection> getCollections() {
    return collections;
  }

  /** Add information to the ToString builder
   *
   * @param ts ToString builder
   */
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.append("collections", getCollections());
  }
}
