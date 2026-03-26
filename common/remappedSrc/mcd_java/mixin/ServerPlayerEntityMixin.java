
package mcd_java.mixin;

import mcd_java.effects.ArmorEffects;
import mcd_java.effects.EnchantmentEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    public void mcda$onArmorsAbilitiesTick(CallbackInfo ci) {
        if(!((Object) this instanceof ServerPlayer playerEntity))
            return;
        if (!playerEntity.isAlive())
            return;

        Level world = playerEntity.getCommandSenderWorld();
        // Effects from Enchantments
        if (world.getGameTime() % 30 == 0) {
            EnchantmentEffects.applyCowardice(playerEntity);
            EnchantmentEffects.applyFrenzied(playerEntity);
            EnchantmentEffects.applyReckless(playerEntity);
        }

        // Effects from Armor Sets every 30 ticks
        if (world.getGameTime() % 30 == 0) {
            ArmorEffects.applyFireResistance(playerEntity); // Sprout & Living Vines Armour
            ArmorEffects.applyHaste(playerEntity); // Cave Crawler (below Y level 32) & Highland (above Y level 100)
            ArmorEffects.applyHeroOfTheVillage(playerEntity); // Hero's Armour & Gilded Glory
            ArmorEffects.applyHungerPain(playerEntity); // Hungry Horror Armour
            ArmorEffects.applyInvisibility(playerEntity); // Thief Armour Sneaking
            ArmorEffects.applyLuck(playerEntity); // Opulent Armour
            ArmorEffects.applySlowFalling(playerEntity); // Phantom and Frost Bite Armour
            ArmorEffects.applySprintingSpeed(playerEntity); // Shadow Walker Armour
            ArmorEffects.applyStalwartBulwarkResistanceEffect(playerEntity); // Stalwart Mail Armour
            ArmorEffects.applyWaterBreathing(playerEntity); // Glow Squid Armour Water Breathing Underwater
        }

        // Effects from Armor Sets on non-standard ticks
        ArmorEffects.applyRenegadesRushEffect(playerEntity); // Renegade Armour
    }

    @Inject(method = "consumeItem", at = @At("HEAD"))
    public void mcda$consumeItem(CallbackInfo ci){

        ServerPlayer playerEntity = (ServerPlayer) (Object) this;

        ArmorEffects.sweetBerrySpeed(playerEntity);
    }
}
