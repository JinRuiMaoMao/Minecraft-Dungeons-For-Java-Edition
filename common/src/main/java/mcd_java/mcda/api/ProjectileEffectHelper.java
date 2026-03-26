package mcd_java.mcda.api;

import mcd_java.mcda.items.ArmorSets;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static mcd_java.mcda.api.CleanlinessHelper.checkFullArmor;
import static mcd_java.mcda.api.CleanlinessHelper.random;

public class ProjectileEffectHelper {

    public static void fireSnowballAtNearbyEnemy(LivingEntity user, int distance) {
        World world = user.getWorld();
        Snowball snowballEntity = new Snowball(world, user);
        fireProjectileAtNearbyEntities(snowballEntity, user, distance, world);
    }

    public static void fireShulkerBulletAtNearbyEnemy(LivingEntity user) {
        if (!checkFullArmor(user, ArmorSets.STURDY_SHULKER))
            return;

        World world = user.getWorld();
        Random random = user.getRandom();

        int distance = 10;
        List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class,
                new Box(user.getX() - distance, user.getY() - distance, user.getZ() - distance,
                        user.getX() + distance, user.getY() + distance, user.getZ() + distance),
                (nearbyEntity) -> nearbyEntity != user && AbilityHelper.canFireAtEnemy(user, nearbyEntity));
        if (nearbyEntities.size() < 2) return;
        Optional<LivingEntity> nearest = nearbyEntities.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(user)));
        LivingEntity target = nearest.get();

        ShulkerBullet shulkerBulletEntity = new ShulkerBullet(world, user, target,
                Direction.Axis.getRandom(random));
        // borrowed from AbstractSkeletonEntity
        double d = target.getX() - shulkerBulletEntity.getX();
        double e = target.getY(0.3333333333333333D) - shulkerBulletEntity.getY();
        double f = target.getZ() - shulkerBulletEntity.getZ();
        double g = Math.sqrt(d * d + f * f);
        shulkerBulletEntity.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.5F, 1.0F);
        setProjectileTowards(shulkerBulletEntity, d, e, g, 0);
        //
        user.getWorld().spawnEntity(shulkerBulletEntity);
    }

    public static void ricochetArrowLikeShield(PersistentProjectileEntity persistentProjectileEntity){
        persistentProjectileEntity.setDeltaMovement(persistentProjectileEntity.getDeltaMovement().scale(-0.1D));
        persistentProjectileEntity.getViewYRot(180.0F);
        persistentProjectileEntity.yRotO += 180.0F;
        if (!persistentProjectileEntity.getWorld().isClientSide && persistentProjectileEntity.getDeltaMovement().lengthSqr() < 1.0E-7D){
            if (persistentProjectileEntity.pickup == AbstractArrow.Pickup.ALLOWED){
                persistentProjectileEntity.spawnAtLocation(new ItemStack(Items.ARROW), 0.1F);
            }
            persistentProjectileEntity.remove(Entity.RemovalReason.KILLED);
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public static void setProjectileTowards(Projectile projectileEntity, double x, double y, double z, float inaccuracy) {
        Vec3d vec3d = (new Vec3(x, y, z))
                .normalize()
                .add(
                        random.nextGaussian() * 0.0075 * inaccuracy,
                        random.nextGaussian() * 0.0075 * inaccuracy,
                        random.nextGaussian() * 0.0075 * inaccuracy);
        projectileEntity.setDeltaMovement(vec3d);
        float f = Mth.sqrt((float)projectileEntity.distanceToSqr(vec3d));
        projectileEntity.getViewYRot((float)(Mth.atan2(vec3d.x, vec3d.z) * (180.0 / Math.PI)));
        projectileEntity.getViewXRot((float)(Mth.atan2(vec3d.y, f) * (180.0 / Math.PI)));
        projectileEntity.yRotO = projectileEntity.getYRot();
        projectileEntity.xRotO = projectileEntity.getXRot();
    }

    private static List<LivingEntity> getNearbyLivingEntities(LivingEntity user, int distance, World world){
        return world.getEntitiesOfClass(LivingEntity.class,
                new Box(user.getX() - distance, user.getY() - distance, user.getZ() - distance,
                        user.getX() + distance, user.getY() + distance, user.getZ() + distance),
                (nearbyEntity) -> nearbyEntity != user && AbilityHelper.canFireAtEnemy(user, nearbyEntity));
    }

    private static void fireProjectileAtNearbyEntities(Projectile projectileEntity, LivingEntity user, int distance, World world) {
        List<LivingEntity> nearbyEntities = getNearbyLivingEntities(user, distance, world);
        if (nearbyEntities.size() < 2) return;
        Optional<LivingEntity> nearest = nearbyEntities.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(user)));
        LivingEntity target = nearest.get();
        double d = target.getX() - projectileEntity.getX();
        double e = target.getY(0.3333333333333333D) - projectileEntity.getY();
        double f = target.getZ() - projectileEntity.getZ();
        double g = Math.sqrt(d * d + f * f);
        projectileEntity.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.5F, 1.0F);
        setProjectileTowards(projectileEntity, d, e, g, 0);
        user.getWorld().spawnEntity(projectileEntity);
    }
}