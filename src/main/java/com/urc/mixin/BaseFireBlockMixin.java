package com.urc.mixin;

import com.urc.URCSettings;
import com.urc.portal.CustomPortalFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets a lit fire inside a closed, non-rectangular obsidian loop create a nether portal.
 *
 * <p>Two vanilla gates have to be opened: {@code canBePlacedAt} decides whether flint and steel
 * is allowed to place the fire at all (it uses the rectangular shape check), and
 * {@code onPlace} is where the portal blocks are actually created.</p>
 *
 * <p>Portal blocks are only created on the server. Letting the client predict them as well means
 * two independent shape scans, and when the client world lags behind the two sides pick different
 * planes, leaving mis-oriented ghost blocks. The client just keeps the fire and waits for the
 * authoritative block updates.</p>
 */
@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockMixin {
	@Inject(method = "canBePlacedAt", at = @At("HEAD"), cancellable = true)
	private static void urc$allowCustomPortalIgnition(Level level, BlockPos pos, Direction face,
			CallbackInfoReturnable<Boolean> cir) {
		if (!URCSettings.customNetherPortal && !URCSettings.horizontalNetherPortal) {
			return;
		}

		CustomPortalFrame.clearPendingAxis();

		if (!CustomPortalFrame.inPortalDimension(level) || !level.getBlockState(pos).isAir()) {
			return;
		}

		// The fire sits on the plane of the clicked face, so the face pins the portal plane.
		// Clicking a ceiling/floor face suggests a flat portal when that rule is enabled.
		boolean allowVertical = URCSettings.customNetherPortal;
		boolean allowHorizontal = URCSettings.horizontalNetherPortal;
		Direction.Axis faceAxis = face.getAxis();
		Direction.Axis hint = faceAxis.isHorizontal() ? faceAxis : (allowHorizontal ? Direction.Axis.Y : null);

		CustomPortalFrame.Frame frame =
				CustomPortalFrame.findEmptyFrame(level, pos, hint, allowVertical, allowHorizontal);

		if (frame != null) {
			CustomPortalFrame.rememberPendingAxis(frame.axis());
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "onPlace", at = @At("HEAD"), cancellable = true)
	private void urc$createCustomPortal(BlockState state, Level level, BlockPos pos, BlockState oldState,
			boolean movedByPiston, CallbackInfo ci) {
		if ((!URCSettings.customNetherPortal && !URCSettings.horizontalNetherPortal)
				|| !CustomPortalFrame.inPortalDimension(level) || oldState.is(state.getBlock())) {
			return;
		}

		boolean allowVertical = URCSettings.customNetherPortal;
		boolean allowHorizontal = URCSettings.horizontalNetherPortal;
		Direction.Axis pending = CustomPortalFrame.takePendingAxis();

		if (level.isClientSide()) {
			// Keep the fire block; the server creates the portal and syncs it to us.
			if (CustomPortalFrame.findEmptyFrame(level, pos, pending, allowVertical, allowHorizontal) != null) {
				ci.cancel();
			}

			return;
		}

		if (CustomPortalFrame.tryCreate(level, pos, pending, allowVertical, allowHorizontal)) {
			ci.cancel();
		}
	}
}
