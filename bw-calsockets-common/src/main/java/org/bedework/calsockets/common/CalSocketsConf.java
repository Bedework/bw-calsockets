/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calsockets.common;

import org.bedework.util.jmx.MBeanInfo;

/**
 * User: mike Date: 2/26/18 Time: 23:41
 */
public interface CalSocketsConf {
  /**
   * @return location of server
   */
  @MBeanInfo("Location of server")
  String getCalDAVUrl();

  /**
   *
   * @param val location of server
   */
  void setCalDAVUrl(String val);

  /**
   *
   * @return authentication token
   */
  @MBeanInfo("authentication token")
  String getToken();

  /**
   *
   * @param val authentication token
   */
  void setToken(final String val);
}
