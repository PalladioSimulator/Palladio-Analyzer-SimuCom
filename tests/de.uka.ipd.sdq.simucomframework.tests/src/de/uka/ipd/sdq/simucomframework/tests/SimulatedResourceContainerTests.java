package de.uka.ipd.sdq.simucomframework.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ProcessingResourceType;

import de.uka.ipd.sdq.scheduler.ISchedulingFactory;
import de.uka.ipd.sdq.scheduler.resources.active.SimDelayResource;
import de.uka.ipd.sdq.simucomframework.core.ResourceRegistry;
import de.uka.ipd.sdq.simucomframework.core.SimuComConfig;
import de.uka.ipd.sdq.simucomframework.core.SimuComSimProcess;
import de.uka.ipd.sdq.simucomframework.core.exceptions.ResourceContainerIsMissingRequiredResourceType;
import de.uka.ipd.sdq.simucomframework.core.model.SimuComModel;
import de.uka.ipd.sdq.simucomframework.core.resources.SchedulingStrategy;
import de.uka.ipd.sdq.simucomframework.core.resources.SimulatedResourceContainer;
import de.uka.ipd.sdq.simucomframework.simucomstatus.SimucomstatusFactory;
import de.uka.ipd.sdq.simulation.abstractsimengine.IEntity;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimEngineFactory;

class SimulatedResourceContainerTests {

	// Mocks
	SimuComModel mockModel;
	ResourceRegistry resourceRegistry;
	ISimEngineFactory simEngineFactory;
	ISchedulingFactory schedulingFactory;
	SimuComConfig simuComConfig;

	@BeforeEach
	void createMockModel() {

		mockModel = mock(SimuComModel.class);

		simEngineFactory = mock(ISimEngineFactory.class);
		when(mockModel.getSimEngineFactory()).thenReturn(simEngineFactory);

		resourceRegistry = new ResourceRegistry(mockModel);
		when(mockModel.getResourceRegistry()).thenReturn(resourceRegistry);
		
		var status = SimucomstatusFactory.eINSTANCE.createSimuComStatus();
		status.setResourceStatus(SimucomstatusFactory.eINSTANCE.createSimulatedResources());
		when(mockModel.getSimulationStatus()).thenReturn(status);
		
		schedulingFactory = mock(ISchedulingFactory.class);
		when(mockModel.getSchedulingFactory()).thenReturn(schedulingFactory);
		
		simuComConfig = mock(SimuComConfig.class);
		when(simuComConfig.getSimulateFailures()).thenReturn(false);
		when(mockModel.getConfiguration()).thenReturn(simuComConfig);
		
	}

	@Test
	void testBasicNestedResourceContainer() {

		// Given a container with a single child container
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
		assertEquals(parentContainer, foundContainer.getParentResourceContainer(),
				"child should have our parent as container");

	}

	@Test
	void testNestedResourceContainerCalls() {

		// Given a container with a single child container
		String parentContainerId = "container0";
		SimulatedResourceContainer parentContainer = mock(SimulatedResourceContainer.class);
		when(parentContainer.getResourceContainerID()).thenReturn(parentContainerId);
		resourceRegistry.addResourceContainer(parentContainer);

		String childContainerId = "container1";
		SimulatedResourceContainer childContainer = new SimulatedResourceContainer(mockModel, childContainerId);
		resourceRegistry.addResourceContainer(childContainer);
		childContainer.setParentResourceContainer(parentContainerId);
		parentContainer.addNestedResourceContainer(childContainerId);

		// check fallback to parent
		var simProcess = mock(SimuComSimProcess.class);
		childContainer.loadActiveResource(simProcess, "dummy", 100);
		verify(parentContainer).loadActiveResource(simProcess, "dummy", 100);

		childContainer.loadActiveResource(simProcess, "dummy", 0, 100);
		verify(parentContainer).loadActiveResource(simProcess, "dummy", 0, 100);

		childContainer.loadActiveResource(simProcess, "dummy", 0, Collections.emptyMap(), 100);
		verify(parentContainer).loadActiveResource(simProcess, "dummy", 0, Collections.emptyMap(), 100);

		childContainer.loadActiveResource(simProcess, 0, "dummy", 100);
		verify(parentContainer).loadActiveResource(simProcess, 0, "dummy", 100);

	}

