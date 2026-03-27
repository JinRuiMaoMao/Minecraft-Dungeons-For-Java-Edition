package mc_javaedition.fabric.placeholder;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.UseAction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PlaceholderArtifactFabric extends Item {
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

    public PlaceholderArtifactFabric(String namespace, String path) {
        super(new Settings().maxCount(1).maxDamage(128));
        this.namespace = namespace;
        this.path = path;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.literal(prettyName(namespace, path));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        String id = resolveArtifactKey(path);
        if (isOnCooldown(user)) {
            return TypedActionResult.fail(stack);
        }
        if ("corrupted_beacon".equals(id)) {
            int souls = stack.getOrCreateNbt().contains(BEACON_SOULS_KEY) ? stack.getOrCreateNbt().getInt(BEACON_SOULS_KEY) : BEACON_MAX_SOULS;
            if (souls < BEACON_SOULS_PER_PULSE) {
                return TypedActionResult.fail(stack);
            }
            if (!world.isClient() && !user.isCreative()) {
                stack.damage(1, user, e -> e.sendToolBreakStatus(hand));
            }
            user.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }
        trigger(world, user, hand, stack);
        user.swingHand(hand);
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean allowContinuingBlockBreaking(PlayerEntity player, ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() == newStack.getItem();
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity user = context.getPlayer();
        if (user == null) {
            return ActionResult.PASS;
        }
        ItemStack stack = context.getStack();
        if (isOnCooldown(user)) {
            return ActionResult.FAIL;
        }
        trigger(context.getWorld(), user, context.getHand(), stack);
        user.swingHand(context.getHand());
        return ActionResult.success(context.getWorld().isClient());
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (world.isClient() || !(entity instanceof PlayerEntity user)) {
            return;
        }
        tickLoveMedallionCharmed(world, user);
        if (!"corrupted_beacon".equals(resolveArtifactKey(path))) {
            return;
        }

        NbtCompound nbt = stack.getOrCreateNbt();
        int souls = nbt.contains(BEACON_SOULS_KEY) ? nbt.getInt(BEACON_SOULS_KEY) : BEACON_MAX_SOULS;
        long now = world.getTime();
        long lastConsume = nbt.contains(BEACON_LAST_CONSUME_TIME_KEY) ? nbt.getLong(BEACON_LAST_CONSUME_TIME_KEY) : now;
        if (souls < BEACON_MAX_SOULS && (now - lastConsume) >= 100L) {
            nbt.putInt(BEACON_SOULS_KEY, BEACON_MAX_SOULS);
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        if ("corrupted_beacon".equals(resolveArtifactKey(path))) {
            return 72000;
        }
        return super.getMaxUseTime(stack);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        if ("corrupted_beacon".equals(resolveArtifactKey(path))) {
            return UseAction.NONE;
        }
        return super.getUseAction(stack);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);
        if (world.isClient() || !(user instanceof PlayerEntity player)) {
            return;
        }
        if (!"corrupted_beacon".equals(resolveArtifactKey(path))) {
            return;
        }
        NbtCompound nbt = stack.getOrCreateNbt();
        int souls = nbt.contains(BEACON_SOULS_KEY) ? nbt.getInt(BEACON_SOULS_KEY) : BEACON_MAX_SOULS;
        int usedTicks = this.getMaxUseTime(stack) - remainingUseTicks;
        if (usedTicks <= 0) {
            return;
        }
        boolean pulseNow = (usedTicks == 1) || (usedTicks % BEACON_PULSE_TICKS == 0);
        if (!pulseNow) return;
        if (souls < BEACON_SOULS_PER_PULSE) {
            player.stopUsingItem();
            player.getItemCooldownManager().set(this, BEACON_COOLDOWN_TICKS);
            return;
        }
        souls -= BEACON_SOULS_PER_PULSE;
        nbt.putInt(BEACON_SOULS_KEY, souls);
        nbt.putLong(BEACON_LAST_CONSUME_TIME_KEY, world.getTime());
        float pulseDamage = BEACON_BASE_DPS * 0.1f;
        for (LivingEntity living : beamTargets(world, player, 40.0d, 1.1d)) {
            living.damage(player.getDamageSources().magic(), pulseDamage);
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
        if (world.isClient() || !(user instanceof PlayerEntity player)) {
            return;
        }
        if (!"corrupted_beacon".equals(resolveArtifactKey(path))) {
            return;
        }
        int usedTicks = this.getMaxUseTime(stack) - remainingUseTicks;
        if (usedTicks > 0) {
            player.getItemCooldownManager().set(this, BEACON_COOLDOWN_TICKS);
        }
    }

    private boolean isOnCooldown(PlayerEntity user) {
        return user.getItemCooldownManager().isCoolingDown(this);
    }

    private void trigger(World world, PlayerEntity user, Hand hand, ItemStack stack) {
        if (!world.isClient()) {
            String id = resolveArtifactKey(path);
            boolean applied = activate(world, user, id);
            if (!applied) {
                return;
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.6f, 1.2f);
            if (!user.isCreative()) {
                stack.damage(1, user, e -> e.sendToolBreakStatus(hand));
            }
            user.getItemCooldownManager().set(this, cooldownFor(id));
        }
    }

    private boolean activate(World world, PlayerEntity user, String id) {
        switch (id) {
            case "soul_healer" -> {
                user.heal(6.0f);
                return true;
            }
            case "boots_of_swiftness" -> {
                // MC Dungeons target: short burst, roughly 2x move speed.
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20 * 8, 4));
                return true;
            }
            case "death_cap_mushroom" -> {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 20 * 8, 1));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 20 * 8, 1));
                return true;
            }
            case "iron_hide_amulet" -> {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 10, 1));
                return true;
            }
            case "totem_of_regeneration" -> {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20 * 10, 1));
                return true;
            }
            case "totem_of_shielding" -> {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 8, 0));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 20 * 8, 1));
                return true;
            }
            case "totem_of_soul_protection" -> {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 8, 1));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 20 * 8, 0));
                return true;
            }
            case "ghost_cloak" -> {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 20 * 6, 0));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20 * 6, 0));
                return true;
            }
            case "light_feather" -> {
                Vec3d v = user.getVelocity();
                user.setVelocity(v.x, Math.max(v.y, 0.65d), v.z);
                user.velocityModified = true;
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 20 * 4, 0));
                return true;
            }
            case "updraft_tome" -> {
                int affected = 0;
                for (LivingEntity living : nearbyHostiles(world, user, 7.0d)) {
                    if (affected >= 7) break;
                    Vec3d lv = living.getVelocity();
                    living.setVelocity(lv.x, Math.max(lv.y, 0.9d), lv.z);
                    living.velocityModified = true;
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * 3, 4));
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20 * 3, 0));
                    living.damage(user.getDamageSources().magic(), 6.0f);
                    affected++;
                }
                return affected > 0;
            }
            case "wind_horn" -> {
                for (LivingEntity living : nearbyHostiles(world, user, 6.5d)) {
                    Vec3d dir = living.getPos().subtract(user.getPos()).normalize();
                    living.takeKnockback(1.8f, -dir.x, -dir.z);
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * 2, 1));
                }
                return true;
            }
            case "gong_of_weakening" -> {
                List<LivingEntity> targets = nearbyAffectableMobs(world, user, 7.0d);
                targets.forEach(l -> {
                    l.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20 * 8, 2));
                    l.damage(user.getDamageSources().magic(), 3.0f);
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
                targets.forEach(l -> l.damage(user.getDamageSources().magic(), 10.0f));
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
                        l.setOnFireFor(4);
                    } else if (mode == 1) {
                        applyFrozen(l, 20 * 3);
                    } else {
                        strikeLightningAt(world, user, l.getPos());
                        l.damage(user.getDamageSources().magic(), 8.0f);
                    }
                    cap++;
                }
                return cap > 0;
            }
            case "fishing_rod" -> {
                LivingEntity target = nearestAffectableMob(world, user, 12.0d);
                if (target == null) return false;
                Vec3d pull = user.getPos().subtract(target.getPos()).normalize().multiply(1.2d);
                target.setVelocity(pull.x, 0.35d, pull.z);
                target.velocityModified = true;
                applyStunned(target, 20);
                return true;
            }
            case "ice_wand" -> {
                int affected = 0;
                for (LivingEntity l : nearbyAffectableMobs(world, user, 8.0d)) {
                    if (affected >= 7) break;
                    applyStunned(l, 20 * 2);
                    applyFrozen(l, 20 * 2);
                    l.damage(user.getDamageSources().magic(), 5.0f);
                    affected++;
                }
                return affected > 0;
            }
            case "love_medallion" -> {
                int affected = 0;
                OWNER_PROTECTED_UNTIL.put(user.getUuid(), world.getTime() + LOVE_MEDALLION_CHARM_TICKS);
                for (LivingEntity l : nearbyAffectableMobs(world, user, 8.0d)) {
                    if (affected >= 3) break;
                    spawnCharmHearts(world, l);
                    l.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 20 * 10, 0));
                    l.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 20 * 10, 1));
                    // Keep "ally" behavior for 10s, then naturally return to normal.
                    l.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 10, 0));
                    if (l instanceof HostileEntity hostile) {
                        markCharmed(hostile, user, world.getTime() + LOVE_MEDALLION_CHARM_TICKS);
                        hostile.setTarget(null);
                        LivingEntity redirected = nearestOtherHostile(world, user, hostile, 12.0d);
                        if (redirected instanceof HostileEntity other) {
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
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 20 * 8, 0));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 8, 0));
                return true;
            }
            case "blast_fungus" -> {
                triggerBlastFungus(world, user);
                return true;
            }
            case "powershaker" -> {
                world.createExplosion(user, user.getX(), user.getY(), user.getZ(), 2.2f, World.ExplosionSourceType.NONE);
                return true;
            }
            case "flaming_quiver", "torment_quiver", "thundering_quiver", "harpoon_quiver" -> {
                user.giveItemStack(new ItemStack(net.minecraft.item.Items.ARROW, 8));
                return true;
            }
            case "satchel_of_elixirs" -> {
                int pick = ThreadLocalRandom.current().nextInt(4);
                if (pick == 0) user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20 * 8, 1));
                if (pick == 1) user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20 * 6, 1));
                if (pick == 2) user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 20 * 6, 0));
                if (pick == 3) user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 6, 0));
                return true;
            }
            case "wonderful_wheat" -> {
                user.heal(4.0f);
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 20 * 2, 0));
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
            default -> {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 20 * 8, 0));
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

    private static List<LivingEntity> nearbyHostiles(World world, PlayerEntity user, double radius) {
        Box aoe = user.getBoundingBox().expand(radius);
        return world.getEntitiesByClass(LivingEntity.class, aoe, living -> living instanceof HostileEntity);
    }

    private static LivingEntity nearestHostile(World world, PlayerEntity user, double radius) {
        List<LivingEntity> list = nearbyHostiles(world, user, radius);
        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (LivingEntity l : list) {
            double d = l.squaredDistanceTo(user);
            if (d < bestD) {
                bestD = d;
                best = l;
            }
        }
        return best;
    }

    private static List<LivingEntity> nearbyAffectableMobs(World world, PlayerEntity user, double radius) {
        Box aoe = user.getBoundingBox().expand(radius);
        return world.getEntitiesByClass(LivingEntity.class, aoe, living -> living != user);
    }

    private static LivingEntity nearestAffectableMob(World world, PlayerEntity user, double radius) {
        List<LivingEntity> list = nearbyAffectableMobs(world, user, radius);
        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (LivingEntity l : list) {
            double d = l.squaredDistanceTo(user);
            if (d < bestD) {
                bestD = d;
                best = l;
            }
        }
        return best;
    }

    private static LivingEntity nearestOtherHostile(World world, PlayerEntity user, LivingEntity self, double radius) {
        Box aoe = self.getBoundingBox().expand(radius);
        List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, aoe, living ->
                living instanceof HostileEntity && living != self && living != user);
        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (LivingEntity l : list) {
            double d = l.squaredDistanceTo(self);
            if (d < bestD) {
                bestD = d;
                best = l;
            }
        }
        return best;
    }

    private static void spawnCharmHearts(World world, LivingEntity target) {
        if (!(world instanceof ServerWorld sw)) {
            return;
        }
        sw.spawnParticles(
                ParticleTypes.HEART,
                target.getX(),
                target.getBodyY(0.6d),
                target.getZ(),
                8,
                0.35d,
                0.35d,
                0.35d,
                0.02d
        );
    }

    private static void markCharmed(HostileEntity target, PlayerEntity owner, long untilTick) {
        CHARMED_UNTIL.put(target.getUuid(), untilTick);
        CHARMED_OWNER.put(target.getUuid(), owner.getUuid());
    }

    private static void tickLoveMedallionCharmed(World world, PlayerEntity ticker) {
        if (!(world instanceof ServerWorld sw)) return;
        long now = world.getTime();
        for (UUID mobId : new ArrayList<>(CHARMED_UNTIL.keySet())) {
            Entity e = sw.getEntity(mobId);
            if (!(e instanceof HostileEntity hostile)) {
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
            PlayerEntity ownerPlayer = null;
            if (ownerId != null) {
                Entity ownerEntity = sw.getEntity(ownerId);
                if (ownerEntity instanceof PlayerEntity p) ownerPlayer = p;
            }
            if (ownerPlayer != null) {
                clearHostilityTowardOwner(hostile, ownerPlayer);
            }
            LivingEntity redirected = nearestEnemyForCharmed(world, hostile, ownerId, 20.0d);
            if (redirected instanceof HostileEntity other) {
                hostile.setTarget(other);
                if (hostile.squaredDistanceTo(other) <= 6.25d) {
                    hostile.tryAttack(other);
                } else {
                    Vec3d chase = other.getPos().subtract(hostile.getPos());
                    if (chase.lengthSquared() > 1.0E-4d) {
                        Vec3d step = chase.normalize().multiply(0.18d);
                        hostile.setVelocity(step.x, Math.max(hostile.getVelocity().y, 0.0d), step.z);
                        hostile.velocityModified = true;
                    }
                }
                if (other.getTarget() == null || other.getTarget() instanceof PlayerEntity || isCharmedActive(other.getUuid(), now)) {
                    other.setTarget(hostile);
                }
            } else {
                hostile.setTarget(null);
                if (ownerPlayer != null && hostile.squaredDistanceTo(ownerPlayer) < 9.0d) {
                    Vec3d away = hostile.getPos().subtract(ownerPlayer.getPos());
                    if (away.lengthSquared() > 1.0E-4d) {
                        Vec3d push = away.normalize().multiply(0.35d);
                        hostile.setVelocity(push.x, Math.max(hostile.getVelocity().y, 0.08d), push.z);
                        hostile.velocityModified = true;
                    }
                }
            }
        }
        redirectNearbyUncharmedAggro(world, sw, ticker, now);
        enforceOwnerSafetyDuringCharm(world, ticker, now);
    }

    private static LivingEntity nearestEnemyForCharmed(World world, HostileEntity self, UUID ownerId, double radius) {
        Box aoe = self.getBoundingBox().expand(radius);
        long now = world.getTime();
        List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, aoe, living ->
                living instanceof HostileEntity
                        && living != self
                        && !isCharmedActive(living.getUuid(), now)
                        && !isSameOwnerPlayer(living, world, ownerId));
        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (LivingEntity l : list) {
            double d = l.squaredDistanceTo(self);
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

    private static void redirectNearbyUncharmedAggro(World world, ServerWorld sw, PlayerEntity owner, long now) {
        List<HostileEntity> nearby = world.getEntitiesByClass(HostileEntity.class, owner.getBoundingBox().expand(32.0d), e -> true);
        for (HostileEntity uncharmed : nearby) {
            if (isCharmedActive(uncharmed.getUuid(), now)) continue;
            if (!(uncharmed.getTarget() instanceof PlayerEntity)) continue;
            HostileEntity charmed = nearestActiveCharmed(world, uncharmed, owner, now, 20.0d);
            if (charmed != null) {
                uncharmed.setTarget(charmed);
                if (charmed.getTarget() == null || charmed.getTarget() instanceof PlayerEntity) {
                    charmed.setTarget(uncharmed);
                }
            } else {
                // During charm window, uncharmed mobs should not keep aggro on player.
                uncharmed.setTarget(null);
            }
        }
    }

    private static HostileEntity nearestActiveCharmed(World world, HostileEntity from, PlayerEntity owner, long now, double radius) {
        Box aoe = from.getBoundingBox().expand(radius);
        List<HostileEntity> list = world.getEntitiesByClass(HostileEntity.class, aoe, e ->
                e != from && isCharmedActive(e.getUuid(), now));
        HostileEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (HostileEntity h : list) {
            if (!h.isAlive()) continue;
            double d = h.squaredDistanceTo(from);
            if (d < bestD) {
                bestD = d;
                best = h;
            }
        }
        return best;
    }

    private static void enforceOwnerSafetyDuringCharm(World world, PlayerEntity owner, long now) {
        boolean active = isOwnerProtectionActive(owner.getUuid(), now) || hasActiveCharmForOwner(owner.getUuid(), now);
        applyOwnerInvulnerability(owner, active);
        if (!active) {
            return;
        }
        List<HostileEntity> nearby = world.getEntitiesByClass(HostileEntity.class, owner.getBoundingBox().expand(18.0d), e -> true);
        for (HostileEntity hostile : nearby) {
            if (hostile.getTarget() == owner) {
                hostile.setTarget(null);
            }
            clearHostilityTowardOwner(hostile, owner);
            if (hostile.squaredDistanceTo(owner) < 12.25d) {
                Vec3d away = hostile.getPos().subtract(owner.getPos());
                if (away.lengthSquared() > 1.0E-4d) {
                    Vec3d push = away.normalize().multiply(0.28d);
                    hostile.setVelocity(push.x, Math.max(hostile.getVelocity().y, 0.10d), push.z);
                    hostile.velocityModified = true;
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

    public static boolean isLoveOwnerProtected(PlayerEntity owner, long now) {
        return isOwnerProtectionActive(owner.getUuid(), now) || hasActiveCharmForOwner(owner.getUuid(), now);
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

    private static void applyOwnerInvulnerability(PlayerEntity owner, boolean active) {
        UUID ownerId = owner.getUuid();
        if (active) {
            if (!OWNER_PREV_INVULN.containsKey(ownerId)) {
                OWNER_PREV_INVULN.put(ownerId, owner.getAbilities().invulnerable);
            }
            if (!owner.getAbilities().invulnerable) {
                owner.getAbilities().invulnerable = true;
                if (owner instanceof ServerPlayerEntity sp) {
                    sp.sendAbilitiesUpdate();
                }
            }
            return;
        }
        Boolean previous = OWNER_PREV_INVULN.remove(ownerId);
        if (previous != null && owner.getAbilities().invulnerable != previous) {
            owner.getAbilities().invulnerable = previous;
            if (owner instanceof ServerPlayerEntity sp) {
                sp.sendAbilitiesUpdate();
            }
        }
    }

    private static boolean isSameOwnerPlayer(LivingEntity living, World world, UUID ownerId) {
        if (!(living instanceof PlayerEntity player) || ownerId == null) return false;
        return ownerId.equals(player.getUuid());
    }

    private static void clearHostilityTowardOwner(HostileEntity hostile, PlayerEntity ownerPlayer) {
        if (hostile.getTarget() == ownerPlayer) {
            hostile.setTarget(null);
        }
        tryInvokeSingleArg(hostile, "setAttacker", LivingEntity.class, null);
        Object angerable = hostile;
        tryInvokeSingleArg(angerable, "setAngryAt", UUID.class, null);
        tryInvokeSingleArg(angerable, "setAngerTime", int.class, 0);
        tryInvokeNoArg(angerable, "stopAnger");
    }

    private static void tryInvokeNoArg(Object target, String methodName) {
        try {
            target.getClass().getMethod(methodName).invoke(target);
        } catch (Throwable ignored) {
        }
    }

    private static void tryInvokeSingleArg(Object target, String methodName, Class<?> argType, Object arg) {
        try {
            target.getClass().getMethod(methodName, argType).invoke(target, arg);
        } catch (Throwable ignored) {
        }
    }

    private static void applyStunned(LivingEntity target, int durationTicks) {
        // "Stunned" is modeled as near-immobilize + weakened offense.
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, durationTicks, 10));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, durationTicks, 2));
    }

    private static void applyPoisoned(LivingEntity target, int durationTicks) {
        // "Poisoned" here means stronger poison (剧毒).
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, durationTicks, 1));
    }

    private static void applyFrozen(LivingEntity target, int durationTicks) {
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, durationTicks, 6));
    }

    private static List<LivingEntity> beamTargets(World world, PlayerEntity user, double range, double radius) {
        Vec3d start = beaconStart(user);
        Vec3d end = start.add(user.getRotationVec(1.0f).multiply(range));
        Box box = new Box(start, end).expand(radius);
        return world.getEntitiesByClass(LivingEntity.class, box, living -> {
            if (!(living instanceof HostileEntity)) return false;
            Vec3d p = living.getPos().add(0.0d, living.getHeight() * 0.5d, 0.0d);
            return pointToSegmentDistanceSq(p, start, end) <= radius * radius
                    && world.raycast(new net.minecraft.world.RaycastContext(
                    start, p,
                    net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                    net.minecraft.world.RaycastContext.FluidHandling.NONE,
                    user
            )).getType() == net.minecraft.util.hit.HitResult.Type.MISS;
        });
    }

    private static void strikeLightningRod(World world, PlayerEntity user) {
        Vec3d center = resolveTargetPoint(world, user, 24.0d);
        Box aoe = new Box(
                center.x - 2.5d, center.y - 2.5d, center.z - 2.5d,
                center.x + 2.5d, center.y + 2.5d, center.z + 2.5d
        );
        for (LivingEntity living : world.getEntitiesByClass(LivingEntity.class, aoe, e -> e instanceof HostileEntity)) {
            living.damage(user.getDamageSources().magic(), 14.0f);
        }
        var bolt = EntityType.LIGHTNING_BOLT.create(world);
        if (bolt != null) {
            bolt.refreshPositionAfterTeleport(center.x, center.y, center.z);
            world.spawnEntity(bolt);
        }
    }

    private static void strikeLightningAt(World world, PlayerEntity user, Vec3d center) {
        var bolt = EntityType.LIGHTNING_BOLT.create(world);
        if (bolt != null) {
            bolt.refreshPositionAfterTeleport(center.x, center.y, center.z);
            world.spawnEntity(bolt);
        }
    }

    private static Vec3d resolveTargetPoint(World world, PlayerEntity user, double range) {
        HitResult hit = user.raycast(range, 1.0f, false);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bhr = (BlockHitResult) hit;
            return bhr.getPos();
        }
        return user.getEyePos().add(user.getRotationVec(1.0f).multiply(range));
    }

    private static void triggerScatterMines(World world, PlayerEntity user) {
        Vec3d forward = user.getRotationVec(1.0f).normalize();
        Vec3d flatForward = new Vec3d(forward.x, 0.0d, forward.z).normalize();
        if (flatForward.lengthSquared() < 1.0E-6d) flatForward = new Vec3d(0.0d, 0.0d, 1.0d);
        Vec3d left = new Vec3d(-flatForward.z, 0.0d, flatForward.x);
        Vec3d center = user.getPos().add(flatForward.multiply(1.7d));
        Vec3d[] points = new Vec3d[]{
                center,
                center.add(left.multiply(1.2d)),
                center.subtract(left.multiply(1.2d))
        };
        for (Vec3d p : points) {
            world.createExplosion(user, p.x, p.y, p.z, 1.9f, World.ExplosionSourceType.NONE);
        }
    }

    private static void triggerBlastFungus(World world, PlayerEntity user) {
        Vec3d forward = user.getRotationVec(1.0f).normalize();
        Vec3d flatForward = new Vec3d(forward.x, 0.0d, forward.z).normalize();
        if (flatForward.lengthSquared() < 1.0E-6d) flatForward = new Vec3d(0.0d, 0.0d, 1.0d);
        Vec3d left = new Vec3d(-flatForward.z, 0.0d, flatForward.x);
        Vec3d center = user.getPos().add(flatForward.multiply(1.6d));
        Vec3d[] points = new Vec3d[]{
                center,
                center.add(left.multiply(1.6d)),
                center.subtract(left.multiply(1.6d)),
                center.add(flatForward.multiply(1.8d)),
                center.add(flatForward.multiply(0.8d)).add(left.multiply(0.8d))
        };
        for (Vec3d p : points) {
            world.createExplosion(user, p.x, p.y, p.z, 1.8f, World.ExplosionSourceType.NONE);
        }
    }

    private static boolean triggerSpinblade(World world, PlayerEntity user) {
        Vec3d start = user.getEyePos();
        Vec3d mid = start.add(user.getRotationVec(1.0f).normalize().multiply(12.0d));
        int hits = 0;
        hits += damageAlongSegment(world, user, start, mid, 1.0d, 8.0f); // outgoing
        hits += damageAlongSegment(world, user, mid, start, 1.0d, 8.0f); // returning
        return hits > 0;
    }

    private static boolean triggerEyeOfGuardian(World world, PlayerEntity user) {
        int hits = 0;
        for (LivingEntity living : beamTargets(world, user, 40.0d, 1.2d)) {
            living.damage(user.getDamageSources().magic(), 12.0f);
            hits++;
        }
        // Simulate limited turning speed while beam is active.
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * 4, 2));
        return hits > 0;
    }

    private static boolean triggerCorruptedPumpkin(World world, PlayerEntity user) {
        int hits = 0;
        for (LivingEntity living : beamTargets(world, user, 40.0d, 1.3d)) {
            living.damage(user.getDamageSources().magic(), 10.0f);
            hits++;
        }
        return hits > 0;
    }

    private static double pointToSegmentDistanceSq(Vec3d p, Vec3d a, Vec3d b) {
        Vec3d ab = b.subtract(a);
        double abLen2 = ab.lengthSquared();
        if (abLen2 <= 1.0E-7d) return p.squaredDistanceTo(a);
        double t = p.subtract(a).dotProduct(ab) / abLen2;
        t = Math.max(0.0d, Math.min(1.0d, t));
        Vec3d proj = a.add(ab.multiply(t));
        return p.squaredDistanceTo(proj);
    }

    private static int damageAlongSegment(World world, PlayerEntity user, Vec3d from, Vec3d to, double radius, float damage) {
        Box box = new Box(from, to).expand(radius);
        int hits = 0;
        for (LivingEntity living : world.getEntitiesByClass(LivingEntity.class, box, e -> e instanceof HostileEntity)) {
            Vec3d p = living.getPos().add(0.0d, living.getHeight() * 0.5d, 0.0d);
            if (pointToSegmentDistanceSq(p, from, to) <= radius * radius) {
                living.damage(user.getDamageSources().magic(), damage);
                hits++;
            }
        }
        return hits;
    }

    private static Vec3d beaconStart(PlayerEntity user) {
        // Slightly below eye line so the beam does not block crosshair.
        return user.getEyePos().add(0.0d, -0.35d, 0.0d);
    }


    private static void spawnTamedWolf(World world, PlayerEntity user) {
        WolfEntity wolf = EntityType.WOLF.create(world);
        if (wolf == null) return;
        wolf.refreshPositionAndAngles(user.getX(), user.getY(), user.getZ(), user.getYaw(), 0.0f);
        wolf.setOwner(user);
        wolf.setTamed(true);
        world.spawnEntity(wolf);
    }

    private static <T extends Entity> void spawnEntity(World world, EntityType<T> type, PlayerEntity user) {
        T entity = type.create(world);
        if (entity == null) return;
        entity.refreshPositionAndAngles(user.getX(), user.getY(), user.getZ(), user.getYaw(), 0.0f);
        world.spawnEntity(entity);
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
