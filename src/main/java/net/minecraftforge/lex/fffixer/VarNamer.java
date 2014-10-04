package net.minecraftforge.lex.fffixer;

import java.util.HashMap;
import java.util.Iterator;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class VarNamer implements IClassProcessor {

    private final FFFixerImpl inst;

    public VarNamer(FFFixerImpl inst) {
        this.inst = inst;
    }

    @Override
    public void process(ClassNode node) {
        if (node.name.equals("aa")) {
            fix_aa(node);
        }
        if (node.name.equals("bR")) {
            fix_bR(node);
        }
        if (node.name.equals("ce")) {
            fix_ce(node);
        }
        if (node.name.equals("de")) {
            fix_de(node);
        }
    }

    private void fix_aa(ClassNode node) {
        MethodNode method = FFFixerImpl.getMethod(node, "a", "(I)Ljava/lang/String;");
        Iterator<AbstractInsnNode> iter = method.instructions.iterator();
        while (iter.hasNext()) {
            AbstractInsnNode insn = iter.next();
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInvoke = (MethodInsnNode) insn;

                if (methodInvoke.owner.equals("de") && methodInvoke.name.equals("<init>")) { // Add more validation?
                    InsnList added = new InsnList();
                    added.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    added.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "aa", "g", "()LQ;", false));
                    added.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "bB", "b", "(LQ;)Ljava/lang/String;", false));
                    added.add(new InsnNode(Opcodes.ICONST_0));
                    methodInvoke.desc = "(IILjava/lang/String;Z)V";
                    method.instructions.insertBefore(insn, added);
                    inst.setWorkDone();
                    break;
                }
            }
        }
    }

    private void fix_bR(ClassNode node) {
        MethodNode method = FFFixerImpl.getMethod(node, "a", "(LaK;Lbn;Ljava/io/BufferedWriter;I)Z");
        Iterator<AbstractInsnNode> iter = method.instructions.iterator();

        int hit = 0;
        while (iter.hasNext()) {
            AbstractInsnNode insn = iter.next();
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInvoke = (MethodInsnNode) insn;

                if (methodInvoke.owner.equals("de") && methodInvoke.name.equals("<init>")) { // Add more validation?
                    if (++hit == 2) {
                        methodInvoke.desc = "(IILjava/lang/String;Z)V";
                        method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 9));
                        method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ILOAD, 27));
                        inst.setWorkDone();
                        break;
                    }
                }
            }
        }
    }

    private void fix_ce(ClassNode node) {
        node.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "varHelper", Type.getDescriptor(VarHelper.class), null, null));

        MethodNode constructor = FFFixerImpl.getMethod(node, "<init>", "()V");
        Iterator<AbstractInsnNode> iter = constructor.instructions.iterator();
        while (iter.hasNext()) {
            AbstractInsnNode insn = iter.next();
            if (insn.getOpcode() == Opcodes.RETURN) {
                InsnList added = new InsnList();
                added.add(new VarInsnNode(Opcodes.ALOAD, 0));
                added.add(new TypeInsnNode(Opcodes.NEW, Type.getInternalName(VarHelper.class)));
                added.add(new InsnNode(Opcodes.DUP));
                added.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, Type.getInternalName(VarHelper.class), "<init>", "()V", false));
                added.add(new FieldInsnNode(Opcodes.PUTFIELD, "ce", "varHelper", Type.getDescriptor(VarHelper.class)));

                constructor.instructions.insertBefore(insn, added);
                inst.setWorkDone();
                break;
            }
        }

        MethodNode method = FFFixerImpl.getMethod(node, "b", "(Lde;)Ljava/lang/String;");

        Iterator<AbstractInsnNode> itr = method.instructions.iterator();
        while (itr.hasNext()) {
            AbstractInsnNode insn = itr.next();

            // Final check for String before returning
            if (insn.getOpcode() == Opcodes.CHECKCAST) {
                InsnList newInsns = new InsnList();

                newInsns.add(new VarInsnNode(Opcodes.ASTORE, 2)); // Store read value to #2

                newInsns.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                newInsns.add(new FieldInsnNode(Opcodes.GETFIELD, "ce", "varHelper", Type.getDescriptor(VarHelper.class))); // varHelper
                newInsns.add(new VarInsnNode(Opcodes.ALOAD, 2)); // Load saved value

                // Load type value
                newInsns.add(new VarInsnNode(Opcodes.ALOAD, 1));
                newInsns.add(new FieldInsnNode(Opcodes.GETFIELD, "de", "type", Type.getDescriptor(String.class)));
                newInsns.add(new VarInsnNode(Opcodes.ALOAD, 1));
                newInsns.add(new FieldInsnNode(Opcodes.GETFIELD, "de", "varargs", Type.BOOLEAN_TYPE.getDescriptor()));

                // Stack: this, name, type
                newInsns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(VarHelper.class), "help",
                        Type.getMethodDescriptor(Type.getType(String.class), Type.getType(String.class), Type.getType(String.class), Type.BOOLEAN_TYPE), false));

                // Store value back
                newInsns.add(new VarInsnNode(Opcodes.ASTORE, 2));
                newInsns.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                newInsns.add(new FieldInsnNode(Opcodes.GETFIELD, "ce", "a", Type.getDescriptor(HashMap.class)));
                newInsns.add(new VarInsnNode(Opcodes.ALOAD, 1));
                newInsns.add(new VarInsnNode(Opcodes.ALOAD, 2));
                newInsns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(HashMap.class), "put",
                        Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Object.class), Type.getType(Object.class)), false));

                newInsns.add(new InsnNode(Opcodes.POP));
                newInsns.add(new VarInsnNode(Opcodes.ALOAD, 2));

                method.instructions.insert(insn, newInsns);
                inst.setWorkDone();
            }
        }
    }

    private void fix_de(ClassNode node) {
        node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "type", Type.getDescriptor(String.class), null, null));
        node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "varargs", Type.BOOLEAN_TYPE.getDescriptor(), null, null));

        MethodNode constructor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.getType(String.class), Type.BOOLEAN_TYPE), null, null);
        constructor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        constructor.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
        constructor.instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
        constructor.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "de", "<init>", "(II)V", false));
        constructor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        constructor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
        constructor.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "de", "type", Type.getDescriptor(String.class)));
        constructor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        constructor.instructions.add(new VarInsnNode(Opcodes.ILOAD, 4));
        constructor.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "de", "varargs", Type.BOOLEAN_TYPE.getDescriptor()));
        constructor.instructions.add(new InsnNode(Opcodes.RETURN));
        node.methods.add(constructor);
        inst.setWorkDone();
    }
}
