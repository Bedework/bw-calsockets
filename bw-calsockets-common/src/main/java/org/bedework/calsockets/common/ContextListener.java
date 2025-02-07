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

import org.bedework.calsockets.common.conf.CalSocketsConfigMbean;
import org.bedework.util.jmx.ConfBase;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import javax.management.ObjectName;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * @author douglm
 *
 */
public class ContextListener implements Logged, ServletContextListener {
  static class Configurator extends ConfBase {
    CalSocketsConfigMbean sysInfo;

    public Configurator() {
      super("org.bedework.calsocket:service=BwCalsockets");
    }

    @Override
    public String loadConfig() {
      return null;
    }

    @Override
    public void start() {
      try {
        getManagementContext().start();

        final ConfBase conf = loadInstance(
                "org.bedework.calsockets.common.conf.CalSocketsConfig");
        register(new ObjectName(conf.getServiceName()),
                 conf);
        conf.loadConfig();
        sysInfo = (CalSocketsConfigMbean)conf;

        sysInfo.start();
      } catch (final Throwable t){
        t.printStackTrace();
      }
    }

    @Override
    public void stop() {
      try {
        getManagementContext().stop();
      } catch (final Throwable t){
        t.printStackTrace();
      }
    }
  }

  private static ConfBase loadInstance(final String cname) {
    try {
      final ClassLoader loader = Thread.currentThread().getContextClassLoader();
      final Class cl = loader.loadClass(cname);

      if (cl == null) {
        throw new Exception("Class " + cname + " not found");
      }

      final Object o = cl.newInstance();

      if (o == null) {
        throw new Exception("Unable to instantiate class " + cname);
      }

      return (ConfBase)o;
    } catch (final Throwable t) {
      t.printStackTrace();
      throw new RuntimeException(t);
    }
  }

  private static final Configurator conf = new Configurator();

  static {
    // Initialise now so it's visible after deployment
    conf.start();
  }

  @Override
  public void contextInitialized(final ServletContextEvent sce) {
  }

  @Override
  public void contextDestroyed(final ServletContextEvent sce) {
    conf.stop();
  }

  public static CalSocketsConfigMbean getSysInfo() {
    return conf.sysInfo;
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
