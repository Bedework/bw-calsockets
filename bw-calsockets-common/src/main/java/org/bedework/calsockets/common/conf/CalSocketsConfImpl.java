/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calsockets.common.conf;

import org.bedework.calsockets.common.CalSocketsConf;
import org.bedework.util.config.ConfigBase;

/**
 * User: mike Date: 2/26/18 Time: 23:48
 */
public class CalSocketsConfImpl extends ConfigBase<CalSocketsConfImpl>
        implements CalSocketsConf {
  private String calDAVUrl;
  private String token;

  @Override
  public String getCalDAVUrl() {
    return calDAVUrl;
  }

  @Override
  public void setCalDAVUrl(final String val) {
    calDAVUrl = val;
  }

  @Override
  public String getToken() {
    return token;
  }

  @Override
  public void setToken(final String val) {
    token = val;
  }
}
