/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calsockets.server;

import org.bedework.calsockets.common.JsonMapper;
import org.bedework.calsockets.common.MessageBase;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

/**
 * User: mike Date: 2/23/18 Time: 15:15
 */
public class BwCalDecoder
        implements Logged, Decoder.TextStream<MessageBase> {
  private final ObjectMapper om;

  public BwCalDecoder() {
    om = new JsonMapper();
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    om.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
  }

  @Override
  public MessageBase decode(final Reader reader)
          throws DecodeException, IOException {
    try {
      return readObject(reader, MessageBase.class);
    } catch (final Throwable t) {
      if (debug()) {
        error(t);
      }
      throw new DecodeException((String)null, t.getMessage(), t);
    }
  }

  @Override
  public void init(final EndpointConfig endpointConfig) {

  }

  @Override
  public void destroy() {

  }

  public <T> T readObject(final Reader rdr,
                          final Class<T> resultType) throws Throwable {
    return om.readValue(rdr, resultType);
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
