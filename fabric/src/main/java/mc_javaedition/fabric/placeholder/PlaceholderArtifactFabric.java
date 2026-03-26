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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
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
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PlaceholderArtifactFabric extends Item {
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
        if (isOnCooldown(user)) {
            return TypedActionResult.fail(stack);
        }
        trigger(world, user, hand, stack);
        user.swingHand(hand);
        return TypedActionResult.success(stack, world.isClient());
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

    private boolean isOnCooldown(PlayerEntity user) {
        return user.getItemCooldownManager().isCoolingDown(this);
    }

    private void trigger(World world, PlayerEntity user, Hand hand, ItemStack stack) {
        if (!world.isClient()) {
            String id = resolveArtifactKey(path);
            activate(world, user, id);
            user.sendMessage(Text.literal("Artifact activated: " + path), true);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.6f, 1.2f);
            if (!user.isCreative()) {
                stack.damage(1, user, e -> e.sendToolBreakStatus(hand));
            }
            user.getItemCooldownManager().set(this, cooldownFor(id));
        }
    }

    private void activate(World world, PlayerEntity user, String id) {
        switch (id) {
            case "soul_healer" -> user.heal(6.0f);
            case "boots_of_swiftness" -> {
                // MC Dungeons target: short burst, roughly 2x move speed.
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20 * 8, 4));
            }
            case "death_cap_mushroom" -> {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 20 * 8, 1));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 20 * 8, 1));
            }
            case "iron_hide_amulet" -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 10, 1));
            case "totem_of_regeneration" -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20 * 10, 1));
            case "totem_of_shielding" -> {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 8, 0));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 20 * 8, 1));
            }
            case "totem_of_soul_protection" -> {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 8, 1));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 20 * 8, 0));
            }
            case "ghost_cloak" -> {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 20 * 6, 0));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20 * 6, 0));
            }
            case "light_feather", "updraft_tome" -> {
                Vec3d v = user.getVelocity();
                user.setVelocity(v.x, Math.max(v.y, 0.65d), v.z);
                user.velocityModified = true;
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 20 * 4, 0));
                if (id.equals("updraft_tome")) {
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
                }
            }
            case "wind_horn" -> {
                for (LivingEntity living : nearbyHostiles(world, user, 6.5d)) {
                    Vec3d dir = living.getPos().subtract(user.getPos()).normalize();
                    living.takeKnockback(1.8f, -dir.x, -dir.z);
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * 2, 1));
                }
            }
            case "gong_of_weakening" -> nearbyHostiles(world, user, 6.0d).forEach(l -> l.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20 * 6, 1)));
            case "shock_powder" -> nearbyHostiles(world, user, 6.0d).forEach(l -> {
                l.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * 4, 2));
                l.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 20 * 3, 0));
            });
            case "harvester" -> nearbyHostiles(world, user, 6.0d).forEach(l -> l.damage(user.getDamageSources().magic(), 10.0f));
            case "lightning_rod" -> strikeLightningRod(world, user);
            case "scatter_mines" -> triggerScatterMines(world, user);
            case "spinblade" -> triggerSpinblade(world, user);
            case "eye_of_the_guardian" -> triggerEyeOfGuardian(world, user);
            case "corrupted_pumpkin" -> triggerCorruptedPumpkin(world, user);
            case "satchel_of_elements" -> nearbyHostiles(world, user, 6.0d).forEach(l -> l.damage(user.getDamageSources().magic(), 6.0f));
            case "corrupted_beacon" -> {
                for (LivingEntity living : beamTargets(world, user, 40.0d, 1.1d)) {
                    living.damage(user.getDamageSources().magic(), 2.0f);
                }
            }
            case "blast_fungus" -> triggerBlastFungus(world, user);
            case "powershaker" -> world.createExplosion(user, user.getX(), user.getY(), user.getZ(), 2.2f, World.ExplosionSourceType.MOB);
            case "flaming_quiver", "torment_quiver", "thundering_quiver", "harpoon_quiver" -> user.giveItemStack(new ItemStack(net.minecraft.item.Items.ARROW, 8));
            case "satchel_of_elixirs" -> {
                int pick = ThreadLocalRandom.current().nextInt(4);
                if (pick == 0) user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20 * 8, 1));
                if (pick == 1) user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20 * 6, 1));
                if (pick == 2) user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 20 * 6, 0));
                if (pick == 3) user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 6, 0));
            }
            case "wonderful_wheat" -> {
                user.heal(4.0f);
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 20 * 2, 0));
            }
            case "tasty_bone" -> spawnTamedWolf(world, user);
            case "golem_kit" -> spawnEntity(world, EntityType.IRON_GOLEM, user);
            case "buzzy_nest" -> {
                spawnEntity(world, EntityType.BEE, user);
                spawnEntity(world, EntityType.BEE, user);
            }
            case "enchanted_grass" -> spawnEntity(world, EntityType.SHEEP, user);
            case "love_medallion" -> nearbyHostiles(world, user, 7.0d).forEach(l -> l.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 20 * 10, 0)));
            case "corrupted_seeds" -> nearbyHostiles(world, user, 7.0d).forEach(l -> {
                l.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * 6, 1));
                l.damage(user.getDamageSources().magic(), 2.0f);
            });
            case "enchanters_tome" -> {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 20 * 8, 0));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 8, 0));
            }
            default -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 20 * 8, 0));
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

    private static List<LivingEntity> nearbyHostiles(World world, PlayerEntity user, double radius) {
        Box aoe = user.getBoundingBox().expand(radius);
        return world.getEntitiesByClass(LivingEntity.class, aoe, living -> living instanceof HostileEntity);
    }

    private static List<LivingEntity> beamTargets(World world, PlayerEntity user, double range, double radius) {
        Vec3d start = user.getEyePos();
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
            world.createExplosion(user, p.x, p.y, p.z, 1.9f, World.ExplosionSourceType.MOB);
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
            world.createExplosion(user, p.x, p.y, p.z, 1.8f, World.ExplosionSourceType.MOB);
        }
    }

    private static void triggerSpinblade(World world, PlayerEntity user) {
        Vec3d start = user.getEyePos();
        Vec3d mid = start.add(user.getRotationVec(1.0f).normalize().multiply(12.0d));
        damageAlongSegment(world, user, start, mid, 1.0d, 8.0f); // outgoing
        damageAlongSegment(world, user, mid, start, 1.0d, 8.0f); // returning
    }

    private static void triggerEyeOfGuardian(World world, PlayerEntity user) {
        for (LivingEntity living : beamTargets(world, user, 40.0d, 1.2d)) {
            living.damage(user.getDamageSources().magic(), 12.0f);
        }
        // Simulate limited turning speed while beam is active.
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * 4, 2));
    }

    private static void triggerCorruptedPumpkin(World world, PlayerEntity user) {
        for (LivingEntity living : beamTargets(world, user, 40.0d, 1.3d)) {
            living.damage(user.getDamageSources().magic(), 10.0f);
        }
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

    private static void damageAlongSegment(World world, PlayerEntity user, Vec3d from, Vec3d to, double radius, float damage) {
        Box box = new Box(from, to).expand(radius);
        for (LivingEntity living : world.getEntitiesByClass(LivingEntity.class, box, e -> e instanceof HostileEntity)) {
            Vec3d p = living.getPos().add(0.0d, living.getHeight() * 0.5d, 0.0d);
            if (pointToSegmentDistanceSq(p, from, to) <= radius * radius) {
                living.damage(user.getDamageSources().magic(), damage);
            }
        }
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
