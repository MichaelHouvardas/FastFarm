package com.fastfarm;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public class FastFarmClient implements ClientModInitializer {
	private static KeyBinding breakKeybind;

	@Override
	public void onInitializeClient() {
		// Register the keybinding for breaking crops
		breakKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.fastfarm.break",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				"category.fastfarm"
		));

		// Register the event to check for keypress
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null && breakKeybind.wasPressed()) {
				performBreakingAction(client.player);
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
}
