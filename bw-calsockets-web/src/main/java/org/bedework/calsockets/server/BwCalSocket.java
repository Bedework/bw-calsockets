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
package org.bedework.calsockets.server;

import org.bedework.calsockets.common.MessageBase;
import org.bedework.calsockets.common.requests.InitRequest;
import org.bedework.calsockets.common.requests.SyncCollectionRequest;
import org.bedework.calsockets.common.responses.InitResponse;
import org.bedework.calsockets.common.responses.SyncCollectionResponse;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import static org.bedework.calsockets.common.MessageBase.getItemAction;
import static org.bedework.calsockets.common.MessageBase.getItemsAction;
import static org.bedework.calsockets.common.MessageBase.initAction;
import static org.bedework.calsockets.common.MessageBase.syncCollectionAction;

/**
 * User: mike Date: 2/7/18 Time: 10:21
 */
@SuppressWarnings("unused")
@ServerEndpoint(value = "/wskt",
        decoders = {BwCalDecoder.class})
public class BwCalSocket implements Logged {
  private static final Map<String, BwSession> bwSessions = new HashMap<>();

  /* ====================================================================
   *                     Websocket methods
   * ====================================================================
   */
  @OnOpen
  public void open(final Session session) {
    final Principal pr = session.getUserPrincipal();
    final String id = session.getId();

    if (pr == null) {
      // Ignore for the moment
      if (debug()) {
        debug("Open for session " + id +
                      " with null principal");
      }
    } else {
      if (debug()) {
        debug("Open for session " + id +
                      " with principal " + pr);
      }


      synchronized (bwSessions) {
        // Do close - just in case
        close(session);

        try {
          final BwSession bws = new BwSession(session, pr);

          bwSessions.put(id, bws);
        } catch (final Throwable t) {
          if (debug()) {
            error(t);
          }

          // TODO - send back some sort of error response then close.
        }
      } // synchronized
    }
  }

  @OnClose
  public void close(final Session session) {
    final String id = session.getId();

    if (debug()) {
      debug("Close for session " + id);
    }

    final BwSession bws = bwSessions.get(id);

    if (bws == null) {
      return;
    }

    bws.close();
    bwSessions.remove(id);
  }

  @OnError
  public void onError(final Throwable error) {
    error(error);
  }

  @OnMessage
  public void handleMessage(final MessageBase message,
                            final Session session) {
    final String id = session.getId();

    if (debug()) {
      debug("received: " + message + " for id " + id);
    }

    final BwSession bws = bwSessions.get(id);

    if (bws == null) {
      if (debug()) {
        debug("No active session for " + id);
      }
      return;
    }

    try {
      switch (message.getAction()) {
        case initAction:
          handleInit(bws, (InitRequest)message);
          break;

        case getItemAction:
          break;

        case getItemsAction:
          break;

        case syncCollectionAction:
          handleSync(bws, (SyncCollectionRequest)message);
          break;

        default:
      }
    } catch (final IOException ioe) {
      error(ioe);
    }
  }

  private void handleInit(final BwSession bws,
                          final InitRequest req) throws IOException {
    final InitResponse resp = bws.getInitResponse(req);

    bws.send(resp);
  }

  private void handleSync(final BwSession bws,
                          final SyncCollectionRequest req) throws IOException {
    final SyncCollectionResponse resp = bws.syncCollection(req);

    bws.send(resp);
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
