package jinrui.mcdar.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ProjectileEffectHelper {
    public static void ricochetArrowLikeShield(AbstractArrow ppe){
        ppe.setDeltaMovement(ppe.getDeltaMovement().scale(-0.1D));
        ppe.getViewYRot(180.0F);
        ppe.yRotO += 180.0F;
        if (!ppe.level().isClientSide && ppe.getDeltaMovement().lengthSqr() < 1.0E-7D){
            if (ppe.pickup == AbstractArrow.Pickup.ALLOWED){
                ppe.spawnAtLocation(new ItemStack(Items.ARROW), 0.1F);
            }
            ppe.remove(Entity.RemovalReason.KILLED);
        }
    }
    public static void flamingQuiverArrow(AbstractArrow ppe){
        ppe.setSecondsOnFire(100);
    }

}
