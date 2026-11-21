package net.sandius.rembulan.runtime;

/**
 * Charges the ticks of work that a library function or an operator does in Java, in a loop that
 * can't pause, such as matching a pattern, building a long string or reading a file.
 *
 * <p>A call uses one meter for all its work. The meter counts the work in units, 1200 to a tick,
 * and every 50 ticks it registers them with {@link ExecutionContext#registerTicksUnpausable(int)},
 * where the scheduler may stop the work by throwing an exception. Before its first registration,
 * it registers 0 ordinary ticks with {@link ExecutionContext#registerTicks(int)}: so the
 * unpausable ticks since the last ordinary registration are those of this call alone, and work
 * done before the call (in the same piece of compiled code) isn't counted as the call's. Short
 * calls never register anything.</p>
 *
 * <p>The costs are fixed, measured so that a tick stands for about 200 ns of work, like a few
 * operations of compiled Lua code: on JDK 21 (x86-64, warm JIT) with a scheduler that only counts
 * the ticks, each kind of work comes out at 100-350 ns per tick. Within a kind, some inputs cost
 * less per unit than others; their work is charged more ticks than it takes, not fewer.</p>
 */
public final class WorkMeter {

	/** The units of work that make a tick: the costs below are in units. */
	public static final int UNITS_PER_TICK = 1200;

	/**
	 * A step of a pattern match: trying one pattern item at one position, or scanning one
	 * character for a repeated item (20 per tick).
	 */
	public static final int PATTERN_STEP = 60;

	/** Pattern steps that trying a match at one more start position costs. */
	public static final int PATTERN_STEPS_PER_POSITION = 2;

	/** Pattern steps that turning a match into a result costs in gsub. */
	public static final int PATTERN_STEPS_PER_RESULT = 6;

	/** A byte copied into a string being built, or by the {@code ..} operator (400 per tick). */
	public static final int BYTE = 3;

	/** An append to a string being built, besides its bytes (12 per tick). */
	public static final int APPEND = 100;

	/** A byte read from a file (150 per tick). */
	public static final int READ_BYTE = 8;

	/** A byte written to a file (200 per tick). */
	public static final int WRITE_BYTE = 6;

	/** A step of a raw sort of numbers: one level of a heap's sift, two compares and a swap. */
	public static final int SORT_STEP_NUMBERS = 1200;

	/** A step of a raw sort of strings. */
	public static final int SORT_STEP_STRINGS = 4800;

	/** A table element moved by table.insert, table.remove or table.move: a get and a set. */
	public static final int TABLE_MOVE = 300;

	private static final long UNITS_PER_CHARGE = 50L * UNITS_PER_TICK;

	private final ExecutionContext context;
	private final int unitsPerStep;
	private long units;
	private boolean started;

	/**
	 * @param context  the context to charge, or {@code null} to charge nothing
	 * @param unitsPerStep  the cost of a step of this meter's main kind of work
	 */
	public WorkMeter(ExecutionContext context, int unitsPerStep) {
		this.context = context;
		this.unitsPerStep = unitsPerStep;
	}

	/** A meter whose steps are pattern steps. */
	public static WorkMeter forPatternSteps(ExecutionContext context) {
		return new WorkMeter(context, PATTERN_STEP);
	}

	/** A meter whose steps are bytes copied into a string being built. */
	public static WorkMeter forBytes(ExecutionContext context) {
		return new WorkMeter(context, BYTE);
	}

	/** A meter whose steps are bytes read from a file. */
	public static WorkMeter forReadBytes(ExecutionContext context) {
		return new WorkMeter(context, READ_BYTE);
	}

	/** A meter whose steps are bytes written to a file. */
	public static WorkMeter forWriteBytes(ExecutionContext context) {
		return new WorkMeter(context, WRITE_BYTE);
	}

	/** A meter whose steps are steps of a raw sort of numbers or strings. */
	public static WorkMeter forSortSteps(ExecutionContext context, boolean strings) {
		return new WorkMeter(context, strings ? SORT_STEP_STRINGS : SORT_STEP_NUMBERS);
	}

	/** A meter whose steps are table elements moved. */
	public static WorkMeter forTableMoves(ExecutionContext context) {
		return new WorkMeter(context, TABLE_MOVE);
	}

	/**
	 * Charges copying {@code bytes} bytes in one piece, before it is done (the {@code ..}
	 * operator): nothing for less than a tick's worth.
	 *
	 * @param context  the context to charge, must not be {@code null}
	 */
	public static void chargeBytes(ExecutionContext context, long bytes) {
		long ticks = bytes * BYTE / UNITS_PER_TICK;
		if (ticks > 0) {
			// what came before is ordinary work, not this copy's
			context.registerTicks(0);
			context.registerTicksUnpausable((int) Math.min(ticks, Integer.MAX_VALUE));
		}
	}

	/** Counts one step of the meter's main kind of work. */
	public void step() {
		add(unitsPerStep);
	}

	/** Counts {@code n} steps of the meter's main kind of work. */
	public void steps(long n) {
		add(n * unitsPerStep);
	}

	/** Counts {@code bytes} bytes appended in one piece to a string being built. */
	public void addAppend(long bytes) {
		add(APPEND + bytes * BYTE);
	}

	/** Counts {@code n} units of work, and charges them once there are enough. */
	public void add(long n) {
		units += n;
		if (units >= UNITS_PER_CHARGE) {
			charge();
		}
	}

	private void charge() {
		long ticks = units / UNITS_PER_TICK;
		units -= ticks * UNITS_PER_TICK;
		if (context != null) {
			if (!started) {
				// what came before is ordinary work, not this call's
				started = true;
				context.registerTicks(0);
			}
			context.registerTicksUnpausable((int) Math.min(ticks, Integer.MAX_VALUE));
		}
	}

}
