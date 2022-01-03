package de.uka.ipd.sdq.simucomframework.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.uka.ipd.sdq.simucomframework.ResourceRegistry;
import de.uka.ipd.sdq.simucomframework.SimuComSimProcess;
import de.uka.ipd.sdq.simucomframework.model.SimuComModel;
import de.uka.ipd.sdq.simucomframework.resources.SimulatedResourceContainer;

class SimulatedResourceContainerTests {

	@Test
	void testBasicNestedResourceContainer() {
		
		var mockModel = Mockito.mock(SimuComModel.class);
		var resourceRegistry = new ResourceRegistry(mockModel);
		Mockito.when(mockModel.getResourceRegistry()).thenReturn(resourceRegistry);
		
		//Given a container with a single child container
		String parentContainerId = "container0";
		SimulatedResourceContainer parentContainer = new SimulatedResourceContainer(mockModel, parentContainerId);
		resourceRegistry.addResourceContainer(parentContainer);
		
		String childContainerId = "container1";
		SimulatedResourceContainer childContainer = new SimulatedResourceContainer(mockModel, childContainerId);
		resourceRegistry.addResourceContainer(childContainer);
		childContainer.setParentResourceContainer(parentContainerId);
		parentContainer.addNestedResourceContainer(childContainerId);
		
		
		var nested = parentContainer.getNestedResourceContainers();
		assertEquals(1, nested.size(), "we should have one nested container");
		
		var foundContainer = nested.get(0);
		assertEquals(childContainerId, foundContainer.getResourceContainerID(), "nested container should have our Id");
		assertEquals(parentContainer, foundContainer.getParentResourceContainer(), "child should have our parent as container");
		
	}
	
	
	
	@Test
	void testNestedResourceContainerCalls() {
		
		var mockModel = Mockito.mock(SimuComModel.class);
		var resourceRegistry = new ResourceRegistry(mockModel);
		Mockito.when(mockModel.getResourceRegistry()).thenReturn(resourceRegistry);
		
		//Given a container with a single child container
		String parentContainerId = "container0";
		SimulatedResourceContainer parentContainer = Mockito.mock(SimulatedResourceContainer.class);
		Mockito.when(parentContainer.getResourceContainerID()).thenReturn(parentContainerId);
		resourceRegistry.addResourceContainer(parentContainer);
		
		
		String childContainerId = "container1";
		SimulatedResourceContainer childContainer = new SimulatedResourceContainer(mockModel, childContainerId);
		resourceRegistry.addResourceContainer(childContainer);
		childContainer.setParentResourceContainer(parentContainerId);
		parentContainer.addNestedResourceContainer(childContainerId);
		
		// check fallback to parent
		var simProcess = Mockito.mock(SimuComSimProcess.class);
		parentContainer.loadActiveResource(simProcess, "dummy", 100);
		Mockito.verify(parentContainer).loadActiveResource(simProcess, "dummy", 100);
		
		parentContainer.loadActiveResource(simProcess, "dummy", 0, 100);
		Mockito.verify(parentContainer).loadActiveResource(simProcess, "dummy", 0, 100);
		
		parentContainer.loadActiveResource(simProcess, "dummy", 0, Collections.emptyMap(), 100);
		Mockito.verify(parentContainer).loadActiveResource(simProcess, "dummy", 0, Collections.emptyMap(), 100);
		
		parentContainer.loadActiveResource(simProcess, 0, "dummy", 100);
		Mockito.verify(parentContainer).loadActiveResource(simProcess, 0, "dummy", 100);
		
	}
	
	
	
	

}
