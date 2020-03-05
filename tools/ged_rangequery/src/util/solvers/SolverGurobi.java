package util.solvers;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import net.sf.javailp.Constraint;
import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.ResultImpl;
import net.sf.javailp.Term;
import net.sf.javailp.VarType;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The {@code SolverGurobi} is the {@code Solver} Gurobi.
 * 
 * This modification allows to pass a Gurobi environment as a
 * parameter.
 * 
 * @author fabiogenoese, lukasiewycz, kriege
 * 
 */
public class SolverGurobi extends net.sf.javailp.SolverGurobi {
	
	public GRBEnv env;
	
	public SolverGurobi() {
		try {
			env = new GRBEnv("gurobi.log");
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}
	
	public SolverGurobi(GRBEnv env) {
		this.env = env;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#solve(net.sf.javailp.Problem)
	 */
	public Result solve(Problem problem) {

		Map<Object, GRBVar> objToVar = new HashMap<Object, GRBVar>();
		Map<GRBVar, Object> varToObj = new HashMap<GRBVar, Object>();
		// Map<String, GRBVar> nameToVar = new HashMap<String, GRBVar>(nvar);

		try {

			initWithParameters(env);

			GRBModel model = new GRBModel(env);

			OptType optType = problem.getOptType();
			Map<Object, Double> optimizationCoefficients = new HashMap<Object, Double>();
			Linear objective = problem.getObjective();
			if (objective != null) {
				for (Term term : objective) {
					Object variable = term.getVariable();
					double coeff = term.getCoefficient().doubleValue();
					if (optType == OptType.MAX) {
						coeff *= -1;
					}
					optimizationCoefficients.put(variable, coeff);
				}
			}

			for (Object variable : problem.getVariables()) {
				VarType varType = problem.getVarType(variable);
				Number lowerBound = problem.getVarLowerBound(variable);
				Number upperBound = problem.getVarUpperBound(variable);

				double lb = (lowerBound != null ? lowerBound.doubleValue()
						: -Double.MAX_VALUE);
				double ub = (upperBound != null ? upperBound.doubleValue()
						: Double.MAX_VALUE);

				final String name = variable.toString();
				final char type;
				switch (varType) {
				case BOOL:
					type = GRB.BINARY;
					break;
				case INT:
					type = GRB.INTEGER;
					break;
				default: // REAL
					type = GRB.CONTINUOUS;
					break;
				}

				Double coeff = optimizationCoefficients.get(variable);
				if (coeff == null) {
					coeff = 0.0;
				}

				GRBVar var = model.addVar(lb, ub, coeff, type, name);
				objToVar.put(variable, var);
				varToObj.put(var, variable);
			}
			model.update();

			for (Constraint constraint : problem.getConstraints()) {
				GRBLinExpr expr = new GRBLinExpr();

				for (Term term : constraint.getLhs()) {
					GRBVar var = objToVar.get(term.getVariable());
					expr.addTerm(term.getCoefficient().doubleValue(), var);
				}

				final char operator;
				if (constraint.getOperator() == Operator.GE)
					operator = GRB.GREATER_EQUAL;
				else if (constraint.getOperator() == Operator.LE)
					operator = GRB.LESS_EQUAL;
				else
					operator = GRB.EQUAL;

				model.addConstr(expr, operator, constraint.getRhs()
						.doubleValue(), constraint.getName());
			}

			for(Hook hook: hooks){
				hook.call(env, model, objToVar, varToObj, problem);
			}
			
			model.optimize();

			Result result;
			if (problem.getObjective() != null) {
				result = new ResultImpl(problem.getObjective());
			} else {
				result = new ResultImpl();
			}

			for (Entry<Object, GRBVar> entry : objToVar.entrySet()) {
				Object variable = entry.getKey();
				GRBVar var = entry.getValue();

				double primalValue = var.get(GRB.DoubleAttr.X);

				if (problem.getVarType(variable).isInt()) {
					int v = (int) Math.round(primalValue);
					result.putPrimalValue(variable, v);
				} else {
					result.putPrimalValue(variable, primalValue);
				}
			}

			return result;

		} catch (GRBException e) {
			e.printStackTrace();
			return null;
		}

	}

}