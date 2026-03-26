package mc_javaedition.forge.placeholder;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PlaceholderArtifactForge extends Item {
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
        if (isOnCooldown(user)) {
            return InteractionResultHolder.fail(stack);
        }
        trigger(world, user, hand, stack);
        user.swing(hand);
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
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

    private boolean isOnCooldown(Player user) {
        return user.getCooldowns().isOnCooldown(this);
    }

    private void trigger(Level world, Player user, InteractionHand hand, ItemStack stack) {
        if (!world.isClientSide) {
            String id = resolveArtifactKey(path);
            activate(world, user, id);
            user.displayClientMessage(Component.literal("Artifact activated: " + path), true);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.2f);
            if (!user.getAbilities().instabuild) {
                stack.hurtAndBreak(1, user, p -> p.broadcastBreakEvent(hand));
            }
            user.getCooldowns().addCooldown(this, cooldownFor(id));
        }
    }

    private void activate(Level world, Player user, String id) {
        switch (id) {
            case "soul_healer" -> user.heal(6.0f);
            case "boots_of_swiftness" -> {
                // MC Dungeons target: short burst, roughly 2x move speed.
                user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 8, 4));
            }
            case "death_cap_mushroom" -> {
                user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 8, 1));
                user.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 8, 1));
            }
            case "iron_hide_amulet" -> user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 10, 1));
            case "totem_of_regeneration" -> user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 10, 1));
            case "totem_of_shielding" -> {
                user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 8, 0));
                user.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 8, 1));
            }
            case "totem_of_soul_protection" -> {
                user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 8, 1));
                user.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 8, 0));
            }
            case "ghost_cloak" -> {
                user.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 6, 0));
                user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 0));
            }
            case "light_feather", "updraft_tome" -> {
                Vec3 v = user.getDeltaMovement();
                user.setDeltaMovement(v.x, Math.max(v.y, 0.65d), v.z);
                user.hurtMarked = true;
                user.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 4, 0));
                if (id.equals("updraft_tome")) {
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
                }
            }
            case "wind_horn" -> {
                for (LivingEntity living : nearbyHostiles(world, user, 6.5d)) {
                    Vec3 dir = living.position().subtract(user.position()).normalize();
                    living.knockback(1.8f, -dir.x, -dir.z);
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 2, 1));
                }
            }
            case "gong_of_weakening" -> nearbyHostiles(world, user, 6.0d).forEach(l -> l.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 6, 1)));
            case "shock_powder" -> nearbyHostiles(world, user, 6.0d).forEach(l -> {
                l.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 4, 2));
                l.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 3, 0));
            });
            case "harvester" -> nearbyHostiles(world, user, 6.0d).forEach(l -> l.hurt(user.damageSources().magic(), 10.0f));
            case "lightning_rod" -> strikeLightningRod(world, user);
            case "scatter_mines" -> triggerScatterMines(world, user);
            case "spinblade" -> triggerSpinblade(world, user);
            case "eye_of_the_guardian" -> triggerEyeOfGuardian(world, user);
            case "corrupted_pumpkin" -> triggerCorruptedPumpkin(world, user);
            case "satchel_of_elements" -> nearbyHostiles(world, user, 6.0d).forEach(l -> l.hurt(user.damageSources().magic(), 6.0f));
            case "corrupted_beacon" -> {
                for (LivingEntity living : beamTargets(world, user, 40.0d, 1.1d)) {
                    living.hurt(user.damageSources().magic(), 2.0f);
                }
            }
            case "blast_fungus" -> triggerBlastFungus(world, user);
            case "powershaker" -> world.explode(user, user.getX(), user.getY(), user.getZ(), 2.2f, Level.ExplosionInteraction.MOB);
            case "flaming_quiver", "torment_quiver", "thundering_quiver", "harpoon_quiver" -> user.addItem(new ItemStack(Items.ARROW, 8));
            case "satchel_of_elixirs" -> {
                int pick = ThreadLocalRandom.current().nextInt(4);
                if (pick == 0) user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 8, 1));
                if (pick == 1) user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 6, 1));
                if (pick == 2) user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 0));
                if (pick == 3) user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 0));
            }
            case "wonderful_wheat" -> {
                user.heal(4.0f);
                user.addEffect(new MobEffectInstance(MobEffects.SATURATION, 20 * 2, 0));
            }
            case "tasty_bone" -> spawnTamedWolf(world, user);
            case "golem_kit" -> spawnEntity(world, EntityType.IRON_GOLEM, user);
            case "buzzy_nest" -> {
                spawnEntity(world, EntityType.BEE, user);
                spawnEntity(world, EntityType.BEE, user);
            }
            case "enchanted_grass" -> spawnEntity(world, EntityType.SHEEP, user);
            case "love_medallion" -> nearbyHostiles(world, user, 7.0d).forEach(l -> l.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 10, 0)));
            case "corrupted_seeds" -> nearbyHostiles(world, user, 7.0d).forEach(l -> {
                l.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 6, 1));
                l.hurt(user.damageSources().magic(), 2.0f);
            });
            case "enchanters_tome" -> {
                user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 8, 0));
                user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 8, 0));
            }
            default -> user.addEffect(new MobEffectInstance(MobEffects.LUCK, 20 * 8, 0));
        }
    }

    private static int cooldownFor(String idPath) {
        if (idPath.contains("quiver")) return 30;
        if (idPath.equals("corrupted_beacon")) return 50; // 2.5s
        if (idPath.equals("corrupted_pumpkin")) return 50; // 2.5s
        if (idPath.equals("lightning_rod")) return 40; // 2s
        if (idPath.equals("eye_of_the_guardian")) return 440; // 22s
        if (idPath.equals("spinblade")) return 100; // 5s
        if (idPath.equals("boots_of_swiftness")) return 100; // 5s
        if (idPath.equals("harvester")) return 80; // 4s
        if (idPath.equals("blast_fungus")) return 120; // 6s
        if (idPath.equals("scatter_mines")) return 240; // 12s
        if (idPath.equals("updraft_tome")) return 240; // 12s
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

    private static List<LivingEntity> beamTargets(Level world, Player user, double range, double radius) {
        Vec3 start = user.getEyePosition();
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
            world.explode(user, p.x, p.y, p.z, 1.9f, Level.ExplosionInteraction.MOB);
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
            world.explode(user, p.x, p.y, p.z, 1.8f, Level.ExplosionInteraction.MOB);
        }
    }

    private static void triggerSpinblade(Level world, Player user) {
        Vec3 start = user.getEyePosition();
        Vec3 mid = start.add(user.getLookAngle().normalize().scale(12.0d));
        damageAlongSegment(world, user, start, mid, 1.0d, 8.0f); // outgoing
        damageAlongSegment(world, user, mid, start, 1.0d, 8.0f); // returning
    }

    private static void triggerEyeOfGuardian(Level world, Player user) {
        for (LivingEntity living : beamTargets(world, user, 40.0d, 1.2d)) {
            living.hurt(user.damageSources().magic(), 12.0f);
        }
        // Simulate limited turning speed while beam is active.
        user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 4, 2));
    }

    private static void triggerCorruptedPumpkin(Level world, Player user) {
        for (LivingEntity living : beamTargets(world, user, 40.0d, 1.3d)) {
            living.hurt(user.damageSources().magic(), 10.0f);
        }
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

    private static void damageAlongSegment(Level world, Player user, Vec3 from, Vec3 to, double radius, float damage) {
        AABB box = new AABB(from, to).inflate(radius);
        for (LivingEntity living : world.getEntitiesOfClass(LivingEntity.class, box, e -> e instanceof Monster)) {
            Vec3 p = living.position().add(0.0d, living.getBbHeight() * 0.5d, 0.0d);
            if (pointToSegmentDistanceSq(p, from, to) <= radius * radius) {
                living.hurt(user.damageSources().magic(), damage);
            }
        }
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
