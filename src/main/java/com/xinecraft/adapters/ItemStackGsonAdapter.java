package com.xinecraft.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.Map;

public class ItemStackGsonAdapter implements JsonSerializer<ItemStack> {
    @Override
    public JsonElement serialize(ItemStack itemStack, Type type, JsonSerializationContext context) {
        if (itemStack == null) {
            return null;
        }

        JsonObject jsonItem = new JsonObject();

        Map<String, Object> itemJson = itemStack.serialize();
        for (Map.Entry<String, Object> entry : itemJson.entrySet()) {
            jsonItem.addProperty(entry.getKey(), entry.getValue().toString());
        }

        return jsonItem;
    }
}
