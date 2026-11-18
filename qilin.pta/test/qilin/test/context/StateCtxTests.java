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

package qilin.test.context;

import driver.PTAPattern;
import org.junit.Test;
import qilin.core.PTA;
import qilin.core.PTAScene;
import qilin.core.pag.CallSite;
import qilin.pta.PTAConfig;
import qilin.pta.toolkits.statectx.CtxState;
import qilin.pta.toolkits.statectx.StateTransitions;
import qilin.pta.tools.StateCtxPTA;
import qilin.test.util.AssertionsParser;
import qilin.test.util.IAssertion;
import qilin.test.util.JunitTests;
import qilin.util.PTAUtils;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.options.Options;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class StateCtxTests extends JunitTests {
    private static final String FIGURE7 = "qilin.microben.context.statectx.Figure7";

    @Test
    public void testFigure7OneCallSite() {
        PTA pta = run(FIGURE7, "1c");
        Set<IAssertion> assertions = AssertionsParser.retrieveQueryInfo(pta);
        assertEquals(1, assertions.size());
        for (IAssertion mAssert : assertions) {
            assertFalse(mAssert.check());
        }
        assertEquals(2, contextsOf(pta, "set"));
    }

    @Test
    public void testFigure7TwoCallSite() {
        PTA pta = run(FIGURE7, "2c");
        checkAssertions(pta);
        assertEquals(4, contextsOf(pta, "set"));
    }

    @Test
    public void testFigure7StateCtx() {
        PTAConfig.v().getAppConfig().MAIN_CLASS = FIGURE7;
        Options.v().set_main_class(FIGURE7);
        PTAScene.v().setMainClass(PTAScene.v().getSootClass(FIGURE7));
        PTAConfig.v().getPtaConfig().ptaPattern = new PTAPattern("1c");
        PTA pta = new StateCtxPTA(figure7Transitions(), 0);
        pta.pureRun();
        checkAssertions(pta);
        assertEquals(2, contextsOf(pta, "set"));
    }

    /*
     * The two calls of m in main lead to different states; in m, this.set(v) keeps the
     * state (no entry) and v.set(this) swaps the two states.
     * */
    private static StateTransitions figure7Transitions() {
        SootMethod main = PTAScene.v().getSootClass(FIGURE7).getMethodByName("main");
        SootMethod m = c().getMethodByName("m");
        CtxState q0 = new CtxState("q0"), x = new CtxState("X"), y = new CtxState("Y");
        StateTransitions transitions = new StateTransitions(q0);
        transitions.add(nthCallOf(main, "m", 0), q0, x);
        transitions.add(nthCallOf(main, "m", 1), q0, y);
        CallSite swap = nthCallOf(m, "set", 1);
        transitions.add(swap, x, y);
        transitions.add(swap, y, x);
        return transitions;
    }

    private static SootClass c() {
        return PTAScene.v().getSootClass(FIGURE7 + "$C");
    }

    private static CallSite nthCallOf(SootMethod caller, String calleeName, int n) {
        for (Unit unit : PTAUtils.getMethodBody(caller).getUnits()) {
            Stmt stmt = (Stmt) unit;
            if (stmt.containsInvokeExpr() && stmt.getInvokeExpr().getMethodRef().getName().equals(calleeName) && n-- == 0) {
                return new CallSite(unit);
            }
        }
        throw new RuntimeException("no such call of " + calleeName + " in " + caller);
    }

    private static long contextsOf(PTA pta, String methodName) {
        SootMethod method = c().getMethodByName(methodName);
        return pta.getReachableMethods().stream().filter(momc -> momc.method() == method).count();
    }
}
