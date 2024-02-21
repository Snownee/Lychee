package snownee.lychee.util.json;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import snownee.lychee.Lychee;

public class JsonFragmentManager {
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient().create();
	protected final ResourceManager resourceManager;
	protected final FileToIdConverter idConverter;
	protected final JsonFragment.Context context = new JsonFragment.Context(this::getOrLoad, Maps.newHashMap());
	private final Map<ResourceLocation, JsonElement> fragments = Maps.newHashMap();
	private final Set<JsonElement> processed = Sets.newIdentityHashSet();

	public JsonFragmentManager(ResourceManager resourceManager) {
		this(resourceManager, "lychee_fragments");
	}

	public JsonFragmentManager(ResourceManager resourceManager, String directory) {
		this.resourceManager = resourceManager;
		idConverter = FileToIdConverter.json(directory);
	}

	public void process(JsonElement json) {
		if (processed.contains(json)) {
			return;
		}
		try {
			processed.add(json);
			context.vars().clear();
			JsonFragment.process(json, context);
		} catch (Exception e) {
			Lychee.LOGGER.error("Failed to process fragment " + json, e);
		}
	}

	public JsonElement getOrLoad(ResourceLocation id) {
		return fragments.computeIfAbsent(id, k -> {
			try {
				JsonObject fragment = GSON.fromJson(
						resourceManager.getResourceOrThrow(idConverter.idToFile(id)).openAsReader(),
						JsonObject.class);
				return GsonHelper.getAsJsonObject(fragment, "value");
			} catch (Exception e) {
				Lychee.LOGGER.error("Failed to load fragment " + id, e);
				return null;
			}
		});
	}
}
