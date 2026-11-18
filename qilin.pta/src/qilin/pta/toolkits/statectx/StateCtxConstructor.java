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

import qilin.core.context.ContextElement;
import qilin.core.context.ContextElements;
import qilin.core.pag.CallSite;
import qilin.core.pag.ContextAllocNode;
import qilin.parm.ctxcons.CtxConstructor;
import soot.Context;
import soot.MethodOrMethodContext;
import soot.SootMethod;

/*
 * The callee's context is the state reached from the caller's state on the call site.
 * The empty context stands for the initial state.
 * */
public class StateCtxConstructor implements CtxConstructor {
    private final StateTransitions transitions;

    public StateCtxConstructor(StateTransitions transitions) {
        this.transitions = transitions;
    }

    @Override
    public Context constructCtx(MethodOrMethodContext caller, ContextAllocNode receiverNode, CallSite callSite, SootMethod target) {
        Context callerContext = caller.context();
        assert callerContext instanceof ContextElements;
        ContextElements ctxElems = (ContextElements) callerContext;
        CtxState from = ctxElems.isEmpty() ? transitions.initial() : (CtxState) ctxElems.get(0);
        CtxState to = transitions.next(from, callSite);
        if (to == from) {
            return callerContext;
        }
        if (to == transitions.initial()) {
            return emptyContext;
        }
        return new ContextElements(new ContextElement[]{to}, 1);
    }
}
