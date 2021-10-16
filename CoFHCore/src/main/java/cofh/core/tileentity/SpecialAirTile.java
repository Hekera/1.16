package cofh.core.tileentity;

import cofh.core.block.nyi.SpecialAirBlock;
import cofh.lib.util.AreaUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static cofh.lib.util.references.CoreReferences.SPECIAL_AIR_TILE;
import static cofh.core.block.nyi.SpecialAirBlock.LIGHT;

public class SpecialAirTile extends TileEntity {

    protected byte power;
    protected HashMap<String, Integer> configs = new HashMap<>();

    public SpecialAirTile() {

        super(SPECIAL_AIR_TILE);
    }

    public int getPower() {

        return power;
    }

    public void addConfig(SpecialAirConfig config) {

        this.configs.put(config.name, this.configs.getOrDefault(config.name, 0) + 1);
        BlockState state = this.world.getBlockState(this.pos);
        if ((state.getBlock() instanceof SpecialAirBlock) && (state.get(LIGHT) <= config.light)) {
            this.world.setBlockState(this.pos, state.with(LIGHT, (int) config.light));
        }
        this.power = (byte) Math.max(config.power, this.power);
    }

    public void removeConfig(SpecialAirConfig config) {

        if (!this.configs.containsKey(config.name)) {
            return;
        }

        int supporters = this.configs.get(config.name) - 1;
        if (supporters < 1) {
            this.configs.remove(config.name);
            if (this.configs.isEmpty()) {
                revert();
            }
            else {
                BlockState state = this.world.getBlockState(this.pos);
                if ((state.getBlock() instanceof SpecialAirBlock) && (state.get(LIGHT) <= config.light)) {
                    this.world.setBlockState(this.pos, state.with(LIGHT, getMaxLight()));
                }
                if (config.power >= this.power) {
                    this.power = getMaxPower();
                }
            }
        }
        else {
            this.configs.put(config.name, supporters);
        }
    }

    public void revert() {
        this.world.setBlockState(pos, Blocks.AIR.getDefaultState());
        this.world.removeTileEntity(this.pos);
        this.remove();
    }

    public int getMaxLight() {

        return this.configs.keySet().stream().map((config) -> AreaUtils.airConfigs.get(config).light)
                .max(Comparator.naturalOrder()).orElse((byte) 0);
    }

    public byte getMaxPower() {

        return this.configs.keySet().stream().map((config) -> AreaUtils.airConfigs.get(config).power)
                .max(Comparator.naturalOrder()).orElse((byte) 0);
    }

    public List<AreaUtils.IEffectApplier> getEffects() {

        return this.configs.keySet().stream().map((config) -> AreaUtils.airConfigs.get(config).effect)
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<IParticleData> getParticles() {

        return this.configs.keySet().stream().map((config) -> AreaUtils.airConfigs.get(config).particle)
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    // region NBT
    @Override
    public void read(BlockState state, CompoundNBT nbt) {

        super.read(state, nbt);
        nbt.getCompound("configs").keySet().forEach((config) -> this.configs.put(config, nbt.getInt(config)));
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {

        super.write(nbt);
        writeConfigs(nbt);
        return nbt;
    }

    protected void writeConfigs(CompoundNBT nbt) {

        CompoundNBT configsNBT = new CompoundNBT();
        this.configs.forEach(configsNBT::putInt);
        nbt.put("configs", configsNBT);
    }
    // endregion

    public static class SpecialAirConfig {

        public final String name;
        public final byte light;
        public final byte power;
        public final AreaUtils.IEffectApplier effect;
        public final IParticleData particle;

        public SpecialAirConfig(String name, byte light, byte power, @Nullable AreaUtils.IEffectApplier effect, @Nullable IParticleData particle) {

            this.name = name;
            this.light = light;
            this.power = power;
            this.effect = effect;
            this.particle = particle;
        }
    }
}
