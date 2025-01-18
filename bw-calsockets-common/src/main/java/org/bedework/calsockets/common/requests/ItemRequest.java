/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calsockets.common.requests;

import org.bedework.calsockets.common.MessageBase;
import org.bedework.base.ToString;

/**
 * User: mike Date: 2/8/18 Time: 17:40
 */
public class ItemRequest extends MessageBase {
  private String itemRef;

  public ItemRequest() {
    super(getItemAction);
  }

  /**
   * @param val a ref to the item
   */
  public void setItemRef(final String val) {
    itemRef = val;
  }

  /**
   * @return a ref to the item
   */
  public String getItemRef() {
    return itemRef;
  }

  public void copyTo(final ItemRequest target) {
    super.copyTo(target);

    target.setItemRef(getItemRef());
  }

  /** Add information to the ToString builder
   *
   * @param ts ToString builder
   */
  public void toStringSegment(final ToString ts) {
    ts.append("itemRef", getItemRef());
  }
}
