package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.AbilityHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.enums.DefensiveArtifactID;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class WindHornItem extends ArtifactDefensiveItem{

    private static final String INSTRUMENT_KEY = "instrument";
    private final TagKey<Instrument> instrumentTag;


    public WindHornItem(TagKey<Instrument> instrumentTag) {
        super(
                DefensiveArtifactID.WIND_HORN,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.DEFENSIVE_ARTIFACT_STATS
                        .get(DefensiveArtifactID.WIND_HORN).mcdar$getDurability()
        );
        this.instrumentTag = instrumentTag;
    }

    public InteractionResultHolder<ItemStack> use (Level world, Player user, InteractionHand hand){
        ItemStack itemStack = user.getItemInHand(hand);
        Optional<? extends Holder<Instrument>> optional = this.getInstrument(itemStack);
        if (optional.isPresent()) {
            Instrument instrument = optional.get().value();
            user.startUsingItem(hand);
            WindHornItem.playSound(world, user, instrument);
            for (LivingEntity nearbyEntity : AOEHelper.getEntitiesByPredicate(user, 5,
                    (nearbyEntity) -> nearbyEntity != user && !AbilityHelper.isPetOf(nearbyEntity, user) && nearbyEntity.isAlive())) {
                AOEHelper.knockbackNearbyEnemies(user, nearbyEntity, 2.0F);
            }

            if (!user.isCreative())
                itemStack.hurtAndBreak(1, user, (entity) -> entity.broadcastBreakEvent(hand));

            McdarEnchantmentHelper.mcdar$cooldownHelper(
                    user,
                    this
            );
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);

        }
        return new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }

    private Optional<? extends Holder<Instrument>> getInstrument(ItemStack stack) {
        ResourceLocation identifier;
        CompoundTag nbtCompound = stack.getTag();
        if (nbtCompound != null && nbtCompound.contains(INSTRUMENT_KEY, Tag.TAG_STRING) && (identifier = ResourceLocation.tryParse(nbtCompound.getString(INSTRUMENT_KEY))) != null) {
            return BuiltInRegistries.INSTRUMENT.getHolder(ResourceKey.create(Registries.INSTRUMENT, identifier));
        }
        Iterator<Holder<Instrument>> iterator = BuiltInRegistries.INSTRUMENT.getTagOrEmpty(this.instrumentTag).iterator();
        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        }
        return Optional.empty();
    }

    private static void playSound(Level world, Player player, Instrument instrument) {
        SoundEvent soundEvent = instrument.soundEvent().value();
        float f = instrument.range() / 16.0f;
        world.playSound(player, player, soundEvent, SoundSource.RECORDS, f, 1.0f);
        world.gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), GameEvent.Context.of(player));
    }
}
