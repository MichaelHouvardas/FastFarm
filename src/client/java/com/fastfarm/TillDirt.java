package com.fastfarm;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public class TillDirt implements ClientModInitializer {
    private static KeyBinding tillKeybind;

    @Override
    public void onInitializeClient() {
        // Register the keybinding for tilling dirt
        tillKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fastfarm.till",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_T,
                "category.fastfarm"
        ));

        // Register the event to check for keypress
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                if (tillKeybind.wasPressed()) {
                    performTillingAction(client.player);
                }
            }
        });
    }

    private void performTillingAction(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;
        BlockPos playerPos = player.getBlockPos();

        // Check if the player is holding a hoe
        ItemStack heldItem = player.getMainHandStack();
        if (heldItem.getItem() instanceof HoeItem) {

            // Define the area to iterate over (3-block radius)
            BlockPos minPos = playerPos.add(-3, -1, -3);
            BlockPos maxPos = playerPos.add(3, 1, 3);

            // Iterate over all block positions in the area
            for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
                if (world.getBlockState(pos).getBlock() == Blocks.GRASS_BLOCK ||
                        world.getBlockState(pos).getBlock() == Blocks.DIRT) {

                    // Simulate player using the hoe on the block
                    BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);

                    // Attempt to interact with the block
                    if (client.interactionManager != null) {
                        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hitResult);
                    }

                }
            }
        }
    }
}
