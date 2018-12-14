/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calsockets.server;

import org.bedework.calsockets.common.CalDAVClient;
import org.bedework.calsockets.common.CalDAVClient.PrincipalInfo;
import org.bedework.calsockets.common.CalSocketsConf;
import org.bedework.calsockets.common.ContextListener;
import org.bedework.calsockets.common.JsonMapper;
import org.bedework.calsockets.common.MessageBase;
import org.bedework.calsockets.common.artifacts.ResourceCollection;
import org.bedework.calsockets.common.exc.CalSocketsException;
import org.bedework.calsockets.common.requests.InitRequest;
import org.bedework.calsockets.common.requests.SyncCollectionRequest;
import org.bedework.calsockets.common.responses.InitResponse;
import org.bedework.calsockets.common.responses.Response;
import org.bedework.calsockets.common.responses.SyncCollectionResponse;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.websocket.Session;

import static org.bedework.calsockets.common.responses.Response.Status.ok;

/**
 * User: mike Date: 2/23/18 Time: 00:54
 */
public class BwSession implements Logged {
  //@Resource(lookup="java:jboss/ee/concurrency/factory/default")
  private final ManagedThreadFactory threadFactory;

  private final Session wsSession;

  private final static ObjectMapper om = new JsonMapper();

  private final Principal pr;

  PrincipalInfo pi;

  final CalDAVClient client;

  public BwSession(final Session wsSession,
                   final Principal pr) throws CalSocketsException {
    this.wsSession = wsSession;
    this.pr = pr;

    threadFactory = getThreadFactory();

    client = new CalDAVClient(getConf().getCalDAVUrl(), pr);
  }

  public Session getWsSession() {
    return wsSession;
  }

  public void send(final MessageBase msg) throws IOException {
    getWsSession().getBasicRemote().sendText(toJson(msg));
  }

  public String toJson(final MessageBase msg) {
    try {
      final StringWriter sw = new StringWriter();
      om.writeValue(sw, msg);
      return sw.toString();
    } catch (final Throwable t) {
      // TODO - create error response string
      if (debug()) {
        error(t);
      }
      return "{\"status\": error, \"message\":\"" + t.getMessage() + "\"}";
    }
  }

  public InitResponse getInitResponse(final InitRequest req) {
    final InitResponse resp = new InitResponse();

    resp.setId(req.getId());

    final PrincipalInfo pi = getPi(resp);

    if ((pi == null) || (pi.homeUrl == null)) {
      if (resp.getStatus() != ok) {
        return resp;
      }
      return Response.error(resp, "No calendar home for " + pr);
    }

    final List<ResourceCollection> collections =
            client.getCalendars(resp, pi.homeUrl);
    if (collections == null) {
      return resp;
    }

    resp.setCollections(collections);

    return Response.ok(resp, null);
  }

  public static class SyncTask implements Logged, Runnable {
    private final SyncCollectionRequest req;
    private final BwSession session;
    private Thread thread;
    private boolean running;

    SyncTask(final SyncCollectionRequest req,
             final BwSession session) {
      this.req = new SyncCollectionRequest();
      req.copyTo(this.req);
      this.session = session;
    }

    public void run() {
      thread = Thread.currentThread();
      running = true;
      if (!sleep(500)) {
        return;
      }

      info("Sync thread started for ");

      //noinspection StatementWithEmptyBody
      while (running && doSync() && sleep(10000)) {
      }

      info("Sync thread exit");
    }

    private boolean doSync() {
      final SyncCollectionResponse resp = new SyncCollectionResponse();

      resp.setId(req.getId());

      final List<String> hrefs =
              session.client.syncCollection(req, resp);

      if (Util.isEmpty(hrefs)) {
        return send(resp);
      }

      if (debug()) {
        debug("Received " + hrefs.size() + " hrefs");
      }

      for (final String href: hrefs) {
        final SyncCollectionResponse resresp = new SyncCollectionResponse();

        resresp.setId(req.getId());

        final String rsrc = session.client.getString(resresp, href);

        resresp.setItems(Collections.singletonList(rsrc));

        if (!send(resresp)) {
          return false;
        }
      }

      return true;
    }

    boolean send(final SyncCollectionResponse resp) {
      try {
        session.send(resp);
        return resp.isOk();
      } catch (final IOException ioe) {
        warn(ioe.getMessage());
        return false;
      }
    }

    boolean sleep(final long t) {
      try {
        Thread.sleep(t);
        return true;
      } catch (final InterruptedException ignored) {
        info("Sync thread interrupted");
        return false;
      }
    }

    public void stop() {
      running = false;
      if (thread != null) {
        thread.interrupt();
      }
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

  private final static Map<String, SyncTask> syncers = new HashMap<>();

  public SyncCollectionResponse syncCollection(
          final SyncCollectionRequest req) {
    final SyncCollectionResponse resp = new SyncCollectionResponse();

    resp.setId(req.getId());

    synchronized (syncers) {
      /* TODO - cluster issues - only one member syncing a collection
       */

      final SyncTask curSyncer = syncers.get(req.getCollectionRef());
      if (curSyncer != null) {
        curSyncer.stop();
        syncers.remove(req.getCollectionRef());
      }
    }

    try {
      client.syncOk(req, resp);

      if (resp.getStatus() != ok) {
        return resp;
      }

      final SyncTask syncTask = new SyncTask(req, this);
      final Thread thread = threadFactory.newThread(syncTask);
      thread.start();

      return Response.ok(resp, null);
    } catch (final Throwable t) {
      if (debug()) {
        error(t);
      }

      return Response.error(resp, t);
    }
  }

  public void close() {
  }

  /* ====================================================================
   *                         Private methods
   * ==================================================================== */

  private CalSocketsConf getConf() {
    return ContextListener.getSysInfo();
  }

  private PrincipalInfo getPi(final Response resp) {
    if (pi != null) {
      return pi;
    }

    try {
      pi = client.getPrincipalDetails();
      return pi;
    } catch (final Throwable t) {
      Response.error(resp, t);
      return null;
    }
  }
  /* ====================================================================
   *                         CalDAV methods
   * ==================================================================== */

  private ManagedThreadFactory getThreadFactory() {
    try {
      final ManagedThreadFactory factory;

      final Context ctx = new InitialContext();
      /*
      try {
        Context jcectx = (Context)ctx.lookup("java:comp/env/");

        // Still here - use that
        if (jcectx != null) {
          ctx = jcectx;
        }
      } catch (NamingException nfe) {
        // Stay with root
      }
      */

      factory = (ManagedThreadFactory)ctx.lookup(
              "java:jboss/ee/concurrency/factory/default");

      return factory;
    } catch (final Throwable t) {
      if (debug()) {
        error(t);
      }

      return null;
    }
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
