/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calsockets.common.artifacts;

import java.util.List;

/** Represent a collection
 *
 * User: mike Date: 2/8/18 Time: 17:53
 */
public class ResourceCollection {
  private String ref;
  private String name;
  private List<String> componentTypes;

  /**
   *
   * @param val a reference to the collection
   */
  public void setRef(final String val) {
    ref = val;
  }

  /**
   * @return a reference to the collection
   */
  public String getRef() {
    return ref;
  }

  /**
   *
   * @param val a displayable name
   */
  public void setName(final String val) {
    name = val;
  }

  /**
   * @return a displayable name
   */
  public String getName() {
    return name;
  }

  /**
   * @param val a list of component types
   */
  public void setComponentTypes(final List<String> val) {
    componentTypes = val;
  }

  /**
   * @return list of component types
   */
  public List<String> getComponentTypes() {
    return componentTypes;
  }
}

