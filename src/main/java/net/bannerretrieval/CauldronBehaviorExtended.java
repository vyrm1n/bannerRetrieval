package net.bannerretrieval;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public interface CauldronBehaviorExtended {
    CauldronInteraction WASH_SHIELD = (state, world, pos, player, hand, stack) -> {
        BannerPatternLayers bannerPatternsComponent = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        DyeColor bannerColor = stack.get(DataComponents.BASE_COLOR);
        assert bannerColor != null;

        if (bannerPatternsComponent.layers().isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack shieldItemStack = stack.copy();

        shieldItemStack.remove(DataComponents.BASE_COLOR);
        shieldItemStack.set(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);

        ItemStack bannerItemStack = new ItemStack(bannerItemFromColor(bannerColor));

        bannerItemStack.set(DataComponents.BANNER_PATTERNS, bannerPatternsComponent);

        stack.consume(1, player);
        if (stack.isEmpty()) {
            player.setItemInHand(hand, shieldItemStack);
        } else if (player.getInventory().add(shieldItemStack)) {
            player.inventoryMenu.sendAllDataToRemote();
        } else {
            player.drop(shieldItemStack, false, Prediction.SERVER_ONLY);
        }
        if (player.getInventory().add(bannerItemStack)) {
            player.inventoryMenu.sendAllDataToRemote();
        } else {
            player.drop(bannerItemStack, false, Prediction.SERVER_ONLY);
        }

        player.awardStat(Stats.USE_CAULDRON);
        LayeredCauldronBlock.lowerFillLevel(state, world, pos);
        return InteractionResult.SUCCESS;
    };

    private static Item bannerItemFromColor(DyeColor color) {
        // Individual Items.*_BANNER constants were replaced by a single ColorCollection in 26.3.
        Item banner = Items.BANNER.pick(color);
        return banner != null ? banner : Items.BANNER.purple();
    }
}
