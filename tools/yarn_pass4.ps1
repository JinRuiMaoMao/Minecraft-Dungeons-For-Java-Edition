$root = Join-Path $PSScriptRoot "..\common\src\main\java" | Resolve-Path
$repl = @(
  @{ Old = 'BuiltInRegistries'; New = 'Registries' }
  @{ Old = 'import net.minecraft.enchantment.EnchantmentCategory;'; New = 'import net.minecraft.enchantment.EnchantmentTarget;' }
  @{ Old = 'EnchantmentCategory '; New = 'EnchantmentTarget ' }
  @{ Old = 'import net.minecraft.enchantment.EnchantmentInstance;'; New = 'import net.minecraft.enchantment.EnchantmentLevelEntry;' }
  @{ Old = 'EnchantmentInstance'; New = 'EnchantmentLevelEntry' }
  @{ Old = 'import net.minecraft.nbt.CompoundTag;'; New = 'import net.minecraft.nbt.NbtCompound;' }
  @{ Old = 'CompoundTag '; New = 'NbtCompound ' }
  @{ Old = 'CompoundTag)'; New = 'NbtCompound)' }
  @{ Old = 'import net.minecraft.entity.passive.Bee;'; New = 'import net.minecraft.entity.passive.BeeEntity;' }
  @{ Old = 'extends Bee '; New = 'extends BeeEntity ' }
  @{ Old = 'instanceof Bee '; New = 'instanceof BeeEntity ' }
)
Get-ChildItem $root -Filter *.java -Recurse | ForEach-Object {
  $t = [IO.File]::ReadAllText($_.FullName)
  $o = $t
  foreach ($r in $repl) { $t = $t.Replace($r.Old, $r.New) }
  if ($t -ne $o) { [IO.File]::WriteAllText($_.FullName, $t) }
}
Write-Host pass4 ok
