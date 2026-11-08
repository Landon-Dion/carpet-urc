package com.urc.block;

import com.urc.URCBlockEntities;
import com.urc.URCSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Stores the two slab materials of a mixed slab. */
public class MixedSlabBlockEntity extends BlockEntity {
	private BlockState bottom = Blocks.AIR.defaultBlockState();
	private BlockState top = Blocks.AIR.defaultBlockState();

	public MixedSlabBlockEntity(BlockPos pos, BlockState state) {
		super(URCBlockEntities.MIXED_SLAB, pos, state);
	}

	public void setHalves(BlockState bottom, BlockState top) {
		this.bottom = bottom;
		this.top = top;
		this.setChanged();
	}

	public BlockState getBottom() {
		return this.bottom;
	}

	public BlockState getTop() {
		return this.top;
	}

	public boolean isEmpty() {
		return this.bottom.isAir() || this.top.isAir();
	}

	@Override
	public void setRemoved() {
		if (this.level != null && !this.level.isClientSide() && URCSettings.mixedSlab) {
			MixedSlabStash.put(this.level, this.worldPosition, this.bottom, this.top);
		}

		super.setRemoved();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("bottom", idOf(this.bottom));
		output.putString("top", idOf(this.top));
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.bottom = stateOf(input.getStringOr("bottom", "minecraft:air"));
		this.top = stateOf(input.getStringOr("top", "minecraft:air"));
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return this.saveCustomOnly(registries);
	}

	private static String idOf(BlockState state) {
		return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
	}

	private static BlockState stateOf(String id) {
		Identifier identifier = Identifier.tryParse(id);

		if (identifier == null) {
			return Blocks.AIR.defaultBlockState();
		}

		Block block = BuiltInRegistries.BLOCK.getValue(identifier);
		return block == null ? Blocks.AIR.defaultBlockState() : block.defaultBlockState();
	}
}
