# Remaining Mojmap class/package names -> Yarn 1.20.1 (apply to common Java sources)
$root = Join-Path $PSScriptRoot "..\common\src\main\java" | Resolve-Path

$replacements = @(
  @{ Old = 'VillagerProfession'; New = '___VILLPROF___' }
)

Get-ChildItem -Path $root -Filter *.java -Recurse | ForEach-Object {
  $text = [IO.File]::ReadAllText($_.FullName)
  foreach ($r in $replacements) { $text = $text.Replace($r.Old, $r.New) }
  [IO.File]::WriteAllText($_.FullName, $text)
}

$lineReplacements = @(
  @{ Old = 'import net.minecraft.entity.OwnableEntity;'; New = 'import net.minecraft.entity.passive.TameableEntity;' }
  @{ Old = 'instanceof OwnableEntity'; New = 'instanceof TameableEntity' }
  @{ Old = 'OwnableEntity tameable'; New = 'TameableEntity tameable' }
  @{ Old = 'import net.minecraft.entity.passive.IronGolem;'; New = 'import net.minecraft.entity.passive.IronGolemEntity;' }
  @{ Old = 'instanceof IronGolem'; New = 'instanceof IronGolemEntity' }
  @{ Old = 'import net.minecraft.village.Villager;'; New = 'import net.minecraft.village.VillagerEntity;' }
  @{ Old = 'instanceof Villager '; New = 'instanceof VillagerEntity ' }
  @{ Old = 'instanceof Villager)'; New = 'instanceof VillagerEntity)' }
  @{ Old = 'import net.minecraft.potion.PotionUtils;'; New = 'import net.minecraft.potion.PotionUtil;' }
  @{ Old = 'PotionUtils.'; New = 'PotionUtil.' }
  @{ Old = 'import net.minecraft.entity.AreaEffectCloud;'; New = 'import net.minecraft.entity.AreaEffectCloudEntity;' }
  @{ Old = 'AreaEffectCloud '; New = 'AreaEffectCloudEntity ' }
  @{ Old = 'import net.minecraft.particle.ParticleOptions;'; New = 'import net.minecraft.particle.ParticleEffect;' }
  @{ Old = 'ParticleOptions '; New = 'ParticleEffect ' }
  @{ Old = 'import net.minecraft.network.syncher.EntityDataAccessor;'; New = 'import net.minecraft.entity.data.TrackedData;' }
  @{ Old = 'EntityDataAccessor<'; New = 'TrackedData<' }
  @{ Old = 'import net.minecraft.entity.LightningBolt;'; New = 'import net.minecraft.entity.LightningEntity;' }
  @{ Old = 'LightningBolt '; New = 'LightningEntity ' }
  @{ Old = 'import net.minecraft.entity.passive.MushroomCow;'; New = 'import net.minecraft.entity.passive.MooshroomEntity;' }
  @{ Old = 'instanceof MushroomCow'; New = 'instanceof MooshroomEntity' }
  @{ Old = 'MushroomCow '; New = 'MooshroomEntity ' }
  @{ Old = 'import net.minecraft.entity.passive.Pig;'; New = 'import net.minecraft.entity.passive.PigEntity;' }
  @{ Old = 'instanceof Pig '; New = 'instanceof PigEntity ' }
  @{ Old = 'Pig '; New = 'PigEntity ' }
  @{ Old = 'import net.minecraft.entity.passive.Turtle;'; New = 'import net.minecraft.entity.passive.TurtleEntity;' }
  @{ Old = 'instanceof Turtle '; New = 'instanceof TurtleEntity ' }
  @{ Old = 'Turtle '; New = 'TurtleEntity ' }
  @{ Old = 'import net.minecraft.entity.mob.Creeper;'; New = 'import net.minecraft.entity.mob.CreeperEntity;' }
  @{ Old = 'instanceof Creeper '; New = 'instanceof CreeperEntity ' }
  @{ Old = 'instanceof Creeper)'; New = 'instanceof CreeperEntity)' }
  @{ Old = 'Creeper creeperEntity'; New = 'CreeperEntity creeperEntity' }
  @{ Old = 'Villager villagerEntity'; New = 'VillagerEntity villagerEntity' }
  @{ Old = 'import net.minecraft.entity.projectile.AbstractArrow;'; New = 'import net.minecraft.entity.projectile.PersistentProjectileEntity;' }
  @{ Old = 'AbstractArrow '; New = 'PersistentProjectileEntity ' }
  @{ Old = 'import net.minecraft.entity.Mob;'; New = 'import net.minecraft.entity.mob.MobEntity;' }
  @{ Old = 'import net.minecraft.entity.Sheep;'; New = 'import net.minecraft.entity.passive.SheepEntity;' }
  @{ Old = 'extends Sheep'; New = 'extends SheepEntity' }
  @{ Old = 'import net.minecraft.entity.passive.Sheep;'; New = 'import net.minecraft.entity.passive.SheepEntity;' }
  @{ Old = 'EntityType<? extends Sheep>'; New = 'EntityType<? extends SheepEntity>' }
  @{ Old = 'import net.minecraft.entity.ai.pathing.BlockPathTypes;'; New = 'import net.minecraft.entity.ai.pathing.PathNodeType;' }
  @{ Old = 'BlockPathTypes'; New = 'PathNodeType' }
  @{ Old = 'import net.minecraft.entity.ai.pathing.WalkNodeEvaluator;'; New = 'import net.minecraft.entity.ai.pathing.LandPathNodeMaker;' }
  @{ Old = 'WalkNodeEvaluator'; New = 'LandPathNodeMaker' }
  @{ Old = 'import net.minecraft.item.Rarity;'; New = 'import net.minecraft.util.Rarity;' }
  @{ Old = 'import net.minecraft.loot.entry.LootItem;'; New = 'import net.minecraft.loot.entry.ItemEntry;' }
  @{ Old = 'LootItem.'; New = 'ItemEntry.' }
  @{ Old = 'import net.minecraft.loot.BuiltInLootTables;'; New = 'import net.minecraft.loot.LootTables;' }
  @{ Old = 'BuiltInLootTables'; New = 'LootTables' }
  @{ Old = 'import net.minecraft.util.ActionResultHolder;'; New = 'import net.minecraft.util.TypedActionResult;' }
  @{ Old = 'ActionResultHolder<'; New = 'TypedActionResult<' }
  @{ Old = 'new ActionResultHolder'; New = 'new TypedActionResult' }
  @{ Old = 'import net.minecraft.item.UseAnim;'; New = 'import net.minecraft.util.UseAction;' }
  @{ Old = 'UseAnim'; New = 'UseAction' }
  @{ Old = 'import net.minecraft.world.phys.AABB;'; New = 'import net.minecraft.util.math.Box;' }
  @{ Old = 'new AABB('; New = 'new Box(' }
  @{ Old = 'import net.minecraft.world.phys.Vec3;'; New = 'import net.minecraft.util.math.Vec3d;' }
  @{ Old = 'import net.minecraft.world.phys.HitResult;'; New = 'import net.minecraft.util.hit.HitResult;' }
  @{ Old = 'import net.minecraft.world.phys.EntityHitResult;'; New = 'import net.minecraft.util.hit.EntityHitResult;' }
  @{ Old = 'import net.minecraft.world.phys.Vec3i;'; New = 'import net.minecraft.util.math.Vec3i;' }
  @{ Old = 'import net.minecraft.item.UseOnContext;'; New = 'import net.minecraft.item.ItemUsageContext;' }
  @{ Old = 'UseOnContext '; New = 'ItemUsageContext ' }
  @{ Old = 'import net.minecraft.item.TooltipFlag;'; New = 'import net.minecraft.item.tooltip.TooltipContext;' }
  @{ Old = 'TooltipFlag '; New = 'TooltipContext ' }
  @{ Old = 'import net.minecraft.world.InteractionHand;'; New = 'import net.minecraft.util.Hand;' }
  @{ Old = 'InteractionHand '; New = 'Hand ' }
  @{ Old = 'import net.minecraft.world.InteractionResult;'; New = 'import net.minecraft.util.ActionResult;' }
  @{ Old = 'InteractionResult '; New = 'ActionResult ' }
  @{ Old = 'import net.minecraft.world.InteractionResultHolder;'; New = 'import net.minecraft.util.TypedActionResult;' }
  @{ Old = 'InteractionResultHolder<'; New = 'TypedActionResult<' }
  @{ Old = 'import net.minecraft.server.level.ServerLevel;'; New = 'import net.minecraft.server.world.ServerWorld;' }
  @{ Old = 'ServerLevel '; New = 'ServerWorld ' }
  @{ Old = 'ServerLevel)'; New = 'ServerWorld)' }
  @{ Old = 'ServerLevel,'; New = 'ServerWorld,' }
  @{ Old = 'import net.minecraft.world.level.Level;'; New = 'import net.minecraft.world.World;' }
  @{ Old = 'Level world'; New = 'World world' }
  @{ Old = '(Level '; New = '(World ' }
  @{ Old = ', Level '; New = ', World ' }
  @{ Old = '<Level>'; New = '<World>' }
)

