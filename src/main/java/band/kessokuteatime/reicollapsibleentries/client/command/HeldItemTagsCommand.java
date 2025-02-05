package band.kessokuteatime.reicollapsibleentries.client.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import band.kessokuteatime.reicollapsibleentries.REICollapsibleEntries;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class HeldItemTagsCommand implements Command<FabricClientCommandSource> {
	@Override
	@SuppressWarnings("all")
	public int run(CommandContext<FabricClientCommandSource> context) {
		ItemStack stack = context.getSource().getPlayer().getMainHandStack();
		Identifier itemId = Registries.ITEM.getId(stack.getItem());

		if (stack.isOf(Items.AIR)) return 0;
		long size = stack.getItem().getRegistryEntry().streamTags().count();

		context.getSource().sendFeedback(Text.translatable(
				size == 0 ? "tagged.none" : size == 1 ? "tagged.only" : "tagged.more",
				stack.toHoverableText()
		));

		if (size > 0) {
			context.getSource().sendFeedback(streamTags(stack));
		}

		return SINGLE_SUCCESS;
	}

	@SuppressWarnings("deprecation")
	private Text streamTags(ItemStack stack) {
		return stack.getItem().getRegistryEntry().streamTags()
				.map(tag -> REICollapsibleEntries.paintIdentifier(tag.id())
						.styled(style -> style
								.withHoverEvent(new HoverEvent(
										HoverEvent.Action.SHOW_TEXT,
										Text.translatable("command.reicollapsibleentries.tags.click")
								))
								.withClickEvent(new ClickEvent(
										ClickEvent.Action.OPEN_URL,
										tag.id().toString()
								)))
				)
				.reduce((a, b) -> a.append("\n").append(b))
				.orElse(Text.empty());
	}
}
