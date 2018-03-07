/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calsockets.common.conf;

import org.bedework.util.jmx.ConfBase;

/**
 * User: mike Date: 2/26/18 Time: 23:54
 */
public class CalSocketsConfig extends ConfBase<CalSocketsConfImpl>
        implements CalSocketsConfigMbean {
  /* Name of the property holding the location of the config data */
  private static final String confuriPname = "org.bedework.calsocket.confuri";

  /**
   */
  public CalSocketsConfig() {
    super("org.bedework.calsocket:service=BwCalsockets");

    setConfigName("calsocket");

    setConfigPname(confuriPname);
  }

  /* ========================================================================
   * Attributes
   * ======================================================================== */

  @Override
  public String getCalDAVUrl() {
    return getConfig().getCalDAVUrl();
  }

  @Override
  public void setCalDAVUrl(final String val) {
    getConfig().setCalDAVUrl(val);
  }

  @Override
  public String getToken() {
    return getConfig().getToken();
  }

  @Override
  public void setToken(final String val) {
    getConfig().setToken(val);
  }

  @Override
  public String loadConfig() {
    return loadConfig(CalSocketsConfImpl.class);
  }
}
