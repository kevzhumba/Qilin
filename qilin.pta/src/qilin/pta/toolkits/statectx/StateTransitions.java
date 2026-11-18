/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.pta.toolkits.statectx;

import qilin.core.pag.CallSite;

import java.util.HashMap;
import java.util.Map;

/*
 * The transition function of a context automaton whose alphabet is the set of call sites.
 * A call site without an entry for the caller's state acts as the identity on that state,
 * i.e., the callee inherits the caller's context (context tunneling).
 * */
public class StateTransitions {
    private final CtxState initial;
    private final Map<CallSite, Map<CtxState, CtxState>> delta = new HashMap<>();

    public StateTransitions(CtxState initial) {
        this.initial = initial;
    }

    public CtxState initial() {
        return initial;
    }

    public void add(CallSite callSite, CtxState from, CtxState to) {
        delta.computeIfAbsent(callSite, k -> new HashMap<>()).put(from, to);
    }

    public CtxState next(CtxState from, CallSite callSite) {
        Map<CtxState, CtxState> row = delta.get(callSite);
        if (row == null) {
            return from;
        }
        return row.getOrDefault(from, from);
    }
}
