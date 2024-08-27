package com.fastfarm;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.BlockItem;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public class FastFarmPlace implements ClientModInitializer {

    private static KeyBinding togglePlaceBlockKeybind;
    private static KeyBinding placeBlockOnceKeybind;
    private boolean placingBlocks = false; // Toggle flag

    @Override
    public void onInitializeClient() {
        // Register the keybindings
        togglePlaceBlockKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fastfarm.toggle_place_block", // Translation key
                GLFW.GLFW_KEY_P, // Default key is 'P'
                "category.fastfarm" // Category
        ));

        placeBlockOnceKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fastfarm.place_block_once", // Translation key
                GLFW.GLFW_KEY_L, // Default key is 'L'
                "category.fastfarm" // Category
        ));

        // Register a tick event listener to check for key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                if (togglePlaceBlockKeybind.wasPressed()) {
                    // Toggle the placingBlocks flag when the key is pressed
                    placingBlocks = !placingBlocks;

                    // Notify the player about the toggle state
                    if (placingBlocks) {
                        client.player.sendMessage(Text.literal("Block placing toggled ON"), true);
                    } else {
                        client.player.sendMessage(Text.literal("Block placing toggled OFF"), true);
                    }
                }

                // If placingBlocks is true, continue placing blocks
                if (placingBlocks) {
                    placeBlockAroundPlayer(client);
                }

                // Check if the single-place keybind is pressed
                if (placeBlockOnceKeybind.wasPressed()) {
                    placeBlockBelow(client); // Place block once without toggling
                }
            }
        });
    }

    private void placeBlockAroundPlayer(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        World world = client.world;
        BlockPos playerPos = client.player.getBlockPos();

        // Define the area to iterate over (3-block radius)
        BlockPos minPos = playerPos.add(-3, -1, -3);
        BlockPos maxPos = playerPos.add(3, 1, 3);

        // Iterate over all block positions in the area
        for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
            if (world.getBlockState(pos).getBlock() == Blocks.FARMLAND) {
                // Attempt to replant crops if there's an empty space above farmland
                placeCropAt(client, pos.up());
            }
        }
    }

    private void placeCropAt(MinecraftClient client, BlockPos pos) {
        if (client.player == null || client.world == null) {
            return;
        }

        World world = client.world;

        // Create a hit result for placing a block
        BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);

        // Check if the main hand contains a block item, otherwise use the offhand
        if (client.player.getMainHandStack().getItem() instanceof BlockItem) {
            client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hitResult);
        } else if (client.player.getOffHandStack().getItem() instanceof BlockItem) {
            client.interactionManager.interactBlock(client.player, Hand.OFF_HAND, hitResult);
        }
    }

    private void placeBlockBelow(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        World world = client.world;
        BlockPos playerPos = client.player.getBlockPos().down(); // Get block position below player

        // Check if we can place a block at that position
        if (world.isAir(playerPos)) {
            // Attempt to place the block from main hand or offhand
            placeCropAt(client, playerPos);
        }
    }
}
