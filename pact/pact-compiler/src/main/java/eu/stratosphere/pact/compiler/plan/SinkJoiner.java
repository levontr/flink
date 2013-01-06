/***********************************************************************************************************************
 *
 * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/

package eu.stratosphere.pact.compiler.plan;

import java.util.Collections;
import java.util.List;

import eu.stratosphere.pact.compiler.DataStatistics;
import eu.stratosphere.pact.compiler.costs.CostEstimator;
import eu.stratosphere.pact.compiler.operators.OperatorDescriptorDual;
import eu.stratosphere.pact.compiler.operators.UtilSinkJoinOpDescriptor;
import eu.stratosphere.pact.generic.contract.DualInputContract;
import eu.stratosphere.pact.generic.stub.AbstractStub;
import eu.stratosphere.pact.runtime.shipping.ShipStrategyType;

/**
 * This class represents a utility node that is not part of the actual plan. It is used for plans with multiple data sinks to
 * transform it into a plan with a single root node. That way, the code that makes sure no costs are double-counted and that 
 * candidate selection works correctly with nodes that have multiple outputs is transparently reused.
 */
public class SinkJoiner extends TwoInputNode
{
	public SinkJoiner(OptimizerNode input1, OptimizerNode input2) {
		super(new NoContract());
		
		PactConnection conn1 = new PactConnection(input1, this);
		PactConnection conn2 = new PactConnection(input2, this);
		
		conn1.setShipStrategy(ShipStrategyType.FORWARD);
		conn2.setShipStrategy(ShipStrategyType.FORWARD);
		
		this.input1 = conn1;
		this.input2 = conn2;
	}
	
	/* (non-Javadoc)
	 * @see eu.stratosphere.pact.compiler.plan.OptimizerNode#getName()
	 */
	@Override
	public String getName() {
		return "Internal Utility Node";
	}

	@Override
	protected List<OperatorDescriptorDual> getPossibleProperties() {
		return Collections.<OperatorDescriptorDual>singletonList(new UtilSinkJoinOpDescriptor());
	}

	/* (non-Javadoc)
	 * @see eu.stratosphere.pact.compiler.plan.OptimizerNode#computeOutputEstimates(eu.stratosphere.pact.compiler.DataStatistics)
	 */
	@Override
	public void computeOutputEstimates(DataStatistics statistics) {
		// nothing to be done here
	}

	/* (non-Javadoc)
	 * @see eu.stratosphere.pact.compiler.plan.OptimizerNode#computeInterestingPropertiesForInputs(eu.stratosphere.pact.compiler.costs.CostEstimator)
	 */
	@Override
	public void computeInterestingPropertiesForInputs(CostEstimator estimator) {
		// nothing to be done here
	}
	
	/* (non-Javadoc)
	 * @see eu.stratosphere.pact.compiler.plan.OptimizerNode#getBranchesForParent(eu.stratosphere.pact.compiler.plan.OptimizerNode)
	 */
	@Override
	protected List<UnclosedBranchDescriptor> getBranchesForParent(OptimizerNode parent) {
		// return our own stack of open branches, because nothing is added
		return this.openBranches;
	}

	// ------------------------------------------------------------------------
	//  Mock classes that represents a contract without behavior.
	// ------------------------------------------------------------------------
	
	private static final class MockStub extends AbstractStub {}
	
	private static final class NoContract extends DualInputContract<MockStub>
	{
		private NoContract() {
			super(MockStub.class, "NoContract");
		}
	}
}
