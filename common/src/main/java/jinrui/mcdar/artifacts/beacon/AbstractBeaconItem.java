package jinrui.mcdar.artifacts.beacon;

import jinrui.mcdar.artifacts.ArtifactDamagingItem;
import jinrui.mcdar.enums.DamagingArtifactID;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntityUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBeaconItem extends ArtifactDamagingItem {

    public static final double RAYTRACE_DISTANCE = 256;
    public static final float BEAM_DAMAGE_PER_TICK = 0.5F;
    public static final float XP_COST_PER_TICK = 0.625F;

    public AbstractBeaconItem(DamagingArtifactID artefactID, int artifactDurability) {
        super(artefactID, artifactDurability);
    }

    @Nullable
    public static BeaconBeamColor getBeaconBeamColor(ItemStack itemStack){
        Item stackItem = itemStack.getItem();
        return stackItem instanceof AbstractBeaconItem ? ((AbstractBeaconItem) stackItem).getBeamColor() : null;
    }

    public abstract boolean canFire(PlayerEntity playerEntity, ItemStack itemStack);

    public static ItemStack getBeacon(PlayerEntity player) {
        ItemStack heldItem = PlayerEntity.getMainHandItem();
        if (!(heldItem.getItem() instanceof AbstractBeaconItem)) {
            heldItem = PlayerEntity.getOffhandItem();
            if (!(heldItem.getItem() instanceof AbstractBeaconItem)) {
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }

    public abstract BeaconBeamColor getBeamColor();

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
        ItemStack itemStack = user.getItemInHand(hand);

        if (canFire(user, itemStack)){
            user.playSound(
                    SoundEvents.BEACON_ACTIVATE,
                    1.0f,
                    1.0f
            );
        } else {
            return new TypedActionResult<>(InteractionResult.FAIL, itemStack);
        }

        if (!world.isClientSide) {
            user.startUsingItem(hand);
        }
        return new TypedActionResult<>(InteractionResult.PASS, itemStack);
    }

    @Override
    public int getUseDuration(ItemStack stack){
        return 72000;
    }

    @Override
    public UseAction getUseActionation(ItemStack stack){
        return UseAction.NONE;
    }

    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks){
        user.playSound(
                SoundEvents.BEACON_DEACTIVATE,
                1.0f,
                1.0f
        );
    }

    @Override
    public void onUseTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks){
        if (user instanceof PlayerEntity playerEntity){

            if (playerEntity.isCreative() || this.consumeTick(playerEntity)) {
                HitResult result = playerEntity.pick(RAYTRACE_DISTANCE, 1.0f, false);
                Vec3d eyeVector = playerEntity.getEyePosition(1.0f);
                Vec3d lookVector = playerEntity.getLookAngle();
                Vec3d targetVector = eyeVector.add(lookVector.x * RAYTRACE_DISTANCE, lookVector.y * RAYTRACE_DISTANCE
                        , lookVector.z * RAYTRACE_DISTANCE);
                AABB box = playerEntity.getBoundingBox().expandTowards(lookVector.scale(RAYTRACE_DISTANCE)).expand(1.0D,
                        1.0D, 1.0D);
                EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(world, playerEntity, eyeVector,
                        targetVector, box,
                        entity -> entity instanceof LivingEntity && !entity.isSpectator() /*&& entity.collides()*/);
                if (entityHitResult != null && result.getLocation().distanceToSqr(eyeVector) > entityHitResult.getLocation().distanceToSqr(eyeVector)){
                    if (!world.isClientSide()){
                        Entity entity = entityHitResult.getEntity();
                        entity.invulnerableTime = 0;
                        entity.hurt(playerEntity.damageSources().indirectMagic(playerEntity, playerEntity), BEAM_DAMAGE_PER_TICK);
                    }
                }
                if (getMaxStackSize() % 20 == 0){
                    stack.hurtAndBreak(1, playerEntity, (entity) -> entity.broadcastBreakEvent(playerEntity.getUsedItemHand()));
                }
            }
        }
    }

    protected abstract boolean consumeTick(PlayerEntity playerEntity);
}
