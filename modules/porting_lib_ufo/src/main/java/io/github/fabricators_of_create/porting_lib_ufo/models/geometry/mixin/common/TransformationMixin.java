package io.github.fabricators_of_create.porting_lib_ufo.models.geometry.mixin.common;

import com.mojang.math.Transformation;

import io.github.fabricators_of_create.porting_lib_ufo.models.geometry.extensions.TransformationExtensions;
import net.minecraft.core.Direction;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Transformation.class)
public abstract class TransformationMixin implements TransformationExtensions {
	@Shadow
	@Final
	private Matrix4f matrix;

	@Shadow
	public abstract Matrix4f getMatrix();

	@Unique
	private Matrix3f normalTransform = null;

	@Unique
	private void port_lib$checkNormalTransform() {
		if (normalTransform == null) {
			normalTransform = new Matrix3f(this.matrix);
			normalTransform.invert();
			normalTransform.transpose();
		}
	}

	@Override
	public Transformation applyOrigin(Vector3f origin) {
		if (isIdentity()) return Transformation.identity();

		Matrix4f ret = this.getMatrix();
		Matrix4f tmp = new Matrix4f().translation(origin.x(), origin.y(), origin.z());
		tmp.mul(ret, ret);
		tmp.translation(-origin.x(), -origin.y(), -origin.z());
		ret.mul(tmp);
		return new Transformation(ret);
	}

	@Override
	public Matrix3f getNormalMatrix() {
		port_lib$checkNormalTransform();
		return normalTransform;
	}

	@Override
	public void transformPosition(Vector4f position) {
		position.mul(this.getMatrix());
	}

	@Override
	public Direction rotateTransform(Direction facing) {
		return Direction.rotate(getMatrix(), facing);
	}

	@Override
	public boolean isIdentity() {
		return this.equals(Transformation.identity());
	}

	@Override
	public void transformNormal(Vector3f normal) {
		normal.mul(getNormalMatrix());
		normal.normalize();
	}

	@Override
	public Transformation blockCenterToCorner() {
		return applyOrigin(new Vector3f(.5f, .5f, .5f));
	}

	@Override
	public Transformation blockCornerToCenter() {
		return applyOrigin(new Vector3f(-.5f, -.5f, -.5f));
	}
}
