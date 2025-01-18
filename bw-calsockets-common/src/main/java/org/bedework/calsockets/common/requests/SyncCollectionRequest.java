/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calsockets.common.requests;

import org.bedework.calsockets.common.MessageBase;
import org.bedework.base.ToString;

/** Starts synching a collection with the client. On receipt of the
 * request the server will initialise the sync and return a response with
 * no items but with a status and possible message indicating success
 * or failure.
 *
 * <p>Further messages may arrive over time to update the
 * client end copy of the collection. Each of these message will be a
 * SyncCollectionResponse with appropriate status containing one or more
 * calendar items.
 *
 * <p>Note that these further messages may arrive before the response
 * to the request as they are generated asynchronously by the server.
 * The client should ensure it is ready to handle incoming sync messages
 * before it makes the request.
 *
 * User: mike Date: 2/8/18 Time: 17:40
 */
public class SyncCollectionRequest extends MessageBase {
  private String collectionRef;

  public SyncCollectionRequest() {
    super(syncCollectionAction);
  }

  /**
   * @param val a ref to the collection
   */
  public void setCollectionRef(final String val) {
    collectionRef = val;
  }

  /**
   * @return a ref to the collection
   */
  public String getCollectionRef() {
    return collectionRef;
  }

  public void copyTo(final SyncCollectionRequest target) {
    super.copyTo(target);

    target.setCollectionRef(getCollectionRef());
  }

  /** Add information to the ToString builder
   *
   * @param ts ToString builder
   */
  public void toStringSegment(final ToString ts) {
    ts.append("collectionRef", getCollectionRef());
  }
}
