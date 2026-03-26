# Mojmap-style packages -> Yarn 1.20.1 packages (conservative: no broad type renames).
$root = Join-Path $PSScriptRoot "..\common\src\main\java" | Resolve-Path
$replacements = @(
  @{ Old = 'net.minecraft.server.level.ServerPlayer'; New = 'net.minecraft.server.network.ServerPlayerEntity' }
  @{ Old = 'net.minecraft.server.level.ServerLevel'; New = 'net.minecraft.server.world.ServerWorld' }
  @{ Old = 'net.minecraft.world.level.Level'; New = 'net.minecraft.world.World' }
  @{ Old = 'net.minecraft.world.entity.ai.attributes'; New = 'net.minecraft.entity.attribute' }
  @{ Old = 'net.minecraft.world.entity.animal.'; New = 'net.minecraft.entity.passive.' }
  @{ Old = 'net.minecraft.world.entity.monster.'; New = 'net.minecraft.entity.mob.' }
  @{ Old = 'net.minecraft.world.entity.item.'; New = 'net.minecraft.entity.' }
  @{ Old = 'net.minecraft.world.entity.decoration.'; New = 'net.minecraft.entity.decoration.' }
  @{ Old = 'net.minecraft.world.entity.projectile.'; New = 'net.minecraft.entity.projectile.' }
  @{ Old = 'net.minecraft.world.entity.npc.'; New = 'net.minecraft.village.' }
  @{ Old = 'net.minecraft.world.entity.player.'; New = 'net.minecraft.entity.player.' }
  @{ Old = 'net.minecraft.world.entity.'; New = 'net.minecraft.entity.' }
  @{ Old = 'net.minecraft.world.item.enchantment.'; New = 'net.minecraft.enchantment.' }
  @{ Old = 'net.minecraft.world.item.alchemy.'; New = 'net.minecraft.potion.' }
  @{ Old = 'net.minecraft.world.item.context.'; New = 'net.minecraft.item.' }
  @{ Old = 'net.minecraft.world.item.'; New = 'net.minecraft.item.' }
  @{ Old = 'net.minecraft.world.effect.'; New = 'net.minecraft.entity.effect.' }
  @{ Old = 'net.minecraft.world.damagesource.'; New = 'net.minecraft.entity.damage.' }
  @{ Old = 'net.minecraft.world.inventory.'; New = 'net.minecraft.screen.' }
  @{ Old = 'net.minecraft.world.level.storage.loot.predicates.'; New = 'net.minecraft.loot.condition.' }
  @{ Old = 'net.minecraft.world.level.storage.loot.providers.number.'; New = 'net.minecraft.loot.provider.number.' }
  @{ Old = 'net.minecraft.world.level.storage.loot.entries.'; New = 'net.minecraft.loot.entry.' }
  @{ Old = 'net.minecraft.world.level.storage.loot.functions.'; New = 'net.minecraft.loot.function.' }
  @{ Old = 'net.minecraft.world.level.storage.loot.'; New = 'net.minecraft.loot.' }
  @{ Old = 'net.minecraft.world.level.block.entity.'; New = 'net.minecraft.block.entity.' }
  @{ Old = 'net.minecraft.world.level.block.state.'; New = 'net.minecraft.block.' }
  @{ Old = 'net.minecraft.world.level.block.'; New = 'net.minecraft.block.' }
  @{ Old = 'net.minecraft.world.level.gameevent.'; New = 'net.minecraft.world.event.' }
  @{ Old = 'net.minecraft.world.level.material.'; New = 'net.minecraft.block.' }
  @{ Old = 'net.minecraft.world.level.pathfinder.'; New = 'net.minecraft.entity.ai.pathing.' }
  @{ Old = 'net.minecraft.world.level.levelgen.'; New = 'net.minecraft.world.gen.' }
  @{ Old = 'net.minecraft.world.level.chunk.'; New = 'net.minecraft.world.chunk.' }
  @{ Old = 'net.minecraft.world.level.'; New = 'net.minecraft.world.' }
  @{ Old = 'net.minecraft.core.registries.BuiltInRegistries'; New = 'net.minecraft.registry.Registries' }
  @{ Old = 'net.minecraft.core.particles.'; New = 'net.minecraft.particle.' }
  @{ Old = 'net.minecraft.core.BlockPos'; New = 'net.minecraft.util.math.BlockPos' }
  @{ Old = 'net.minecraft.core.Direction'; New = 'net.minecraft.util.math.Direction' }
  @{ Old = 'net.minecraft.core.Vec3i'; New = 'net.minecraft.util.math.Vec3i' }
  @{ Old = 'net.minecraft.core.Vec3'; New = 'net.minecraft.util.math.Vec3d' }
  @{ Old = 'net.minecraft.core.Registry'; New = 'net.minecraft.registry.Registry' }
  @{ Old = 'net.minecraft.core.Position'; New = 'net.minecraft.util.math.Position' }
  @{ Old = 'net.minecraft.core.NonNullList'; New = 'net.minecraft.util.collection.DefaultedList' }
  @{ Old = 'net.minecraft.network.chat.'; New = 'net.minecraft.text.' }
  @{ Old = 'net.minecraft.client.resources.language.'; New = 'net.minecraft.client.resource.language.' }
  @{ Old = 'net.minecraft.sounds.'; New = 'net.minecraft.sound.' }
  @{ Old = 'net.minecraft.resources.ResourceLocation'; New = 'net.minecraft.util.Identifier' }
  @{ Old = 'net.minecraft.ChatFormatting'; New = 'net.minecraft.util.Formatting' }
  @{ Old = 'net.minecraft.world.InteractionHand'; New = 'net.minecraft.util.Hand' }
  @{ Old = 'net.minecraft.world.InteractionResult'; New = 'net.minecraft.util.ActionResult' }
  @{ Old = 'net.minecraft.world.InteractionResultHolder'; New = 'net.minecraft.util.TypedActionResult' }
  @{ Old = 'net.minecraft.util.RandomSource'; New = 'net.minecraft.util.math.random.Random' }
  @{ Old = 'net.minecraft.util.Mth'; New = 'net.minecraft.util.math.MathHelper' }
  @{ Old = 'net.minecraft.advancements.critereon.'; New = 'net.minecraft.predicate.' }
)

Get-ChildItem -Path $root -Filter *.java -Recurse | ForEach-Object {
  $text = [System.IO.File]::ReadAllText($_.FullName)
  $orig = $text
  foreach ($r in $replacements) {
    $text = $text.Replace($r.Old, $r.New)
  }
  $text = $text.Replace('new ResourceLocation(', 'new Identifier(')
  $text = $text -replace '\bResourceLocation\b', 'Identifier'
  $text = $text -replace '\bChatFormatting\b', 'Formatting'
  $text = $text -replace '\bMobEffectInstance\b', 'StatusEffectInstance'
  $text = $text -replace '\bMobEffectCategory\b', 'StatusEffectCategory'
  $text = $text -replace '\bMobEffects\b', 'StatusEffects'
  $text = $text -replace '\bMobEffect\b', 'StatusEffect'
  $text = $text -replace '\bComponent\b', 'Text'
  if ($text -ne $orig) {
    [System.IO.File]::WriteAllText($_.FullName, $text)
  }
}
Write-Host "Done packages under $root"
