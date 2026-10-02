package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.compiler.gen.asm.helpers.ASMUtils;
import net.sandius.rembulan.compiler.gen.asm.helpers.BoxedPrimitivesMethods;
import net.sandius.rembulan.impl.PackedConstants;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

/**
 * The constants of a compiled function, created once in the static initialiser of its class.
 *
 * <p>The first {@link #MAX_FIELDS} constants get a static field each. Further constants, and
 * long strings, are packed into the static array {@code _k} (see {@link PackedConstants}), so that
 * a large chunk exceeds neither the size limit of the static initialiser, nor that of the constant
 * pool, nor that of a string constant.</p>
 */
class CachedConstants {

	static final int MAX_FIELDS = 1000;
	static final int MAX_FIELD_STRING_LENGTH = 1024;

	private static final String ARRAY_FIELD_NAME = "_k";
	private static final Type ARRAY_TYPE = ASMUtils.arrayTypeFor(Object.class);

	private final Type ownerClassType;

	private final List<RunMethod.ConstFieldInstance> fields;
	private final Map<Object, RunMethod.ConstFieldInstance> fieldsByValue;

	private final List<Object> packed;
	private final Map<Object, Integer> packedIndices;

	CachedConstants(Type ownerClassType) {
		this.ownerClassType = Objects.requireNonNull(ownerClassType);
		this.fields = new ArrayList<>();
		this.fieldsByValue = new HashMap<>();
		this.packed = new ArrayList<>();
		this.packedIndices = new HashMap<>();
	}

	public boolean isEmpty() {
		return fields.isEmpty() && packed.isEmpty();
	}

	public InsnList accessInsns(Object value) {
		RunMethod.ConstFieldInstance cfi = fieldsByValue.get(value);
		if (cfi != null) {
			return cfi.accessInsns();
		}

		Integer idx = packedIndices.get(value);
		if (idx != null) {
			return packedAccessInsns(value, idx);
		}

		if (fields.size() < MAX_FIELDS && !isLongString(value)) {
			cfi = newConstFieldInstance(value, fields.size());
			fields.add(cfi);
			fieldsByValue.put(value, cfi);
			return cfi.accessInsns();
		}
		else {
			idx = packed.size();
			packed.add(value);
			packedIndices.put(value, idx);
			return packedAccessInsns(value, idx);
		}
	}

	private static boolean isLongString(Object value) {
		return value instanceof ByteString && ((ByteString) value).length() > MAX_FIELD_STRING_LENGTH;
	}

	private static Type typeOf(Object value) {
		if (value instanceof Long) return Type.getType(Long.class);
		else if (value instanceof Double) return Type.getType(Double.class);
		else if (value instanceof ByteString) return Type.getType(ByteString.class);
		else throw new UnsupportedOperationException("Illegal constant: " + value);
	}

	private RunMethod.ConstFieldInstance newConstFieldInstance(final Object value, int idx) {
		Objects.requireNonNull(value);

		String fieldName = "_k_" + idx;

		if (value instanceof Double || value instanceof Long) {
			return new RunMethod.ConstFieldInstance(value, fieldName, ownerClassType, typeOf(value)) {
				@Override
				public void doInstantiate(InsnList il) {
					il.add(BoxedPrimitivesMethods.loadBoxedConstant(value));
				}
			};
		}
		else if (value instanceof ByteString) {
			return new RunMethod.ConstFieldInstance(value, fieldName, ownerClassType, typeOf(value)) {
				@Override
				public void doInstantiate(InsnList il) {
					il.add(BytecodeEmitVisitor.newByteString((ByteString) value));
				}
			};
		}
		else {
			throw new UnsupportedOperationException("Illegal constant: " + value);
		}
	}

	private InsnList packedAccessInsns(Object value, int idx) {
		InsnList il = new InsnList();
		il.add(new FieldInsnNode(
				GETSTATIC,
				ownerClassType.getInternalName(),
				ARRAY_FIELD_NAME,
				ARRAY_TYPE.getDescriptor()));
		il.add(ASMUtils.loadIntWithoutConstant(idx));
		il.add(new InsnNode(AALOAD));
		il.add(new TypeInsnNode(CHECKCAST, typeOf(value).getInternalName()));
		return il;
	}

	public List<FieldNode> fieldNodes() {
		List<FieldNode> result = new ArrayList<>();
		for (RunMethod.ConstFieldInstance cfi : fields) {
			result.add(cfi.fieldNode());
		}
		if (!packed.isEmpty()) {
			result.add(new FieldNode(
					ACC_PRIVATE + ACC_STATIC + ACC_FINAL,
					ARRAY_FIELD_NAME,
					ARRAY_TYPE.getDescriptor(),
					null,
					null));
		}
		return result;
	}

	public InsnList instantiateInsns() {
		InsnList il = new InsnList();

		for (RunMethod.ConstFieldInstance cfi : fields) {
			il.add(cfi.instantiateInsns());
		}

		if (!packed.isEmpty()) {
			List<String> chunks = PackedConstants.pack(packed);
			il.add(ASMUtils.loadInt(chunks.size()));
			il.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(String.class)));
			for (int i = 0; i < chunks.size(); i++) {
				il.add(new InsnNode(DUP));
				il.add(ASMUtils.loadInt(i));
				il.add(new LdcInsnNode(chunks.get(i)));
				il.add(new InsnNode(AASTORE));
			}
			il.add(new MethodInsnNode(
					INVOKESTATIC,
					Type.getInternalName(PackedConstants.class),
					"unpack",
					Type.getMethodDescriptor(ARRAY_TYPE, ASMUtils.arrayTypeFor(String.class)),
					false));
			il.add(new FieldInsnNode(
					PUTSTATIC,
					ownerClassType.getInternalName(),
					ARRAY_FIELD_NAME,
					ARRAY_TYPE.getDescriptor()));
		}

		return il;
	}

}
