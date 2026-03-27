package mc_javaedition.forge.placeholder;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PlaceholderArtifactForge extends Item {
    private static final String BEACON_SOULS_KEY = "mcd_beacon_souls";
    private static final String BEACON_LAST_CONSUME_TIME_KEY = "mcd_beacon_last_consume_time";
    private static final int BEACON_MAX_SOULS = 300;
    private static final int BEACON_SOULS_PER_PULSE = 2;
    private static final int BEACON_PULSE_TICKS = 2; // 0.1s
    private static final float BEACON_BASE_DPS = 18.0f;
    private static final int BEACON_COOLDOWN_TICKS = 50; // 2.5s
    private static final int LOVE_MEDALLION_CHARM_TICKS = 20 * 10;
    private static final Map<UUID, Long> CHARMED_UNTIL = new HashMap<>();
    private static final Map<UUID, UUID> CHARMED_OWNER = new HashMap<>();
    private static final Map<UUID, Long> OWNER_PROTECTED_UNTIL = new HashMap<>();
    private static final Map<UUID, Boolean> OWNER_PREV_INVULN = new HashMap<>();

    private final String namespace;
    private final String path;

    public PlaceholderArtifactForge(String namespace, String path) {
        super(new Properties().stacksTo(1).durability(128));
        this.namespace = namespace;
        this.path = path;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(prettyName(namespace, path));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        String id = resolveArtifactKey(path);
        if (isOnCooldown(user)) {
            return InteractionResultHolder.fail(stack);
        }
        if ("corrupted_beacon".equals(id)) {
            int souls = stack.getOrCreateTag().contains(BEACON_SOULS_KEY) ? stack.getOrCreateTag().getInt(BEACON_SOULS_KEY) : BEACON_MAX_SOULS;
            if (souls < BEACON_SOULS_PER_PULSE) {
                return InteractionResultHolder.fail(stack);
            }
            if (!world.isClientSide && !user.getAbilities().instabuild) {
                stack.hurtAndBreak(1, user, p -> p.broadcastBreakEvent(hand));
            }
            user.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        trigger(world, user, hand, stack);
        user.swing(hand);
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (slotChanged) {
            return true;
        }
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player user = context.getPlayer();
        if (user == null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        if (isOnCooldown(user)) {
            return InteractionResult.FAIL;
        }
        trigger(context.getLevel(), user, context.getHand(), stack);
        user.swing(context.getHand());
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (world.isClientSide || !(entity instanceof Player user)) {
            return;
        }
        tickLoveMedallionCharmed(world, user);
        if (!"corrupted_beacon".equals(resolveArtifactKey(path))) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int souls = tag.contains(BEACON_SOULS_KEY) ? tag.getInt(BEACON_SOULS_KEY) : BEACON_MAX_SOULS;
        long now = world.getGameTime();
        long lastConsume = tag.contains(BEACON_LAST_CONSUME_TIME_KEY) ? tag.getLong(BEACON_LAST_CONSUME_TIME_KEY) : now;
        if (souls < BEACON_MAX_SOULS && (now - lastConsume) >= 100L) {
            tag.putInt(BEACON_SOULS_KEY, BEACON_MAX_SOULS);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        if ("corrupted_beacon".equals(resolveArtifactKey(path))) {
            return 72000;
        }
        return super.getUseDuration(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if ("corrupted_beacon".equals(resolveArtifactKey(path))) {
            return UseAnim.NONE;
        }
        return super.getUseAnimation(stack);
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(world, user, stack, remainingUseDuration);
        if (world.isClientSide || !(user instanceof Player player)) {
            return;
        }
        if (!"corrupted_beacon".equals(resolveArtifactKey(path))) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        int souls = tag.contains(BEACON_SOULS_KEY) ? tag.getInt(BEACON_SOULS_KEY) : BEACON_MAX_SOULS;
        int usedTicks = this.getUseDuration(stack) - remainingUseDuration;
        if (usedTicks <= 0) {
            return;
        }
        boolean pulseNow = (usedTicks == 1) || (usedTicks % BEACON_PULSE_TICKS == 0);
        if (!pulseNow) return;
        if (souls < BEACON_SOULS_PER_PULSE) {
            player.stopUsingItem();
            player.getCooldowns().addCooldown(this, BEACON_COOLDOWN_TICKS);
            return;
        }
        souls -= BEACON_SOULS_PER_PULSE;
        tag.putInt(BEACON_SOULS_KEY, souls);
        tag.putLong(BEACON_LAST_CONSUME_TIME_KEY, world.getGameTime());
        float pulseDamage = BEACON_BASE_DPS * 0.1f;
        for (LivingEntity living : beamTargets(world, player, 40.0d, 1.1d)) {
            living.hurt(player.damageSources().magic(), pulseDamage);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int timeLeft) {
        super.releaseUsing(stack, world, user, timeLeft);
        if (world.isClientSide || !(user instanceof Player player)) {
            return;
        }
        if (!"corrupted_beacon".equals(resolveArtifactKey(path))) {
            return;
        }
        int usedTicks = this.getUseDuration(stack) - timeLeft;
        if (usedTicks > 0) {
            player.getCooldowns().addCooldown(this, BEACON_COOLDOWN_TICKS);
        }
    }

    private boolean isOnCooldown(Player user) {
        return user.getCooldowns().isOnCooldown(this);
    }

    private void trigger(Level world, Player user, InteractionHand hand, ItemStack stack) {
        if (!world.isClientSide) {
            String id = resolveArtifactKey(path);
            boolean applied = activate(world, user, id);
            if (!applied) {
                return;
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.2f);
            if (!user.getAbilities().instabuild) {
                stack.hurtAndBreak(1, user, p -> p.broadcastBreakEvent(hand));
            }
            user.getCooldowns().addCooldown(this, cooldownFor(id));
        }
    }

    private boolean activate(Level world, Player user, String id) {
        switch (id) {
            case "soul_healer" -> {
                user.heal(6.0f);
                return true;
            }
            case "boots_of_swiftness" -> {
                // MC Dungeons target: short burst, roughly 2x move speed.
                user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 8, 4));
                return true;
            }
            case "death_cap_mushroom" -> {
                user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 8, 1));
                user.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 8, 1));
                return true;
            }
            case "iron_hide_amulet" -> {
                user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 10, 1));
                return true;
            }
            case "totem_of_regeneration" -> {
                user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 10, 1));
                return true;
            }
            case "totem_of_shielding" -> {
                user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 8, 0));
                user.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 8, 1));
                return true;
            }
            case "totem_of_soul_protection" -> {
                user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 8, 1));
                user.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 8, 0));
                return true;
            }
            case "ghost_cloak" -> {
                user.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 6, 0));
                user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 0));
                return true;
            }
            case "light_feather" -> {
                Vec3 v = user.getDeltaMovement();
                user.setDeltaMovement(v.x, Math.max(v.y, 0.65d), v.z);
                user.hurtMarked = true;
                user.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 4, 0));
                return true;
            }
            case "updraft_tome" -> {
                int affected = 0;
                for (LivingEntity living : nearbyHostiles(world, user, 7.0d)) {
                    if (affected >= 7) break;
                    Vec3 lv = living.getDeltaMovement();
                    living.setDeltaMovement(lv.x, Math.max(lv.y, 0.9d), lv.z);
                    living.hurtMarked = true;
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 3, 4));
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 3, 0));
                    living.hurt(user.damageSources().magic(), 6.0f);
                    affected++;
                }
                return affected > 0;
            }
            case "wind_horn" -> {
                for (LivingEntity living : nearbyHostiles(world, user, 6.5d)) {
                    Vec3 dir = living.position().subtract(user.position()).normalize();
                    living.knockback(1.8f, -dir.x, -dir.z);
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 2, 1));
                }
                return true;
            }
            case "gong_of_weakening" -> {
                List<LivingEntity> targets = nearbyAffectableMobs(world, user, 7.0d);
                targets.forEach(l -> {
                    l.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 8, 2));
                    l.hurt(user.damageSources().magic(), 3.0f);
                });
                return !targets.isEmpty();
            }
            case "shock_powder" -> {
                List<LivingEntity> targets = nearbyAffectableMobs(world, user, 6.5d);
                targets.forEach(l -> {
                    applyStunned(l, 20);
                });
                return !targets.isEmpty();
            }
            case "harvester" -> {
                List<LivingEntity> targets = nearbyHostiles(world, user, 6.0d);
                targets.forEach(l -> l.hurt(user.damageSources().magic(), 10.0f));
                return !targets.isEmpty();
            }
            case "lightning_rod" -> {
                strikeLightningRod(world, user);
                return true;
            }
            case "scatter_mines" -> {
                triggerScatterMines(world, user);
                return true;
            }
            case "spinblade" -> {
                return triggerSpinblade(world, user);
            }
            case "eye_of_the_guardian" -> {
                return triggerEyeOfGuardian(world, user);
            }
            case "corrupted_pumpkin" -> {
                return triggerCorruptedPumpkin(world, user);
            }
            case "satchel_of_elements" -> {
                List<LivingEntity> targets = nearbyAffectableMobs(world, user, 7.0d);
                int mode = ThreadLocalRandom.current().nextInt(3); // 0 burn, 1 frozen, 2 lightning
                int cap = 0;
                for (LivingEntity l : targets) {
                    if (cap >= 7) break;
                    if (mode == 0) {
                        l.setSecondsOnFire(4);
                    } else if (mode == 1) {
                        applyFrozen(l, 20 * 3);
                    } else {
                        strikeLightningAt(world, user, l.position());
                        l.hurt(user.damageSources().magic(), 8.0f);
                    }
                    cap++;
                }
                return cap > 0;
            }
            case "fishing_rod" -> {
                LivingEntity target = nearestAffectableMob(world, user, 12.0d);
                if (target == null) return false;
                Vec3 pull = user.position().subtract(target.position()).normalize().scale(1.2d);
                target.setDeltaMovement(pull.x, 0.35d, pull.z);
                target.hurtMarked = true;
                applyStunned(target, 20);
                return true;
            }
            case "ice_wand" -> {
                int affected = 0;
                for (LivingEntity l : nearbyAffectableMobs(world, user, 8.0d)) {
                    if (affected >= 7) break;
                    applyStunned(l, 20 * 2);
                    applyFrozen(l, 20 * 2);
                    l.hurt(user.damageSources().magic(), 5.0f);
                    affected++;
                }
                return affected > 0;
            }
            case "blast_fungus" -> {
                triggerBlastFungus(world, user);
                return true;
            }
            case "powershaker" -> {
                world.explode(user, user.getX(), user.getY(), user.getZ(), 2.2f, Level.ExplosionInteraction.NONE);
                return true;
            }
            case "flaming_quiver", "torment_quiver", "thundering_quiver", "harpoon_quiver" -> {
                user.addItem(new ItemStack(Items.ARROW, 8));
                return true;
            }
            case "satchel_of_elixirs" -> {
                int pick = ThreadLocalRandom.current().nextInt(4);
                if (pick == 0) user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 8, 1));
                if (pick == 1) user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 6, 1));
                if (pick == 2) user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 0));
                if (pick == 3) user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 0));
                return true;
            }
            case "wonderful_wheat" -> {
                user.heal(4.0f);
                user.addEffect(new MobEffectInstance(MobEffects.SATURATION, 20 * 2, 0));
                return true;
            }
            case "tasty_bone" -> {
                spawnTamedWolf(world, user);
                return true;
            }
            case "golem_kit" -> {
                spawnEntity(world, EntityType.IRON_GOLEM, user);
                return true;
            }
            case "buzzy_nest" -> {
                spawnEntity(world, EntityType.BEE, user);
                spawnEntity(world, EntityType.BEE, user);
                return true;
            }
            case "enchanted_grass" -> {
                spawnEntity(world, EntityType.SHEEP, user);
                return true;
            }
            case "love_medallion" -> {
                int affected = 0;
                OWNER_PROTECTED_UNTIL.put(user.getUUID(), world.getGameTime() + LOVE_MEDALLION_CHARM_TICKS);
                for (LivingEntity l : nearbyAffectableMobs(world, user, 8.0d)) {
                    if (affected >= 3) break;
                    spawnCharmHearts(world, l);
                    l.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 10, 0));
                    l.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 10, 1));
                    // Keep "ally" behavior for 10s, then naturally return to normal.
                    l.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 10, 0));
                    if (l instanceof Monster hostile) {
                        markCharmed(hostile, user, world.getGameTime() + LOVE_MEDALLION_CHARM_TICKS);
                        hostile.setTarget(null);
                        LivingEntity redirected = nearestOtherMonster(world, user, hostile, 12.0d);
                        if (redirected instanceof Monster other) {
                            hostile.setTarget(other);
                        }
                    }
                    affected++;
                }
                return affected > 0;
            }
            case "corrupted_seeds" -> {
                int affected = 0;
                for (LivingEntity l : nearbyAffectableMobs(world, user, 8.0d)) {
                    if (affected >= 8) break;
                    applyStunned(l, 20);
                    applyPoisoned(l, 20 * 5);
                    affected++;
                }
                return affected > 0;
            }
            case "enchanters_tome" -> {
                user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 8, 0));
                user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 8, 0));
                return true;
            }
            default -> {
                user.addEffect(new MobEffectInstance(MobEffects.LUCK, 20 * 8, 0));
                return true;
            }
        }
    }

    private static int cooldownFor(String idPath) {
        if (idPath.contains("quiver")) return 30;
        if (idPath.equals("corrupted_beacon")) return 50; // 2.5s
        if (idPath.equals("corrupted_pumpkin")) return 50; // 2.5s
        if (idPath.equals("lightning_rod")) return 80; // 4s
        if (idPath.equals("eye_of_the_guardian")) return 440; // 22s
        if (idPath.equals("spinblade")) return 100; // 5s
        if (idPath.equals("boots_of_swiftness")) return 100; // 5s
        if (idPath.equals("harvester")) return 160; // 8s
        if (idPath.equals("blast_fungus")) return 120; // 6s
        if (idPath.equals("scatter_mines")) return 240; // 12s
        if (idPath.equals("updraft_tome")) return 240; // 12s
        if (idPath.equals("corrupted_seeds")) return 200; // 10s
        if (idPath.equals("fishing_rod")) return 30; // 1.5s
        if (idPath.equals("gong_of_weakening")) return 400; // 20s
        if (idPath.equals("ice_wand")) return 300; // 15s
        if (idPath.equals("love_medallion")) return 600; // 30s
        if (idPath.equals("satchel_of_elements")) return 200; // 10s
        if (idPath.equals("shock_powder")) return 300; // 15s
        if (idPath.contains("totem")) return 120;
        if (idPath.equals("light_feather")) return 20;
        if (idPath.equals("soul_healer")) return 60;
        if (idPath.equals("golem_kit") || idPath.equals("tasty_bone") || idPath.equals("buzzy_nest")) return 140;
        return 80;
    }

    private static String canonicalArtifactId(String idPath) {
        String s = idPath.toLowerCase(java.util.Locale.ROOT);
        if (s.startsWith("artifact_")) s = s.substring("artifact_".length());
        if (s.startsWith("mcdar_")) s = s.substring("mcdar_".length());
        if (s.startsWith("mcd_java_")) s = s.substring("mcd_java_".length());
        if (s.startsWith("mcda_")) s = s.substring("mcda_".length());
        s = s.replace('-', '_');
        return s;
    }

    private static String resolveArtifactKey(String raw) {
        String s = canonicalArtifactId(raw);
        if (s.contains("boots") && s.contains("swiftness")) return "boots_of_swiftness";
        if (s.contains("death") && s.contains("mushroom")) return "death_cap_mushroom";
        if (s.contains("ghost") && s.contains("cloak")) return "ghost_cloak";
        if (s.contains("light") && s.contains("feather")) return "light_feather";
        if (s.contains("blast") && s.contains("fungus")) return "blast_fungus";
        if (s.contains("lightning") && s.contains("rod")) return "lightning_rod";
        if (s.contains("updraft") && s.contains("tome")) return "updraft_tome";
        if (s.contains("enchanter") && s.contains("tome")) return "enchanters_tome";
        if (s.contains("iron") && s.contains("amulet")) return "iron_hide_amulet";
        if (s.contains("soul") && s.contains("healer")) return "soul_healer";
        if (s.contains("totem") && s.contains("regeneration")) return "totem_of_regeneration";
        if (s.contains("totem") && s.contains("shield")) return "totem_of_shielding";
        if (s.contains("totem") && s.contains("soul") && s.contains("protection")) return "totem_of_soul_protection";
        if (s.contains("wind") && s.contains("horn")) return "wind_horn";
        if (s.contains("flaming") && s.contains("quiver")) return "flaming_quiver";
        if (s.contains("harpoon") && s.contains("quiver")) return "harpoon_quiver";
        if (s.contains("thundering") && s.contains("quiver")) return "thundering_quiver";
        if (s.contains("torment") && s.contains("quiver")) return "torment_quiver";
        if (s.contains("corrupted") && s.contains("seeds")) return "corrupted_seeds";
        if (s.contains("gong") && s.contains("weak")) return "gong_of_weakening";
        if (s.contains("love") && s.contains("medallion")) return "love_medallion";
        if (s.contains("satchel") && s.contains("element")) return "satchel_of_elements";
        if (s.contains("satchel") && s.contains("elixir")) return "satchel_of_elixirs";
        if (s.contains("shock") && s.contains("powder")) return "shock_powder";
        if (s.contains("fishing") && s.contains("rod")) return "fishing_rod";
        if (s.contains("ice") && s.contains("wand")) return "ice_wand";
        if (s.contains("buzzy") && s.contains("nest")) return "buzzy_nest";
        if (s.contains("corrupted") && s.contains("beacon")) return "corrupted_beacon";
        if (s.contains("corrupted") && s.contains("pumpkin")) return "corrupted_pumpkin";
        if (s.contains("eye") && s.contains("guardian")) return "eye_of_the_guardian";
        if (s.contains("scatter") && s.contains("mine")) return "scatter_mines";
        if (s.contains("spinblade")) return "spinblade";
        if (s.contains("enchanted") && s.contains("grass")) return "enchanted_grass";
        if (s.contains("golem") && s.contains("kit")) return "golem_kit";
        if (s.contains("tasty") && s.contains("bone")) return "tasty_bone";
        if (s.contains("wonderful") && s.contains("wheat")) return "wonderful_wheat";
        if (s.contains("harvester")) return "harvester";
        if (s.contains("powershaker")) return "powershaker";
        return s;
    }

    private static List<LivingEntity> nearbyHostiles(Level world, Player user, double radius) {
        AABB aoe = user.getBoundingBox().inflate(radius);
        return world.getEntitiesOfClass(LivingEntity.class, aoe, living -> living instanceof Monster);
    }

    private static LivingEntity nearestHostile(Level world, Player user, double radius) {
        List<LivingEntity> list = nearbyHostiles(world, user, radius);
        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (LivingEntity l : list) {
            double d = l.distanceToSqr(user);
            if (d < bestD) {
                bestD = d;
                best = l;
            }
        }
        return best;
    }

    private static List<LivingEntity> nearbyAffectableMobs(Level world, Player user, double radius) {
        AABB aoe = user.getBoundingBox().inflate(radius);
        return world.getEntitiesOfClass(LivingEntity.class, aoe, living -> living != user);
    }

    private static LivingEntity nearestAffectableMob(Level world, Player user, double radius) {
        List<LivingEntity> list = nearbyAffectableMobs(world, user, radius);
        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (LivingEntity l : list) {
            double d = l.distanceToSqr(user);
            if (d < bestD) {
                bestD = d;
                best = l;
            }
        }
        return best;
    }

    private static LivingEntity nearestOtherMonster(Level world, Player user, LivingEntity self, double radius) {
        AABB aoe = self.getBoundingBox().inflate(radius);
        List<LivingEntity> list = world.getEntitiesOfClass(LivingEntity.class, aoe, living ->
                living instanceof Monster && living != self && living != user);
        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (LivingEntity l : list) {
            double d = l.distanceToSqr(self);
            if (d < bestD) {
                bestD = d;
                best = l;
            }
        }
        return best;
    }

    private static void spawnCharmHearts(Level world, LivingEntity target) {
        if (!(world instanceof ServerLevel sw)) {
            return;
        }
        sw.sendParticles(
                ParticleTypes.HEART,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.6d,
                target.getZ(),
                8,
                0.35d,
                0.35d,
                0.35d,
                0.02d
        );
    }

    private static void markCharmed(Monster target, Player owner, long untilTick) {
        CHARMED_UNTIL.put(target.getUUID(), untilTick);
        CHARMED_OWNER.put(target.getUUID(), owner.getUUID());
    }

    private static void tickLoveMedallionCharmed(Level world, Player ticker) {
        if (!(world instanceof ServerLevel sw)) return;
        long now = world.getGameTime();
        for (UUID mobId : new ArrayList<>(CHARMED_UNTIL.keySet())) {
            Entity e = sw.getEntity(mobId);
            if (!(e instanceof Monster hostile)) {
                CHARMED_UNTIL.remove(mobId);
                CHARMED_OWNER.remove(mobId);
                continue;
            }
            Long until = CHARMED_UNTIL.get(mobId);
            if (until == null) continue;
            if (now >= until || !hostile.isAlive()) {
                // User-requested behavior: after 10s charm duration, charmed mob is killed.
                hostile.kill();
                CHARMED_UNTIL.remove(mobId);
                CHARMED_OWNER.remove(mobId);
                continue;
            }
            UUID ownerId = CHARMED_OWNER.get(mobId);
            Player ownerPlayer = null;
            if (ownerId != null) {
                Entity ownerEntity = sw.getEntity(ownerId);
                if (ownerEntity instanceof Player p) ownerPlayer = p;
            }
            if (ownerPlayer != null) {
                clearHostilityTowardOwner(hostile, ownerPlayer);
            }
            LivingEntity redirected = nearestEnemyForCharmed(world, hostile, ownerId, 20.0d);
            if (redirected instanceof Monster other) {
                hostile.setTarget(other);
                if (hostile.distanceToSqr(other) <= 6.25d) {
                    hostile.doHurtTarget(other);
                } else {
                    Vec3 chase = other.position().subtract(hostile.position());
                    if (chase.lengthSqr() > 1.0E-4d) {
                        Vec3 step = chase.normalize().scale(0.18d);
                        hostile.setDeltaMovement(step.x, Math.max(hostile.getDeltaMovement().y, 0.0d), step.z);
                        hostile.hurtMarked = true;
                    }
                }
                if (other.getTarget() == null || other.getTarget() instanceof Player || isCharmedActive(other.getUUID(), now)) {
                    other.setTarget(hostile);
                }
            } else {
                hostile.setTarget(null);
                if (ownerPlayer != null && hostile.distanceToSqr(ownerPlayer) < 9.0d) {
                    Vec3 away = hostile.position().subtract(ownerPlayer.position());
                    if (away.lengthSqr() > 1.0E-4d) {
                        Vec3 push = away.normalize().scale(0.35d);
                        hostile.setDeltaMovement(push.x, Math.max(hostile.getDeltaMovement().y, 0.08d), push.z);
                        hostile.hurtMarked = true;
                    }
                }
            }
        }
        redirectNearbyUncharmedAggro(world, ticker, now);
        enforceOwnerSafetyDuringCharm(world, ticker, now);
    }

    private static LivingEntity nearestEnemyForCharmed(Level world, Monster self, UUID ownerId, double radius) {
        AABB aoe = self.getBoundingBox().inflate(radius);
        long now = world.getGameTime();
        List<LivingEntity> list = world.getEntitiesOfClass(LivingEntity.class, aoe, living ->
                living instanceof Monster
                        && living != self
                        && !isCharmedActive(living.getUUID(), now)
                        && !isSameOwnerPlayer(living, ownerId));
        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (LivingEntity l : list) {
            double d = l.distanceToSqr(self);
            if (d < bestD) {
                bestD = d;
                best = l;
            }
        }
        return best;
    }

    private static boolean isCharmedActive(UUID entityId, long now) {
        Long until = CHARMED_UNTIL.get(entityId);
        return until != null && now < until;
    }

    private static void redirectNearbyUncharmedAggro(Level world, Player owner, long now) {
        List<Monster> nearby = world.getEntitiesOfClass(Monster.class, owner.getBoundingBox().inflate(32.0d), e -> true);
        for (Monster uncharmed : nearby) {
            if (isCharmedActive(uncharmed.getUUID(), now)) continue;
            if (!(uncharmed.getTarget() instanceof Player)) continue;
            Monster charmed = nearestActiveCharmed(world, uncharmed, now, 20.0d);
            if (charmed != null) {
                uncharmed.setTarget(charmed);
                if (charmed.getTarget() == null || charmed.getTarget() instanceof Player) {
                    charmed.setTarget(uncharmed);
                }
            } else {
                // During charm window, uncharmed mobs should not keep aggro on player.
                uncharmed.setTarget(null);
            }
        }
    }

    private static Monster nearestActiveCharmed(Level world, Monster from, long now, double radius) {
        AABB aoe = from.getBoundingBox().inflate(radius);
        List<Monster> list = world.getEntitiesOfClass(Monster.class, aoe, e ->
                e != from && isCharmedActive(e.getUUID(), now));
        Monster best = null;
        double bestD = Double.MAX_VALUE;
        for (Monster m : list) {
            if (!m.isAlive()) continue;
            double d = m.distanceToSqr(from);
            if (d < bestD) {
                bestD = d;
                best = m;
            }
        }
        return best;
    }

    private static void enforceOwnerSafetyDuringCharm(Level world, Player owner, long now) {
        boolean active = isOwnerProtectionActive(owner.getUUID(), now) || hasActiveCharmForOwner(owner.getUUID(), now);
        applyOwnerInvulnerability(owner, active);
        if (!active) {
            return;
        }
        List<Monster> nearby = world.getEntitiesOfClass(Monster.class, owner.getBoundingBox().inflate(18.0d), e -> true);
        for (Monster hostile : nearby) {
            if (hostile.getTarget() == owner) {
                hostile.setTarget(null);
            }
            clearHostilityTowardOwner(hostile, owner);
            if (hostile.distanceToSqr(owner) < 12.25d) {
                Vec3 away = hostile.position().subtract(owner.position());
                if (away.lengthSqr() > 1.0E-4d) {
                    Vec3 push = away.normalize().scale(0.28d);
                    hostile.setDeltaMovement(push.x, Math.max(hostile.getDeltaMovement().y, 0.10d), push.z);
                    hostile.hurtMarked = true;
                }
            }
        }
    }

    private static boolean hasActiveCharmForOwner(UUID ownerId, long now) {
        for (Map.Entry<UUID, UUID> entry : CHARMED_OWNER.entrySet()) {
            if (!ownerId.equals(entry.getValue())) continue;
            Long until = CHARMED_UNTIL.get(entry.getKey());
            if (until != null && now < until) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLoveOwnerProtected(Player owner, long now) {
        return isOwnerProtectionActive(owner.getUUID(), now) || hasActiveCharmForOwner(owner.getUUID(), now);
    }

    private static boolean isOwnerProtectionActive(UUID ownerId, long now) {
        Long until = OWNER_PROTECTED_UNTIL.get(ownerId);
        if (until == null) return false;
        if (now >= until) {
            OWNER_PROTECTED_UNTIL.remove(ownerId);
            return false;
        }
        return true;
    }

    private static void applyOwnerInvulnerability(Player owner, boolean active) {
        UUID ownerId = owner.getUUID();
        if (active) {
            if (!OWNER_PREV_INVULN.containsKey(ownerId)) {
                OWNER_PREV_INVULN.put(ownerId, owner.getAbilities().invulnerable);
            }
            if (!owner.getAbilities().invulnerable) {
                owner.getAbilities().invulnerable = true;
                if (owner instanceof ServerPlayer sp) {
                    sp.onUpdateAbilities();
                }
            }
            return;
        }
        Boolean previous = OWNER_PREV_INVULN.remove(ownerId);
        if (previous != null && owner.getAbilities().invulnerable != previous) {
            owner.getAbilities().invulnerable = previous;
            if (owner instanceof ServerPlayer sp) {
                sp.onUpdateAbilities();
            }
        }
    }

    private static boolean isSameOwnerPlayer(LivingEntity living, UUID ownerId) {
        if (!(living instanceof Player player) || ownerId == null) return false;
        return ownerId.equals(player.getUUID());
    }

    private static void clearHostilityTowardOwner(Monster hostile, Player ownerPlayer) {
        if (hostile.getTarget() == ownerPlayer) {
            hostile.setTarget(null);
        }
        hostile.setLastHurtByMob(null);
        if (hostile instanceof NeutralMob neutral) {
            neutral.setPersistentAngerTarget(null);
            neutral.setRemainingPersistentAngerTime(0);
        }
    }

    private static void applyStunned(LivingEntity target, int durationTicks) {
        // "Stunned" is modeled as near-immobilize + weakened offense.
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, durationTicks, 10));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, durationTicks, 2));
    }

    private static void applyPoisoned(LivingEntity target, int durationTicks) {
        // "Poisoned" here means stronger poison (剧毒).
        target.addEffect(new MobEffectInstance(MobEffects.POISON, durationTicks, 1));
    }

    private static void applyFrozen(LivingEntity target, int durationTicks) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, durationTicks, 6));
    }

    private static List<LivingEntity> beamTargets(Level world, Player user, double range, double radius) {
        Vec3 start = beaconStart(user);
        Vec3 end = start.add(user.getLookAngle().scale(range));
        AABB box = new AABB(start, end).inflate(radius);
        return world.getEntitiesOfClass(LivingEntity.class, box, living -> {
            if (!(living instanceof Monster)) return false;
            Vec3 p = living.position().add(0.0d, living.getBbHeight() * 0.5d, 0.0d);
            return pointToSegmentDistanceSq(p, start, end) <= radius * radius
                    && world.clip(new net.minecraft.world.level.ClipContext(
                    start, p,
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    user
            )).getType() == net.minecraft.world.phys.HitResult.Type.MISS;
        });
    }

    private static void strikeLightningRod(Level world, Player user) {
        Vec3 center = resolveTargetPoint(world, user, 24.0d);
        AABB aoe = new AABB(
                center.x - 2.5d, center.y - 2.5d, center.z - 2.5d,
                center.x + 2.5d, center.y + 2.5d, center.z + 2.5d
        );
        for (LivingEntity living : world.getEntitiesOfClass(LivingEntity.class, aoe, e -> e instanceof Monster)) {
            living.hurt(user.damageSources().magic(), 14.0f);
        }
        var bolt = EntityType.LIGHTNING_BOLT.create(world);
        if (bolt != null) {
            bolt.moveTo(center.x, center.y, center.z, 0.0f, 0.0f);
            world.addFreshEntity(bolt);
        }
    }

    private static void strikeLightningAt(Level world, Player user, Vec3 center) {
        var bolt = EntityType.LIGHTNING_BOLT.create(world);
        if (bolt != null) {
            bolt.moveTo(center.x, center.y, center.z, 0.0f, 0.0f);
            world.addFreshEntity(bolt);
        }
    }

    private static Vec3 resolveTargetPoint(Level world, Player user, double range) {
        Vec3 start = user.getEyePosition();
        Vec3 end = start.add(user.getLookAngle().scale(range));
        HitResult hit = world.clip(new ClipContext(
                start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                user
        ));
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bhr = (BlockHitResult) hit;
            return bhr.getLocation();
        }
        return end;
    }

    private static void triggerScatterMines(Level world, Player user) {
        Vec3 forward = user.getLookAngle().normalize();
        Vec3 flatForward = new Vec3(forward.x, 0.0d, forward.z).normalize();
        if (flatForward.lengthSqr() < 1.0E-6d) flatForward = new Vec3(0.0d, 0.0d, 1.0d);
        Vec3 left = new Vec3(-flatForward.z, 0.0d, flatForward.x);
        Vec3 center = user.position().add(flatForward.scale(1.7d));
        Vec3[] points = new Vec3[]{
                center,
                center.add(left.scale(1.2d)),
                center.subtract(left.scale(1.2d))
        };
        for (Vec3 p : points) {
            world.explode(user, p.x, p.y, p.z, 1.9f, Level.ExplosionInteraction.NONE);
        }
    }

    private static void triggerBlastFungus(Level world, Player user) {
        Vec3 forward = user.getLookAngle().normalize();
        Vec3 flatForward = new Vec3(forward.x, 0.0d, forward.z).normalize();
        if (flatForward.lengthSqr() < 1.0E-6d) flatForward = new Vec3(0.0d, 0.0d, 1.0d);
        Vec3 left = new Vec3(-flatForward.z, 0.0d, flatForward.x);
        Vec3 center = user.position().add(flatForward.scale(1.6d));
        Vec3[] points = new Vec3[]{
                center,
                center.add(left.scale(1.6d)),
                center.subtract(left.scale(1.6d)),
                center.add(flatForward.scale(1.8d)),
                center.add(flatForward.scale(0.8d)).add(left.scale(0.8d))
        };
        for (Vec3 p : points) {
            world.explode(user, p.x, p.y, p.z, 1.8f, Level.ExplosionInteraction.NONE);
        }
    }

    private static boolean triggerSpinblade(Level world, Player user) {
        Vec3 start = user.getEyePosition();
        Vec3 mid = start.add(user.getLookAngle().normalize().scale(12.0d));
        int hits = 0;
        hits += damageAlongSegment(world, user, start, mid, 1.0d, 8.0f); // outgoing
        hits += damageAlongSegment(world, user, mid, start, 1.0d, 8.0f); // returning
        return hits > 0;
    }

    private static boolean triggerEyeOfGuardian(Level world, Player user) {
        int hits = 0;
        for (LivingEntity living : beamTargets(world, user, 40.0d, 1.2d)) {
            living.hurt(user.damageSources().magic(), 12.0f);
            hits++;
        }
        // Simulate limited turning speed while beam is active.
        user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 4, 2));
        return hits > 0;
    }

    private static boolean triggerCorruptedPumpkin(Level world, Player user) {
        int hits = 0;
        for (LivingEntity living : beamTargets(world, user, 40.0d, 1.3d)) {
            living.hurt(user.damageSources().magic(), 10.0f);
            hits++;
        }
        return hits > 0;
    }

    private static double pointToSegmentDistanceSq(Vec3 p, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double abLen2 = ab.lengthSqr();
        if (abLen2 <= 1.0E-7d) return p.distanceToSqr(a);
        double t = p.subtract(a).dot(ab) / abLen2;
        t = Math.max(0.0d, Math.min(1.0d, t));
        Vec3 proj = a.add(ab.scale(t));
        return p.distanceToSqr(proj);
    }

    private static int damageAlongSegment(Level world, Player user, Vec3 from, Vec3 to, double radius, float damage) {
        AABB box = new AABB(from, to).inflate(radius);
        int hits = 0;
        for (LivingEntity living : world.getEntitiesOfClass(LivingEntity.class, box, e -> e instanceof Monster)) {
            Vec3 p = living.position().add(0.0d, living.getBbHeight() * 0.5d, 0.0d);
            if (pointToSegmentDistanceSq(p, from, to) <= radius * radius) {
                living.hurt(user.damageSources().magic(), damage);
                hits++;
            }
        }
        return hits;
    }

    private static Vec3 beaconStart(Player user) {
        // Slightly below eye line so the beam does not block crosshair.
        return user.getEyePosition().add(0.0d, -0.35d, 0.0d);
    }


    private static void spawnTamedWolf(Level world, Player user) {
        Wolf wolf = EntityType.WOLF.create(world);
        if (wolf == null) return;
        wolf.moveTo(user.getX(), user.getY(), user.getZ(), user.getYRot(), 0.0f);
        wolf.tame(user);
        world.addFreshEntity(wolf);
    }

    private static <T extends Entity> void spawnEntity(Level world, EntityType<T> type, Player user) {
        T e = type.create(world);
        if (e == null) return;
        e.moveTo(user.getX(), user.getY(), user.getZ(), user.getYRot(), 0.0f);
        world.addFreshEntity(e);
    }

    private static String prettyName(String namespace, String path) {
        String base = path.replace('/', ' ').replace('_', ' ').trim();
        StringBuilder sb = new StringBuilder();
        boolean cap = true;
        for (int i = 0; i < base.length(); i++) {
            char c = base.charAt(i);
            if (Character.isWhitespace(c)) {
                sb.append(' ');
                cap = true;
                continue;
            }
            if (cap) {
                sb.append(Character.toUpperCase(c));
                cap = false;
            } else {
                sb.append(c);
            }
        }
        return sb + " (" + namespace + ")";
    }
}
