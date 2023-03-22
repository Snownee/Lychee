package snownee.lychee.fragment;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeConfig;

public class Fragments extends SimpleJsonResourceReloadListener {
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient().create();
	private final Map<String, JsonElement> fragments = Maps.newHashMap();
	public static final Fragments INSTANCE = new Fragments("lychee_fragments");

	public Fragments(String dir) {
		super(GSON, dir);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		fragments.clear();
		map.forEach((id, json) -> fragments.put(id.getPath(), json.getAsJsonObject().get("value")));
	}

	public void process(JsonElement json) {
		if (!LycheeConfig.enableFragment || fragments.isEmpty())
			return;
		try {
			JsonFragment.process(json, new JsonFragment.Context(fragments::get, Maps.newHashMap()));
		} catch (Exception e) {
			Lychee.LOGGER.error("Failed to process fragment {}", json);
			throw e;
		}
	}
}
