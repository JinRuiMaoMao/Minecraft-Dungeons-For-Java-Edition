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
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Instrument;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

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

    public TypedActionResult<ItemStack> use (World world, PlayerEntity user, Hand hand){
        ItemStack itemStack = user.getItemInHand(hand);
        Optional<? extends RegistryEntry<Instrument>> optional = this.getInstrument(itemStack);
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
            return new TypedActionResult<>(InteractionResult.SUCCESS, itemStack);

        }
        return new TypedActionResult<>(InteractionResult.FAIL, itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }

    private Optional<? extends RegistryEntry<Instrument>> getInstrument(ItemStack stack) {
        Identifier identifier;
        NbtCompound nbtCompound = stack.getTag();
        if (nbtCompound != null && nbtCompound.contains(INSTRUMENT_KEY, Tag.TAG_STRING) && (identifier = Identifier.tryParse(nbtCompound.getString(INSTRUMENT_KEY))) != null) {
            return Registries.INSTRUMENT.getHolder(ResourceKey.create(Registries.INSTRUMENT, identifier));
        }
        Iterator<RegistryEntry<Instrument>> iterator = Registries.INSTRUMENT.getTagOrEmpty(this.instrumentTag).iterator();
        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        }
        return Optional.empty();
    }

    private static void playSound(World world, PlayerEntity player, Instrument instrument) {
        SoundEvent soundEvent = instrument.soundEvent().value();
        float f = instrument.range() / 16.0f;
        world.playSound(PlayerEntity, PlayerEntity, soundEvent, SoundCategory.RECORDS, f, 1.0f);
        world.gameEvent(GameEvent.INSTRUMENT_PLAY, PlayerEntity.position(), GameEvent.Context.of(PlayerEntity));
    }
}
