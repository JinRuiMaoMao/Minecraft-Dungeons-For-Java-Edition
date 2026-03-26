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
import net.minecraft.core.BlockPos;
import net.minecraft.item.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
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
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        Player playerEntity = context.getPlayer();
        BlockState blockState = world.getBlockState(blockPos);
        Optional<BlockState> strippedState = this.getStrippedState(blockState);
        Optional<BlockState> decreasedOxidationState = WeatheringCopper.getPrevious(blockState);
        Optional<BlockState> blockStateOptional = Optional.ofNullable((Block)((BiMap)HoneycombItem.WAX_OFF_BY_BLOCK.get()).get(blockState.getBlock())).map((block) -> block.withPropertiesOf(blockState));
        ItemStack itemStack = context.getItemInHand();
        Optional<BlockState> empty = Optional.empty();
        if (context.getItemInHand().is(SwordsID.SWORD_MECHANIZED_SAWBLADE.getItem())) {
            if (strippedState.isPresent()) {
                world.playSound(playerEntity, blockPos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
                empty = strippedState;
            } else if (decreasedOxidationState.isPresent()) {
                world.playSound(playerEntity, blockPos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                world.levelEvent(playerEntity, 3005, blockPos, 0);
                empty = decreasedOxidationState;
            } else if (blockStateOptional.isPresent()) {
                world.playSound(playerEntity, blockPos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
                world.levelEvent(playerEntity, 3004, blockPos, 0);
                empty = blockStateOptional;
            }

            if (empty.isPresent()) {
                if (playerEntity instanceof ServerPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)playerEntity, blockPos, itemStack);
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
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext){
        super.appendHoverText(stack, world, tooltip, tooltipContext);
        CleanlinessHelper.mcdw$tooltipHelper(stack, tooltip, 16);
    }
}
