package jinrui.mcdar.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ProjectileEffectHelper {
    public static void ricochetArrowLikeShield(PersistentProjectileEntity ppe){
        ppe.setDeltaMovement(ppe.getDeltaMovement().scale(-0.1D));
        ppe.getViewYRot(180.0F);
        ppe.yRotO += 180.0F;
        if (!ppe.getWorld().isClientSide && ppe.getDeltaMovement().lengthSqr() < 1.0E-7D){
            if (ppe.pickup == AbstractArrow.Pickup.ALLOWED){
                ppe.spawnAtLocation(new ItemStack(Items.ARROW), 0.1F);
            }
            ppe.remove(Entity.RemovalReason.KILLED);
        }
    }
    public static void flamingQuiverArrow(PersistentProjectileEntity ppe){
        ppe.setSecondsOnFire(100);
    }

}
