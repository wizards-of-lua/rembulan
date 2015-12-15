package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Cons;

/*
 Properties:

   1) For any coroutine c,
         (c.resuming == null || c.resuming.yieldingTo == c) && (c.yieldingTo == null || c.yieldingTo.resuming == c)
         (i.e. coroutines form a doubly-linked list)

   2) if c is the currently-running coroutine, then c.resuming == null

   3) if c is the main coroutine, then c.yieldingTo == null

 Coroutine d can be resumed from c iff
   c != d && d.resuming == null && d.yieldingTo == null

 This means that
   c.resuming = d
   d.yieldingTo = c
 */
public class Coroutine {

	protected final LuaState state;

	// paused call stack: up-to-date only iff coroutine is not running
	protected Cons<CallInfo> callStack;

	protected final ObjectStack objectStack;

	protected Coroutine yieldingTo;
	protected Coroutine resuming;

	public Coroutine(LuaState state, ObjectStack objectStack) {
		Check.notNull(state);
		Check.notNull(objectStack);

		this.state = state;
		this.objectStack = objectStack;
	}

	public LuaState getOwnerState() {
		return state;
	}

	public ObjectStack getObjectStack() {
		return objectStack;
	}

	@Override
	public String toString() {
		return "thread: 0x" + Integer.toHexString(hashCode());
	}

}