/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calsockets.common.responses;

import org.bedework.util.misc.ToString;

import java.util.List;

/**
 * User: mike Date: 2/8/18 Time: 17:51
 */
public class SyncCollectionResponse extends Response {
  private List<Object> items;

  public SyncCollectionResponse() {
    super(syncCollectionResponse);
  }

  /**
   * @param val a list of items
   */
  public void setItems(final List<Object> val) {
    items = val;
  }

  /**
   * @return list of items
   */
  public List<Object> getItems() {
    return items;
  }

  /** Add information to the ToString builder
   *
   * @param ts ToString builder
   */
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);
    ts.append("items", getItems());
  }
}
