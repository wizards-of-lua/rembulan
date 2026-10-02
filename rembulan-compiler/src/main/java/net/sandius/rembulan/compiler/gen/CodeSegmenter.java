/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.ir.BasicBlock;
import net.sandius.rembulan.compiler.ir.BodyNode;
import net.sandius.rembulan.compiler.ir.Code;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.compiler.ir.Line;
import net.sandius.rembulan.compiler.ir.ToNext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class CodeSegmenter {

	private CodeSegmenter() {
		// not to be instantiated
	}

	private static int lastLine(List<BodyNode> nodes) {
		int line = -1;
		for (BodyNode n : nodes) {
			if (n instanceof Line) {
				line = ((Line) n).lineNumber();
			}
		}
		return line;
	}

	public static SegmentedCode segment(Code code, int limit) {
		if (limit <= 0) {
			return SegmentedCode.singleton(code);
		}
		else {
			List<List<BasicBlock>> segmentBlocks = new ArrayList<>();

			List<BasicBlock> currentSegment = new ArrayList<>();
			int count = 0;
			int splitIdx = 0;

			Iterator<BasicBlock> bit = code.blockIterator();
			while (bit.hasNext()) {
				BasicBlock blk = bit.next();
				List<BodyNode> body = blk.body();

				// the part of blk not yet placed: carried line info, then body from pos on;
				// tracked by position, since copying the rest on each split is quadratic
				// in the size of a large block
				Label label = blk.label();
				Line carried = null;
				int pos = 0;

				while (true) {
					int head = carried != null ? 1 : 0;
					int len = head + body.size() - pos + 1;

					if (count + len <= limit) {
						BasicBlock rest = pos == 0 && carried == null
								? blk
								: new BasicBlock(label, bodyOf(carried, body, pos, body.size()), blk.end());
						currentSegment.add(rest);
						count += len;

						if (count == limit) {
							// fits entirely in, but it's the last one
							segmentBlocks.add(Collections.unmodifiableList(currentSegment));

							// start new segment
							currentSegment = new ArrayList<>();
							count = 0;
						}
						break;
					}
					else {
						// split the rest of blk, and try again with the remainder
						int end = pos + (limit - count) - head;
						List<BodyNode> predBody = bodyOf(carried, body, pos, end);
						Label succLabel = new Label(-(splitIdx++ + 1));

						// current segment is done
						currentSegment.add(new BasicBlock(label, predBody, new ToNext(succLabel)));
						segmentBlocks.add(Collections.unmodifiableList(currentSegment));

						// start new segment
						currentSegment = new ArrayList<>();
						count = 0;

						// carry line info over to the remainder
						int line = lastLine(predBody);
						carried = line != -1 ? new Line(line) : null;
						label = succLabel;
						pos = end;
					}
				}
			}

			if (!currentSegment.isEmpty()) {
				segmentBlocks.add(Collections.unmodifiableList(currentSegment));
			}

			return SegmentedCode.of(segmentBlocks);
		}
	}

	private static List<BodyNode> bodyOf(Line carried, List<BodyNode> body, int from, int to) {
		List<BodyNode> result = new ArrayList<>(to - from + 1);
		if (carried != null) {
			result.add(carried);
		}
		result.addAll(body.subList(from, to));
		return Collections.unmodifiableList(result);
	}

}
