package util.solvers;

import gurobi.GRBEnv;
import gurobi.GRBException;
import net.sf.javailp.AbstractSolverFactory;
import net.sf.javailp.Solver;

/**
 * The {@code SolverFactoryGurobi} is a {@code SolverFactory} for Gurobi.
 * 
 * This modification uses the same Gurobi environment for all solver instances.
 * 
 * @author fabiogenoese, kriege
 * 
 */
public class SolverFactoryGurobi extends AbstractSolverFactory {
	
	GRBEnv env;
	
	public SolverFactoryGurobi() {
		try {
			env = new GRBEnv("gurobi.log");
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.AbstractSolverFactory#getInternal()
	 */
	@Override
	protected Solver getInternal() {
		return new SolverGurobi(env);
	}

}
