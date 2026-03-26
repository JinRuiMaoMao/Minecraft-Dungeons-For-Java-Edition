package jinrui.mcdar.artifacts.beacon;

import jinrui.mcdar.artifacts.ArtifactDamagingItem;
import jinrui.mcdar.enums.DamagingArtifactID;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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

    public abstract boolean canFire(Player playerEntity, ItemStack itemStack);

    public static ItemStack getBeacon(Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof AbstractBeaconItem)) {
            heldItem = player.getOffhandItem();
            if (!(heldItem.getItem() instanceof AbstractBeaconItem)) {
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }

    public abstract BeaconBeamColor getBeamColor();

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand){
        ItemStack itemStack = user.getItemInHand(hand);

        if (canFire(user, itemStack)){
            user.playSound(
                    SoundEvents.BEACON_ACTIVATE,
                    1.0f,
                    1.0f
            );
        } else {
            return new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);
        }

        if (!world.isClientSide) {
            user.startUsingItem(hand);
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);
    }

    @Override
    public int getUseDuration(ItemStack stack){
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack){
        return UseAnim.NONE;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks){
        user.playSound(
                SoundEvents.BEACON_DEACTIVATE,
                1.0f,
                1.0f
        );
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks){
        if (user instanceof Player playerEntity){

            if (playerEntity.isCreative() || this.consumeTick(playerEntity)) {
                HitResult result = playerEntity.pick(RAYTRACE_DISTANCE, 1.0f, false);
                Vec3 eyeVector = playerEntity.getEyePosition(1.0f);
                Vec3 lookVector = playerEntity.getLookAngle();
                Vec3 targetVector = eyeVector.add(lookVector.x * RAYTRACE_DISTANCE, lookVector.y * RAYTRACE_DISTANCE
                        , lookVector.z * RAYTRACE_DISTANCE);
                AABB box = playerEntity.getBoundingBox().expandTowards(lookVector.scale(RAYTRACE_DISTANCE)).inflate(1.0D,
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

    protected abstract boolean consumeTick(Player playerEntity);
}
