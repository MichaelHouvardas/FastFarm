package com.fastfarm;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class GiveWheatAnimals implements ClientModInitializer {

    private static KeyBinding breedAnimalsKeybind;

    @Override
    public void onInitializeClient() {
        // Register the keybinding for breeding animals
        breedAnimalsKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fastfarm.breed_animals", // Translation key
                GLFW.GLFW_KEY_G, // Default key is 'G'
                "category.fastfarm" // Category
        ));

        // Register the event to check for key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && breedAnimalsKeybind.wasPressed()) {
                breedAnimalsAroundPlayer(client);
            }
        });
    }

    private void breedAnimalsAroundPlayer(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        BlockPos playerPos = client.player.getBlockPos();
        Vec3d minPos = Vec3d.ofBottomCenter(playerPos.add(-10, 0, -10)); // Convert BlockPos to Vec3d
        Vec3d maxPos = Vec3d.ofBottomCenter(playerPos.add(10, 5, 10));   // Convert BlockPos to Vec3d

        Box area = new Box(minPos, maxPos); // Define area to search for animals

        // Iterate over all entities in the area
        client.world.getEntitiesByClass(AnimalEntity.class, area, animal -> true).forEach(animal -> {
            if (animal instanceof CowEntity || animal instanceof PigEntity || animal instanceof SheepEntity) {
                // Check if the player has wheat in their hand
                ItemStack heldItem = client.player.getMainHandStack();
                if (heldItem.getItem() == Items.WHEAT) {
                    // Interact with the animal to breed it
                    client.interactionManager.interactEntity(client.player, animal, Hand.MAIN_HAND);
                }
            }
        });
    }
}
