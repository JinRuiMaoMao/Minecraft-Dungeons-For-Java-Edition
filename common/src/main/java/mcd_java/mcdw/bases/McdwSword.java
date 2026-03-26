/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.bases;

import mcd_java.mcdw.api.interfaces.IInnateEnchantment;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.api.util.RarityHelper;
import mcd_java.mcdw.enums.SwordsID;
import mcd_java.mcdw.mixin.mcdw.InsulatedAxeItemAccessor;
import mcd_java.mcdw.registries.ItemGroupRegistry;
import com.google.common.collect.BiMap;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.Tier;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.WeatheringCopper;
import net.minecraft.block.BlockState;
import net.minecraft.world.event.GameEvent;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class McdwSword extends SwordItem implements IInnateEnchantment {
    String[] repairIngredient;
    SwordsID swordsEnum;

    public McdwSword(SwordsID swordsEnum, Tier material, int attackDamage, float attackSpeed, String[] repairIngredient) {
        super(material, attackDamage, attackSpeed,
                new Item.Properties().rarity(RarityHelper.fromToolMaterial(material)));
        ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.MELEE).register(entries -> entries.accept(this.getDefaultInstance()));
        this.swordsEnum = swordsEnum;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return CleanlinessHelper.canRepairCheck(repairIngredient, ingredient);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ActionResult useOn(ItemUsageContext context) {
        World world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        PlayerEntity playerEntity = context.getPlayer();
        BlockState blockState = world.getBlockState(blockPos);
        Optional<BlockState> strippedState = this.getStrippedState(blockState);
        Optional<BlockState> decreasedOxidationState = WeatheringCopper.getPrevious(blockState);
        Optional<BlockState> blockStateOptional = Optional.ofNullable((Block)((BiMap)HoneycombItem.WAX_OFF_BY_BLOCK.get()).get(blockState.getBlock())).map((block) -> block.withPropertiesOf(blockState));
        ItemStack itemStack = context.getItemInHand();
        Optional<BlockState> empty = Optional.empty();
        if (context.getItemInHand().is(SwordsID.SWORD_MECHANIZED_SAWBLADE.getItem())) {
            if (strippedState.isPresent()) {
                world.playSound(playerEntity, blockPos, SoundEvents.AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
                empty = strippedState;
            } else if (decreasedOxidationState.isPresent()) {
                world.playSound(playerEntity, blockPos, SoundEvents.AXE_SCRAPE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.levelEvent(playerEntity, 3005, blockPos, 0);
                empty = decreasedOxidationState;
            } else if (blockStateOptional.isPresent()) {
                world.playSound(playerEntity, blockPos, SoundEvents.AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.levelEvent(playerEntity, 3004, blockPos, 0);
                empty = blockStateOptional;
            }

            if (empty.isPresent()) {
                if (playerEntity instanceof ServerPlayerEntity) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)playerEntity, blockPos, itemStack);
                }

                world.setBlock(blockPos, empty.get(), 11);
                world.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(playerEntity, empty.get()));
                if (playerEntity != null) {
                    itemStack.hurtAndBreak(1, playerEntity, (p) -> p.broadcastBreakEvent(context.getHand()));
                }

                return InteractionResult.sidedSuccess(world.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (stack.is(SwordsID.SWORD_MECHANIZED_SAWBLADE.getItem())) {
            if (state.is(BlockTags.MINEABLE_WITH_AXE))
                return 8.0F;
        }
        return super.getDestroySpeed(stack, state);
    }


    private Optional<BlockState> getStrippedState(BlockState state) {
        return Optional.ofNullable(InsulatedAxeItemAccessor.getSTRIPPED_BLOCKS().get(state.getBlock())).map((block) -> block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
    }

    @Override
    public ItemStack getDefaultInstance() {
        return getInnateEnchantedStack(this);
    }

    @Override
    public Map<Enchantment, Integer> getInnateEnchantments() {
        if (this.swordsEnum.getIsEnabled())
            return this.swordsEnum.getInnateEnchantments();
        return Map.of();
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext){
        super.appendTooltip(stack, world, tooltip, tooltipContext);
        CleanlinessHelper.mcdw$tooltipHelper(stack, tooltip, 16);
    }
}