Get-ChildItem -Path $root -Filter *.java -Recurse | ForEach-Object {
  $text = [IO.File]::ReadAllText($_.FullName)
  $orig = $text
  foreach ($r in $lineReplacements) { $text = $text.Replace($r.Old, $r.New) }
  # Method Mojmap -> Yarn (word-ish)
  $text = $text.Replace('getCommandSenderWorld()', 'getWorld()')
  $text = $text.Replace('blockPosition()', 'getBlockPos()')
  $text = $text.Replace('.inflate(', '.expand(')
  $text = $text.Replace('.addEffect(', '.addStatusEffect(')
  $text = $text.Replace('.level()', '.getWorld()')
  $text = $text.Replace('.addFreshEntity(', '.spawnEntity(')
  $text = $text.Replace('.getEntityData()', '.getDataTracker()')
  $text = $text.Replace('StatusEffects.MOVEMENT_SLOWDOWN', 'StatusEffects.SLOWNESS')
  $text = $text.Replace('StatusEffects.CONFUSION', 'StatusEffects.NAUSEA')
  $text = $text -replace '\bMob\b(?![a-zA-Z])', 'MobEntity'
  if ($text -ne $orig) { [IO.File]::WriteAllText($_.FullName, $text) }
}

# Restore VillagerProfession
Get-ChildItem -Path $root -Filter *.java -Recurse | ForEach-Object {
  $text = [IO.File]::ReadAllText($_.FullName)
  $n = $text.Replace('___VILLPROF___', 'VillagerProfession')
  if ($n -ne $text) { [IO.File]::WriteAllText($_.FullName, $n) }
}

Write-Host "pass3 done"
