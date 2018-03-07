/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calsockets.common;

import org.bedework.calsockets.common.requests.InitRequest;
import org.bedework.calsockets.common.requests.ItemRequest;
import org.bedework.calsockets.common.responses.InitResponse;
import org.bedework.calsockets.common.responses.ItemResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: mike
 * Date: 6/13/15
 * Time: 11:08 PM
 */
public class JsonMapper extends ObjectMapper {
  public JsonMapper() {
    setSerializationInclusion(JsonInclude.Include.NON_NULL);
    // configure(JsonFactory.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    final SimpleModule module =
            new SimpleModule("BwCal DeserializerModule",
                             new Version(1, 0, 0, null));
    final EntityDeserializer deserializer =
            new EntityDeserializer();
    deserializer.registerEntity(MessageBase.initAction,
                                InitRequest.class);
    deserializer.registerEntity(MessageBase.initResponse,
                                InitResponse.class);
    deserializer.registerEntity(MessageBase.getItemAction,
                                ItemRequest.class);
    deserializer.registerEntity(MessageBase.getItemResponse,
                                ItemResponse.class);

    module.addDeserializer(MessageBase.class, deserializer);

    registerModule(module);
  }

  private class EntityDeserializer extends
          StdDeserializer<MessageBase> {
    private final Map<String, Class<? extends MessageBase>> registry =
            new HashMap<>();

    EntityDeserializer() {
      super(MessageBase.class);
    }

    void registerEntity(final String entityType,
                        final Class<? extends MessageBase> cl) {
      registry.put(entityType, cl);
    }

    @Override
    public MessageBase deserialize(final JsonParser jp,
                                   final DeserializationContext ctxt)
            throws IOException {
      final ObjectMapper mapper = (ObjectMapper) jp.getCodec();
      final ObjectNode root = mapper.readTree(jp);

      final JsonNode node = root.get("action");

      if (node == null) {
        return null;
      }

      final String type = node.textValue();

      final Class<? extends MessageBase> cl = registry.get(type);

      if (cl == null) {
        return null;
      }

      return mapper.treeToValue(root, cl);
    }
  }
}