	@Test
	void testMissingResourceTypeException() {

		// Given a container with no child container
		String containerId = "container0";
		SimulatedResourceContainer container = new SimulatedResourceContainer(mockModel, containerId);
		resourceRegistry.addResourceContainer(container);

		// check exception for missing resource is thrown
		var simProcess = mock(SimuComSimProcess.class);

		assertThrows(ResourceContainerIsMissingRequiredResourceType.class, () -> {
			container.loadActiveResource(simProcess, "dummy", 100);
		}, "expected exception for a missing required resource type");

		assertThrows(ResourceContainerIsMissingRequiredResourceType.class, () -> {
			container.loadActiveResource(simProcess, "dummy", 0, 100);
		}, "expected exception for a missing required resource type");

		assertThrows(ResourceContainerIsMissingRequiredResourceType.class, () -> {
			container.loadActiveResource(simProcess, "dummy", 0, Collections.emptyMap(), 100);
		}, "expected exception for a missing required resource type");

		assertThrows(ResourceContainerIsMissingRequiredResourceType.class, () -> {
			container.loadActiveResource(simProcess, 0, "dummy", 100);
		}, "expected exception for a missing required resource type");

	}

	@Test
	void testAddActiveResource() {

		// Given a container with no child container
		String containerId = "container0";
		SimulatedResourceContainer container = new SimulatedResourceContainer(mockModel, containerId);
		resourceRegistry.addResourceContainer(container);

		// we add an active resource
		var resource = mock(ProcessingResourceSpecification.class);
		var resourceType = getMockResourceType();
		var resourceContainer = getMockResourceContainer();
		when(resource.getActiveResourceType_ActiveResourceSpecification()).thenReturn(resourceType);
		when(resource.getResourceContainer_ProcessingResourceSpecification()).thenReturn(resourceContainer);
		when(resource.getNumberOfReplicas()).thenReturn(42);
		when(resource.isRequiredByContainer()).thenReturn(false);
		when(resource.getMTTF()).thenReturn(42.0);
		when(resource.getMTTR()).thenReturn(23.0);
		PCMRandomVariable rate = CoreFactory.eINSTANCE.createPCMRandomVariable();
		rate.setSpecification("5");
		when(resource.getProcessingRate_ProcessingResourceSpecification()).thenReturn(rate);

		var mockEntity = mock(IEntity.class);
		when(simEngineFactory.createEntity(any(), anyString())).thenReturn(mockEntity);
		var mockActiveResource = mock(SimDelayResource.class);
		when(schedulingFactory.createSimDelayResource(any(), any())).thenReturn(mockActiveResource);
		container.addActiveResourceWithoutCalculators(resource, null, containerId, SchedulingStrategy.DELAY);
		
		assertEquals(1, container.getActiveResources().size(), "we should have one active resource");
		assertEquals(mockActiveResource, container.getActiveResources().stream().findFirst().get().getScheduledResource(), "the mock resource should be in our container");

	}
	
	

	private final ProcessingResourceType getMockResourceType() {
		var mockResourceType = mock(ProcessingResourceType.class);
		when(mockResourceType.getEntityName()).thenReturn("mockResourceType");
		when(mockResourceType.getId()).thenReturn("mockResourceTypeId");
		return mockResourceType;

	}

	private final ResourceContainer getMockResourceContainer() {
		var mockResourceContainer = mock(ResourceContainer.class);
		when(mockResourceContainer.getEntityName()).thenReturn("mockResourceContainer");
		when(mockResourceContainer.getId()).thenReturn("mockResourceContainerId");
		return mockResourceContainer;

	}

}
