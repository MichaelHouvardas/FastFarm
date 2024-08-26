package com.fastfarm;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public class FastFarmClient implements ClientModInitializer {
	private static KeyBinding fastFarmKeybind;

	@Override
	public void onInitializeClient() {
		// Register the keybinding
		fastFarmKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.fastfarm.toggle",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F,
				"category.fastfarm"
		));

		// Register the event to check for keypress
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null && fastFarmKeybind.wasPressed()) {
				performFarmingAction(client.player);
			}
		});
	}

	private void performFarmingAction(PlayerEntity player) {
		World world = player.getWorld();
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
					// Break the crop block (drops should be handled automatically by the game)
					world.breakBlock(pos, true);

					// Attempt to replant the corresponding seed
					replantCrop(player, world, pos, cropBlock);
				}
			}
		}
	}

	private void replantCrop(PlayerEntity player, World world, BlockPos pos, CropBlock cropBlock) {
		ItemStack seedStack = getSeedForCrop(player, cropBlock);

		if (!seedStack.isEmpty()) {
			// Use the seed from the player's inventory and place it at the position
			world.setBlockState(pos, cropBlock.getDefaultState().with(CropBlock.AGE, 0), 3);

			// Decrement the seed stack if the player is not in creative mode
			if (!player.isCreative()) {
				seedStack.decrement(1);
			}
		}
	}

	private ItemStack getSeedForCrop(PlayerEntity player, CropBlock cropBlock) {
		// Determine the corresponding seed for the given crop block
		ItemStack seedItem = ItemStack.EMPTY;
		if (cropBlock == Blocks.WHEAT) {
			seedItem = new ItemStack(Items.WHEAT_SEEDS);
		} else if (cropBlock == Blocks.CARROTS) {
			seedItem = new ItemStack(Items.CARROT);
		} else if (cropBlock == Blocks.POTATOES) {
			seedItem = new ItemStack(Items.POTATO);
		} else if (cropBlock == Blocks.BEETROOTS) {
			seedItem = new ItemStack(Items.BEETROOT_SEEDS);
		}

		// Search player's inventory for the seed item
		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack stack = player.getInventory().getStack(i);
			if (!stack.isEmpty() && stack.getItem() == seedItem.getItem()) {
				return stack;
			}
		}

		return ItemStack.EMPTY;
	}
}
