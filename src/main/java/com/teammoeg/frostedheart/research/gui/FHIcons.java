package com.teammoeg.frostedheart.research.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.teammoeg.frostedheart.util.SerializeUtil;
import com.teammoeg.frostedheart.util.Writeable;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class FHIcons {
	public static abstract class FHIcon extends Icon implements Writeable {
		public FHIcon() {
			super();
		}
	}

	private static class FHNopIcon extends FHIcon {
		public static final FHNopIcon INSTANCE = new FHNopIcon();

		/**
		 * @param e
		 */
		public static FHNopIcon get(JsonElement e) {
			return INSTANCE;
		}

		/**
		 * @param e
		 */
		public static FHNopIcon get(PacketBuffer e) {
			return INSTANCE;
		}

		private FHNopIcon() {
		}

		@Override
		public JsonElement serialize() {
			return JsonNull.INSTANCE;
		}

		@Override
		public void write(PacketBuffer buffer) {
			buffer.writeVarInt(0);
		}

		@Override
		public void draw(MatrixStack ms, int x, int y, int w, int h) {
		}

	}

	public static class FHItemIcon extends FHIcon {
		Icon nested;
		ItemStack stack;

		public FHItemIcon(JsonElement elm) {
			if (elm.isJsonPrimitive()) {
				stack=SerializeUtil.fromJson(elm);

			} else if (elm.isJsonObject()) {
				if(elm.getAsJsonObject().has("item")) {
					JsonElement it = elm.getAsJsonObject().get("item");
					stack = SerializeUtil.fromJson(it);
				}else stack = SerializeUtil.fromJson(elm);
			}
			init();
		}

		public FHItemIcon(PacketBuffer buffer) {
			stack = SerializeUtil.readOptional(buffer, PacketBuffer::readItemStack).orElse(null);
			init();
		}

		public FHItemIcon(ItemStack stack) {
			this.stack = stack;
			init();
		}
		public FHItemIcon(IItemProvider item2) {
			this(new ItemStack(item2));
		}

		private void init() {
			if (stack != null)
				nested = ItemIcon.getItemIcon(stack);
		}

		@Override
		public void draw(MatrixStack ms, int x, int y, int w, int h) {
			nested.draw(ms, x, y, w, h);
		}

		@Override
		public JsonElement serialize() {
			JsonElement je=SerializeUtil.toJson(stack);
			if(je.isJsonPrimitive())return je;
			JsonObject jo=new JsonObject();
			jo.addProperty("type","item");
			jo.add("item", je);
			return jo;
		}

		@Override
		public void write(PacketBuffer buffer) {
			buffer.writeVarInt(1);
			SerializeUtil.writeOptional2(buffer, stack, PacketBuffer::writeItemStack);
		}

		public ItemStack getStack() {
			return stack;
		}
	}

	private static class FHCombinedIcon extends FHIcon {
		FHIcon large;
		FHIcon small;

		public FHCombinedIcon(JsonElement elm) {
			if (elm.isJsonObject()) {
				large = FHIcons.getIcon(elm.getAsJsonObject().get("base"));
				small = FHIcons.getIcon(elm.getAsJsonObject().get("small"));
			}
		}

		public FHCombinedIcon(PacketBuffer buffer) {
			large = FHIcons.readIcon(buffer);
			small = FHIcons.readIcon(buffer);
		}

		public FHCombinedIcon(FHIcon base, FHIcon small) {
			this.large = base;
			this.small = small;
		}

		@Override
		public void draw(MatrixStack ms, int x, int y, int w, int h) {
			GuiHelper.setupDrawing();
			large.draw(ms, x, y, w, h);
			ms.push();
			ms.translate(0, 0, 110);// let's get top most
			GuiHelper.setupDrawing();
			small.draw(ms, x + w / 2, y + h / 2, w / 2, h / 2);
			ms.pop();
		}

		@Override
		public JsonElement serialize() {
			JsonObject jo = new JsonObject();
			jo.addProperty("type", "combined");
			jo.add("base", large.serialize());
			jo.add("small", small.serialize());
			return jo;
		}

		@Override
		public void write(PacketBuffer buffer) {
			buffer.writeVarInt(2);
			large.write(buffer);
			small.write(buffer);
		}
	}

	private static class FHAnimatedIcon extends FHIcon {
		List<FHIcon> icons;

		public FHAnimatedIcon(JsonElement elm) {
			if (elm.isJsonObject()) {
				JsonObject jo = elm.getAsJsonObject();
				icons = SerializeUtil.parseJsonElmList(jo.get("icons"), FHIcons::getIcon);
			} else if (elm.isJsonArray()) {
				icons = SerializeUtil.parseJsonElmList(elm, FHIcons::getIcon);
			}
		}

		public FHAnimatedIcon(PacketBuffer buffer) {
			icons = SerializeUtil.readList(buffer, FHIcons::readIcon);
		}

		public FHAnimatedIcon() {
			icons = new ArrayList<>();
		}

		public FHAnimatedIcon(FHIcon[] icons2) {
			for (FHIcon i : icons2)
				icons.add(i);
		}

		@Override
		public void draw(MatrixStack ms, int x, int y, int w, int h) {
			if (icons.size() > 0) {
				GuiHelper.setupDrawing();
				icons.get((int) ((System.currentTimeMillis() / 1000) % icons.size())).draw(ms, x, y, w, h);
			}
		}

		@Override
		public JsonElement serialize() {

			return SerializeUtil.toJsonList(icons, FHIcon::serialize);
		}

		@Override
		public void write(PacketBuffer buffer) {
			buffer.writeVarInt(3);
			SerializeUtil.writeList(buffer, icons, FHIcon::write);
		}
	}

	private static class FHIngredientIcon extends FHAnimatedIcon {
		Ingredient igd;

		public FHIngredientIcon(JsonElement elm) {
			this(Ingredient.deserialize(elm.getAsJsonObject().get("ingredient")));
		}

		public FHIngredientIcon(PacketBuffer buffer) {
			this(Ingredient.read(buffer));

		}

		public FHIngredientIcon(Ingredient i) {
			igd = i;
			for (ItemStack stack : igd.getMatchingStacks())
				icons.add(new FHItemIcon(stack));
		}

		@Override
		public JsonElement serialize() {
			JsonObject jo = new JsonObject();
			jo.addProperty("type", "ingredient");
			jo.add("ingredient", igd.serialize());
			return jo;
		}

		@Override
		public void write(PacketBuffer buffer) {
			buffer.writeVarInt(4);
			igd.write(buffer);
		}
	}

	private static class FHTextureIcon extends FHIcon {
		Icon nested;
		ResourceLocation rl;

		public FHTextureIcon(JsonElement elm) {
			this(new ResourceLocation(elm.getAsJsonObject().get("location").getAsString()));
		}

		public FHTextureIcon(PacketBuffer buffer) {
			this(buffer.readResourceLocation());
		}

		public FHTextureIcon(ResourceLocation rl) {
			this.rl = rl;
			nested = ImageIcon.getIcon(rl);
		}

		@Override
		public void draw(MatrixStack ms, int x, int y, int w, int h) {
			GuiHelper.setupDrawing();
			nested.draw(ms, x, y, w, h);
		}

		@Override
		public JsonElement serialize() {
			JsonObject jo = new JsonObject();
			jo.addProperty("type", "texture");
			jo.addProperty("location", rl.toString());
			return jo;
		}

		@Override
		public void write(PacketBuffer buffer) {
			buffer.writeVarInt(5);
			buffer.writeResourceLocation(rl);
		}
	}

	private static class FHTextureUVIcon extends FHIcon {
		Icon nested;
		ResourceLocation rl;
		int x, y, w, h, tw, th;

		public FHTextureUVIcon(JsonElement elm) {
			this(new ResourceLocation(elm.getAsJsonObject().get("location").getAsString()),
					elm.getAsJsonObject().get("x").getAsInt(), elm.getAsJsonObject().get("y").getAsInt(),
					elm.getAsJsonObject().get("w").getAsInt(), elm.getAsJsonObject().get("h").getAsInt(),
					JSONUtils.getInt(elm.getAsJsonObject(), "tw", 256),
					JSONUtils.getInt(elm.getAsJsonObject(), "th", 256));
		}

		public FHTextureUVIcon(PacketBuffer buffer) {
			this(buffer.readResourceLocation(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
					buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
		}

		public FHTextureUVIcon(ResourceLocation rl, int x, int y, int w, int h, int tw, int th) {
			super();
			this.rl = rl;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.tw = tw;
			this.th = th;
			nested = ImageIcon.getIcon(rl).withUV(x, y, w, h);
		}

		@Override
		public void draw(MatrixStack ms, int x, int y, int w, int h) {
			GuiHelper.setupDrawing();
			nested.draw(ms, x, y, w, h);
		}

		@Override
		public JsonElement serialize() {
			JsonObject jo = new JsonObject();
			jo.addProperty("type", "texture_uv");
			jo.addProperty("location", rl.toString());
			jo.addProperty("x", x);
			jo.addProperty("y", y);
			jo.addProperty("w", w);
			jo.addProperty("h", h);
			jo.addProperty("w", tw);
			jo.addProperty("h", th);
			return jo;
		}

		@Override
		public void write(PacketBuffer buffer) {
			buffer.writeVarInt(6);
			buffer.writeResourceLocation(rl);
			buffer.writeVarInt(x);
			buffer.writeVarInt(y);
			buffer.writeVarInt(w);
			buffer.writeVarInt(h);
			buffer.writeVarInt(tw);
			buffer.writeVarInt(th);
		}
	}

	private static class FHDelegateIcon extends FHIcon {
		Icon nested;

		public FHDelegateIcon(Icon nested) {
			super();
			this.nested = nested;
		}

		@Override
		public void draw(MatrixStack ms, int x, int y, int w, int h) {
			GuiHelper.setupDrawing();
			nested.draw(ms, x, y, w, h);
		}

		@Override
		public JsonElement serialize() {
			return FHNopIcon.INSTANCE.serialize();
		}

		@Override
		public void write(PacketBuffer buffer) {
			FHNopIcon.INSTANCE.write(buffer);
		}
	}

	public static final Map<String, Function<JsonElement, FHIcon>> JsonIcon = new HashMap<>();
	public static final List<Function<PacketBuffer, FHIcon>> BufferIcon = new ArrayList<>();
	static {
		JsonIcon.put("nop", FHNopIcon::get);
		JsonIcon.put("item", FHItemIcon::new);
		JsonIcon.put("combined", FHCombinedIcon::new);
		JsonIcon.put("animated", FHAnimatedIcon::new);
		JsonIcon.put("ingredient", FHIngredientIcon::new);
		JsonIcon.put("texture", FHTextureIcon::new);
		JsonIcon.put("texture_uv", FHTextureUVIcon::new);
		BufferIcon.add(FHNopIcon::get);
		BufferIcon.add(FHItemIcon::new);
		BufferIcon.add(FHCombinedIcon::new);
		BufferIcon.add(FHAnimatedIcon::new);
		BufferIcon.add(FHIngredientIcon::new);
		BufferIcon.add(FHTextureIcon::new);
		BufferIcon.add(FHTextureUVIcon::new);
	}

	public static FHIcon getIcon(ItemStack item) {
		return new FHItemIcon(item);
	}

	public static FHIcon getIcon(IItemProvider item) {
		return new FHItemIcon(item);
	}


	public static FHIcon getIcon(FHIcon base, FHIcon small) {
		return new FHCombinedIcon(base, small);
	}

	public static FHIcon getIcon(Ingredient i) {
		return new FHIngredientIcon(i);
	}

	public static FHIcon getIcon(ResourceLocation texture) {
		return new FHTextureIcon(texture);
	}

	public static FHIcon getIcon(ResourceLocation texture, int x, int y, int w, int h, int tw, int th) {
		return new FHTextureUVIcon(texture, x, y, w, h, tw, th);
	}

	/**
	 * Make a FHIcon delegate of the given icon, THIS IS NOT SERIALIZABLE
	 * All Serialization progress would result in getting an NOP icon.
	 */
	public static FHIcon getIcon(Icon i) {
		return new FHDelegateIcon(i);
	}

	public static FHIcon getAnimatedIcon(FHIcon... icons) {
		return new FHAnimatedIcon(icons);
	}
	/**
	 * This does not preserve nbt on save
	 * */
	public static FHIcon getStackIcons(Collection<ItemStack> rewards) {
		return new FHIngredientIcon(Ingredient.fromStacks(rewards.stream()));
	}

	public static FHIcon getIcon(JsonElement elm) {
		if (elm == null || elm.isJsonNull()) {
			return FHNopIcon.INSTANCE;
		}
		if (elm.isJsonPrimitive()) {
			return new FHItemIcon(elm);
		} else if (elm.isJsonArray()) {
			return new FHAnimatedIcon(elm);
		} else if (elm.isJsonObject()) {
			JsonObject jo=elm.getAsJsonObject();
			if(jo.has("type")) {
				Function<JsonElement, FHIcon> func = JsonIcon.get(jo.get("type").getAsString());
				if (func != null)
					return func.apply(elm);
			}else {
				return new FHItemIcon(jo);
			}
		}
		return FHNopIcon.INSTANCE;
	}

	public static FHIcon readIcon(PacketBuffer elm) {
		int type = elm.readVarInt();
		Function<PacketBuffer, FHIcon> func = BufferIcon.get(type);
		if (func != null)
			return func.apply(elm);
		return FHNopIcon.INSTANCE;
	}

	public static FHIcon nop() {
		return FHNopIcon.INSTANCE;
	}

	public static FHIcon getIcon(ItemStack[] stacks) {
		FHIcon[] icons = new FHIcon[stacks.length];
		for (int i = 0; i < stacks.length; i++)
			icons[i] = FHIcons.getIcon(stacks[i]);
		return getAnimatedIcon(icons);
	}

	public static FHIcon getIcon(IItemProvider[] items) {
		return new FHIngredientIcon(Ingredient.fromItems(items));
	}

	public static FHIcon getIcon(Collection<IItemProvider> items) {
		return new FHIngredientIcon(Ingredient.fromItems(items.toArray(new IItemProvider[0])));
	}

}
