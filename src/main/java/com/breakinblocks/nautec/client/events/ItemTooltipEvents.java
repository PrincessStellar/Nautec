package com.breakinblocks.nautec.client.events;

import java.util.Locale;
import net.minecraft.world.item.BlockItem;
import com.breakinblocks.nautec.content.dishstorage.DishStorageBlock;
import com.breakinblocks.nautec.content.biometank.BiomeTankBlock;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.api.gateways.PackedGateway;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class ItemTooltipEvents {
    private ItemTooltipEvents() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        GatewayAddress address = stack.get(NTDataComponents.GATEWAY_ADDRESS.get());
        PackedGateway packed = stack.get(NTDataComponents.GATEWAY_PACKED.get());
        if (address != null) {
            event.getToolTip().add(Component.translatable("nautec.monocle.address").withStyle(ChatFormatting.GRAY).append(address.describe()));
        }
        if (stack.is(NTBlocks.RUSTY_CRATE.asItem())) {
            event.getToolTip().add(Component.translatable("nautec.crate.rusted_tooltip").withStyle(ChatFormatting.GOLD));
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            if (blockItem.getBlock() instanceof DishStorageBlock storage) {
                event.getToolTip().add(Component.translatable("nautec.dish_storage.capacity", storage.getCapacity()).withStyle(ChatFormatting.GRAY));
            } else if (blockItem.getBlock() instanceof BiomeTankBlock) {
                Holder<Item> plant = stack.get(NTDataComponents.BIOME_TANK_PLANT.get());
                if (plant == null) {
                    event.getToolTip().add(Component.translatable("nautec.biome_tank.unseeded").withStyle(ChatFormatting.GRAY));
                } else {
                    event.getToolTip().add(Component.translatable("nautec.biome_tank.grows", plant.value().getName(new ItemStack(plant)),
                            String.format(Locale.ROOT, "%.1f", NTConfig.biomeTankTicks / 20.0)).withStyle(ChatFormatting.GRAY));
                }
            }
        }
        if (packed != null) {
            event.getToolTip().add(Component.translatable("nautec.gateway.packed").withStyle(ChatFormatting.AQUA));
            if (packed.wild()) {
                event.getToolTip().add(Component.translatable("nautec.gateway.packed_wild").withStyle(ChatFormatting.DARK_AQUA));
            }
        }
    }
}
