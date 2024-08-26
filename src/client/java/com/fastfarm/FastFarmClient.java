package com.fastfarm;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public class FastFarmClient implements ClientModInitializer {
	private static KeyBinding breakKeybind;
	private static KeyBinding placeKeybind;

	@Override
	public void onInitializeClient() {
		// Register the keybindings
		breakKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.fastfarm.break",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				"category.fastfarm"
		));

		placeKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.fastfarm.place",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_P,
				"category.fastfarm"
		));

		// Register the event to check for keypress
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null) {
				if (breakKeybind.wasPressed()) {
					performBreakingAction(client.player);
				}
				if (placeKeybind.wasPressed()) {
					performPlacingAction(client.player);
				}
			}
		});
	}

	private void performBreakingAction(PlayerEntity player) {
		MinecraftClient client = MinecraftClient.getInstance();
		World world = client.world;
		BlockPos playerPos = player.getBlockPos();

		// Define the area to iterate over
		BlockPos minPos = playerPos.add(-4, -1, -4);
		BlockPos maxPos = playerPos.add(4, 1, 4);

		// Iterate over all block positions in the area
		for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
			BlockState state = world.getBlockState(pos);

			// Check if the block is a fully grown crop
			if (state.getBlock() instanceof CropBlock cropBlock) {
				if (cropBlock.isMature(state)) {
					// Break the crop block
					breakCropBlock(client, pos);
				}
			}
		}
	}

	private void breakCropBlock(MinecraftClient client, BlockPos pos) {
		// Use MinecraftClient's built-in methods to interact with blocks
		client.interactionManager.updateBlockBreakingProgress(pos, Direction.DOWN);
		client.interactionManager.breakBlock(pos);
	}

	private void performPlacingAction(PlayerEntity player) {
		MinecraftClient client = MinecraftClient.getInstance();
		World world = client.world;
		BlockPos playerPos = player.getBlockPos();

		// Define the area to iterate over
		BlockPos minPos = playerPos.add(-4, -1, -4);
		BlockPos maxPos = playerPos.add(4, 1, 4);

		// Iterate over all block positions in the area
		for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
			BlockState state = world.getBlockState(pos);

			// Check if the block is an empty farmland
			if (state.getBlock() == Blocks.FARMLAND) {
				// Attempt to replant crops
				replantCrop(player, world, pos);
			}
		}
	}

	private void replantCrop(PlayerEntity player, World world, BlockPos pos) {
		ItemStack seedStack = getSeedForCrop(player);

		if (!seedStack.isEmpty()) {
			// Use the seed from the player's inventory and place it at the position
			world.setBlockState(pos.up(), Blocks.WHEAT.getDefaultState().with(CropBlock.AGE, 0), 3);

			// Decrement the seed stack if the player is not in creative mode
			if (!player.isCreative()) {
				seedStack.decrement(1);
			}
		}
	}

	private ItemStack getSeedForCrop(PlayerEntity player) {
		// Find the first stack of seeds in the player's inventory
		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack stack = player.getInventory().getStack(i);
			if (!stack.isEmpty() && (stack.getItem() == Items.WHEAT_SEEDS ||
					stack.getItem() == Items.CARROT ||
					stack.getItem() == Items.POTATO ||
					stack.getItem() == Items.BEETROOT_SEEDS)) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}
}
