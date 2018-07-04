package org.educama.shipment.process;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.spring.boot.starter.test.helper.AbstractProcessEngineRuleTest;
import org.educama.BeanTestConfiguration;
import org.educama.EducamaApplication;
import org.educama.customer.model.Address;
import org.educama.customer.model.Customer;
import org.educama.customer.repository.CustomerRepository;
import org.educama.enums.ClientType;
import org.educama.enums.Status;
import org.educama.shipment.model.Cargo;
import org.educama.shipment.model.Flight;
import org.educama.shipment.model.Services;
import org.educama.shipment.model.Shipment;
import org.educama.shipment.process.sentries.FlightDepartedSentry;
import org.educama.shipment.repository.InvoiceRepository;
import org.educama.shipment.repository.ShipmentRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.processEngine;
import static org.junit.Assert.*;

/**
 * Tests the CMMN model.
 */
@SuppressWarnings("checkstyle:magicnumber")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { EducamaApplication.class, BeanTestConfiguration.class })
@Deployment(resources = "cmmn/ShipmentCase.cmmn")
public class ShipmentCaseModelTest extends AbstractProcessEngineRuleTest {

    private String caseInstanceId;
    private Long shipmentId;

    @Autowired
    ShipmentRepository shipmentRepository;

    @Autowired
    InvoiceRepository invoiceRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ShipmentCaseEvaluator caseModelHandler;

    @Autowired
    FlightDepartedSentry flightDepartedSentry;

    @Autowired
    HistoryService historyService;

    @Before
    public void setup() {
        Shipment shipment = new Shipment();
        shipment.trackingId = "INCOMPLETE_SHIPMENT_ID";
        shipment.customerTypeEnum = ClientType.SENDER;

        shipment.receiver = new Customer("Arthur Dent", new Address("Arthursstreet", "42", "00042", "Earth"));
        customerRepository.save(shipment.receiver);

        shipment.sender = new Customer("Zaphood Beeblebrox", new Address("Zaphodsstreet", "42", "12342", "Beitegeuze"));
        customerRepository.save(shipment.sender);

        shipment.shipmentCargo = new Cargo(null, 2.0, 123.0, "Don't Panic!", true);
        shipment.shipmentServices = new Services(false, false, false, true, false, false, true);
        shipment.shipmentFlight = new Flight("10243", "LH", "FRA", "STR", "2015-06-02T21:34:33.616Z", "2015-06-02T21:34:33.616Z", 100.12);
        shipment.status = Status.UNDEFINED;
        shipmentRepository.save(shipment);

        this.shipmentId = shipment.getId();
    }

    @After
    public void cleanup() {

        processEngine().close();

        Collection<CaseInstance> caseExecutionCollection = processEngine()
                .getCaseService()
                .createCaseInstanceQuery()
                .active()
                .list();

        for (CaseInstance caseInstance:caseExecutionCollection) {
            processEngine().getCaseService().terminateCaseExecution(caseInstance.getId());
            processEngine().getCaseService().closeCaseInstance(caseInstance.getId());
        }

        shipmentRepository.deleteAll();
        customerRepository.deleteAll();
        invoiceRepository.deleteAll();
    }

    @Test
    public void testCaseInitializationWithIncompleteShipmentData() {
        Shipment shipment = shipmentRepository.findOne(this.shipmentId);
        shipment.trackingId = UUID.randomUUID().toString();
        shipmentRepository.save(shipment);

        CaseInstance caseInstance = processEngine().getCaseService()
                .createCaseInstanceByKey(ShipmentCaseConstants.SHIPMENTCASEKEY, shipment.trackingId);
        this.caseInstanceId = caseInstance.getId();

        showCaseOverview(caseInstance);

        // Case Instance active?
        assertTrue(caseInstance.isActive());

        // Milestone 'Shipment order completed' not reached?
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_MILESTONE_SHIPMENT_ORDER_COMPLETED)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult().isAvailable());

        // Task 'Complete shipment order' active?
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_COMPLETE_SHIPMENT_ORDER)
                .caseInstanceBusinessKey(shipment.trackingId).active().singleResult().isActive());

        // Stage 'Process shipment order' is not enabled?
        assertFalse(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_STAGE_PROCESS_SHIPMENT_ORDER)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult().isEnabled());
    }

    @Test
    public void testCaseInitializationWithIncompleteShipmentDataWithModelUpdate() {
        Shipment shipment = shipmentRepository.findOne(this.shipmentId);
        shipment.trackingId = UUID.randomUUID().toString();
        shipmentRepository.save(shipment);

        CaseInstance caseInstance = processEngine().getCaseService()
                .createCaseInstanceByKey(ShipmentCaseConstants.SHIPMENTCASEKEY, shipment.trackingId);
        this.caseInstanceId = caseInstance.getId();

        showCaseOverview(caseInstance);

        // Case Instance active?
        assertTrue(caseInstance.isActive());

        // Milestone 'Shipment order completed' not reached?
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_MILESTONE_SHIPMENT_ORDER_COMPLETED)
                .caseInstanceBusinessKey(shipment.trackingId)
                .singleResult()
                .isAvailable());

        // Task 'Complete shipment order' active?
        CaseExecution completeShipmentOrderCaseExecution = processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_COMPLETE_SHIPMENT_ORDER)
                .caseInstanceBusinessKey(shipment.trackingId)
                .active()
                .singleResult();
        assertTrue(completeShipmentOrderCaseExecution.isActive());

        // Stage 'Process shipment order' is not enabled?
        assertFalse(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_STAGE_PROCESS_SHIPMENT_ORDER)
                .caseInstanceBusinessKey(shipment.trackingId)
                .singleResult()
                .isEnabled());

        Shipment shipment2 = shipmentRepository.findOne(this.shipmentId);
        shipment2.shipmentCargo = new Cargo(1, 2.0, 123.0, "Don't Panic!", true);
        shipmentRepository.save(shipment2);

        // Complete task 'Complete shipment order'
        Task task = processEngine().getTaskService().createTaskQuery()
                .caseExecutionId(completeShipmentOrderCaseExecution.getId())
                .singleResult();
        processEngine().getTaskService().complete(task.getId());

        // evaluate
        System.out.println("CREATING...");
        caseModelHandler.reevaluateCase(shipment.trackingId);

        // print
        showCaseOverview(caseInstance);

        // Task 'Complete shipment order' completed?
        assertNull(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_COMPLETE_SHIPMENT_ORDER)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult());

        // Stage 'Process shipment order' is active?
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_STAGE_PROCESS_SHIPMENT_ORDER)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult().isActive());
    }

    @Test
    public void testCaseAfterCreation() {
        Shipment shipment = shipmentRepository.findOne(this.shipmentId);
        shipment.shipmentCargo.numberPackages = 2;
        shipment.trackingId = UUID.randomUUID().toString();
        shipmentRepository.save(shipment);

        CaseInstance caseInstance = processEngine().getCaseService()
                .createCaseInstanceByKey(ShipmentCaseConstants.SHIPMENTCASEKEY, shipment.trackingId);
        this.caseInstanceId = caseInstance.getId();

        // Case Instance active?
        assertTrue(caseInstance.isActive());

        showCaseOverview(caseInstance);

        // Milestone reached and stage activated?
        assertNull(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_MILESTONE_SHIPMENT_ORDER_COMPLETED)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult());

        // Milestone 'Flight departed' not reached?
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_MILESTONE_FLIGHT_DEPARTED)
                .caseInstanceBusinessKey(shipment.trackingId)
                .singleResult()
                .isAvailable());

        // Stage "PlanItem_Stage_ProcessShipmentOrder" automatically active with
        // the input data?
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_STAGE_PROCESS_SHIPMENT_ORDER)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult().isActive());

        // Task 'PlanItem_HumanTask_ChangeShipmentOrder' enabled? -> Manual Task
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_CHANGE_SHIPMENT_ORDER)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult().isEnabled());

        // Stage 'PlanItem_HumanTask_CreateInvoice' is available?
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_CREATE_INVOICE)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult().isAvailable());

        // Stage 'PlanItem_HumanTask_OrganizeFlight' is active?
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_ORGANIZE_FLIGHT)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult().isActive());
    }

    @Test
    public void testCaseAfterOrganizeFlight() {
        Shipment shipment = shipmentRepository.findOne(this.shipmentId);
        shipment.shipmentCargo.numberPackages = 2;
        shipment.trackingId = UUID.randomUUID().toString();
        shipmentRepository.save(shipment);

        CaseInstance caseInstance = processEngine().getCaseService()
                .createCaseInstanceByKey(ShipmentCaseConstants.SHIPMENTCASEKEY, shipment.trackingId);
        this.caseInstanceId = caseInstance.getId();

        // Case Instance active?
        assertTrue(caseInstance.isActive());

        showCaseOverview(caseInstance);

        // Task 'organize flight' active?
        CaseExecution organizeFlightCaseExecution = processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_ORGANIZE_FLIGHT)
                .caseInstanceBusinessKey(shipment.trackingId)
                .active()
                .singleResult();
        assertTrue(organizeFlightCaseExecution.isActive());

        // Complete task 'organize flight'
        Task task = processEngine().getTaskService().createTaskQuery()
                .caseExecutionId(organizeFlightCaseExecution.getId())
                .singleResult();
        processEngine().getTaskService().complete(task.getId());

        // Milestone "Shipment order complete" reached and stage activated?
        assertNull(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_MILESTONE_SHIPMENT_ORDER_COMPLETED)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult());

        // Milestone 'Flight departed' not reached?
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_MILESTONE_FLIGHT_DEPARTED)
                .caseInstanceBusinessKey(shipment.trackingId)
                .singleResult()
                .isAvailable());

        // Stage "PlanItem_Stage_ProcessShipmentOrder" automatically active with
        // the input data?
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_STAGE_PROCESS_SHIPMENT_ORDER)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult().isActive());

        // Task 'PlanItem_HumanTask_ChangeShipmentOrder' enabled? -> Manual Task
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_CHANGE_SHIPMENT_ORDER)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult().isEnabled());

        // Stage 'PlanItem_HumanTask_CreateInvoice' is active?
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_CREATE_INVOICE)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult().isActive());

        // Task 'PlanItem_HumanTask_OrganizeFlight' is completed?
        assertNull(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_ORGANIZE_FLIGHT)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult());

        showCaseOverview(caseInstance);
    }

    @Test
    public void testCaseAfterOrganizeFlightAndFlightDeparted() {
        Shipment shipment = shipmentRepository.findOne(this.shipmentId);
        shipment.shipmentCargo.numberPackages = 2;
        shipment.trackingId = UUID.randomUUID().toString();
        shipmentRepository.save(shipment);

        CaseInstance caseInstance = processEngine().getCaseService()
                .createCaseInstanceByKey(ShipmentCaseConstants.SHIPMENTCASEKEY, shipment.trackingId);
        this.caseInstanceId = caseInstance.getId();

        // Case Instance active?
        assertTrue(caseInstance.isActive());

        showCaseOverview(caseInstance);

        // Task 'organize flight' active?
        CaseExecution organizeFlightCaseExecution = processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_ORGANIZE_FLIGHT)
                .caseInstanceBusinessKey(shipment.trackingId)
                .active()
                .singleResult();
        assertTrue(organizeFlightCaseExecution.isActive());

        // Complete task 'organize flight'
        Task task = processEngine().getTaskService().createTaskQuery()
                .caseExecutionId(organizeFlightCaseExecution.getId())
                .singleResult();
        processEngine().getTaskService().complete(task.getId());

        // Milestone 'Flight departed' not reached?
        CaseExecution flightDepartedCaseExecution = processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_MILESTONE_FLIGHT_DEPARTED)
                .caseInstanceBusinessKey(shipment.trackingId)
                .available()
                .singleResult();
        assertTrue(flightDepartedCaseExecution.isAvailable());

        // Achieve Milestone ´Flight deaparted`
        flightDepartedSentry.setDeparted(flightDepartedCaseExecution.getId(), true);

        HistoricCaseActivityInstance milestoneInstanceFlightDeparted = historyService
                .createHistoricCaseActivityInstanceQuery()
                .caseInstanceId(flightDepartedCaseExecution.getCaseInstanceId())
                .caseActivityId(ShipmentCaseConstants.PLAN_ITEM_MILESTONE_FLIGHT_DEPARTED)
                .singleResult();
        assertTrue(milestoneInstanceFlightDeparted.isCompleted());

        // Milestone "Shipment order complete" reached and stage activated?
        assertNull(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_MILESTONE_SHIPMENT_ORDER_COMPLETED)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult());

        // Stage "PlanItem_Stage_ProcessShipmentOrder" automatically active with
        // the input data?
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_STAGE_PROCESS_SHIPMENT_ORDER)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult().isActive());

        // Task 'PlanItem_HumanTask_ChangeShipmentOrder' is not available? -> Manual Task
        assertNull(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_CHANGE_SHIPMENT_ORDER)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult());

        // Stage 'PlanItem_HumanTask_CreateInvoice' is active?
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_CREATE_INVOICE)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult().isActive());

        // Task 'PlanItem_HumanTask_OrganizeFlight' is completed?
        assertNull(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_ORGANIZE_FLIGHT)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult());

        showCaseOverview(caseInstance);
    }

    @Test
    public void testCaseAfterOrganizeFlightAndFlightDepartedAndCreateInvoice() {
        Shipment shipment = shipmentRepository.findOne(this.shipmentId);
        shipment.shipmentCargo.numberPackages = 2;
        shipment.trackingId = UUID.randomUUID().toString();
        shipmentRepository.save(shipment);

        CaseInstance caseInstance = processEngine().getCaseService()
                .createCaseInstanceByKey(ShipmentCaseConstants.SHIPMENTCASEKEY, shipment.trackingId);
        this.caseInstanceId = caseInstance.getId();

        // Case Instance active?
        assertTrue(caseInstance.isActive());

        showCaseOverview(caseInstance);

        // Task 'organize flight' active?
        CaseExecution organizeFlightCaseExecution = processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_ORGANIZE_FLIGHT)
                .caseInstanceBusinessKey(shipment.trackingId)
                .active()
                .singleResult();
        assertTrue(organizeFlightCaseExecution.isActive());

        // Complete task 'organize flight'
        Task task = processEngine().getTaskService().createTaskQuery()
                .caseExecutionId(organizeFlightCaseExecution.getId())
                .singleResult();
        processEngine().getTaskService().complete(task.getId());

        // Milestone 'Flight departed' not reached?
        CaseExecution flightDepartedCaseExecution = processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_MILESTONE_FLIGHT_DEPARTED)
                .caseInstanceBusinessKey(shipment.trackingId)
                .available()
                .singleResult();
        assertTrue(flightDepartedCaseExecution.isAvailable());

        // Achieve Milestone ´Flight deaparted`
        flightDepartedSentry.setDeparted(flightDepartedCaseExecution.getId(), true);

        HistoricCaseActivityInstance milestoneInstanceFlightDeparted = historyService
                .createHistoricCaseActivityInstanceQuery()
                .caseInstanceId(flightDepartedCaseExecution.getCaseInstanceId())
                .caseActivityId(ShipmentCaseConstants.PLAN_ITEM_MILESTONE_FLIGHT_DEPARTED)
                .singleResult();
        assertTrue(milestoneInstanceFlightDeparted.isCompleted());

        // Task 'PlanItem_HumanTask_ChangeShipmentOrder' is not available? -> Manual Task
        assertNull(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_CHANGE_SHIPMENT_ORDER)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult());

        // Task 'create invoice' is active
        CaseExecution createInvoiceCaseExecution = processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_CREATE_INVOICE)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult();
        assertTrue(createInvoiceCaseExecution.isActive());

        // Complete Task 'Create Invoice'
        Task task1 = processEngine().getTaskService().createTaskQuery()
                .caseExecutionId(createInvoiceCaseExecution.getId())
                .singleResult();
        processEngine().getTaskService().complete(task1.getId());

        // Task 'create invoice' is completed
        assertNull(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_CREATE_INVOICE)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult());

        showCaseOverview(caseInstance);

        // Shipment case instance is terminated?
        HistoricCaseInstance terminatedCaseInstance = historyService
                .createHistoricCaseInstanceQuery()
                .caseInstanceId(caseInstanceId)
                .terminated()
                .singleResult();
        assertTrue(terminatedCaseInstance.isTerminated());
    }

    @Test
    public void testNullValueDataAfterCreation() {
        Shipment shipment = shipmentRepository.findOne(this.shipmentId);
        shipment.trackingId = UUID.randomUUID().toString();
        shipmentRepository.save(shipment);

        CaseInstance caseInstance = processEngine().getCaseService()
                .createCaseInstanceByKey(ShipmentCaseConstants.SHIPMENTCASEKEY, shipment.trackingId);
        this.caseInstanceId = caseInstance.getId();

        showCaseOverview(caseInstance);
        // Milestone reached and stage activated?
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_MILESTONE_SHIPMENT_ORDER_COMPLETED)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult().isAvailable());

        // Milestone 'Flight departed' not reached?
        assertNull(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_MILESTONE_FLIGHT_DEPARTED)
                .caseInstanceBusinessKey(shipment.trackingId)
                .singleResult());

        // Stage "PlanItem_Stage_ProcessShipmentOrder" automatically active with
        // the input data?
        assertTrue(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_STAGE_PROCESS_SHIPMENT_ORDER)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult().isAvailable());

        // Task 'PlanItem_HumanTask_ChangeShipmentOrder' enabled? -> Manual Task
        assertNull(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_CHANGE_SHIPMENT_ORDER)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult());

        // Stage 'PlanItem_HumanTask_CreateInvoice' is available?
        assertNull(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_CREATE_INVOICE)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult());

        // Stage 'PlanItem_HumanTask_OrganizeFlight' is available?
        assertNull(processEngine().getCaseService().createCaseExecutionQuery()
                .activityId(ShipmentCaseConstants.PLAN_ITEM_HUMAN_TASK_ORGANIZE_FLIGHT)
                .caseInstanceBusinessKey(shipment.trackingId).singleResult());
    }

    /**
     * Helper method because it is not provided by
     * {@link org.camunda.bpm.engine.test.assertions.ProcessEngineTests}.
     *
     * @return current CaseService
     */
    private CaseService caseService() {
        return processEngine().getCaseService();
    }

    // method to display an overview of executions
    private void showCaseOverview(CaseInstance caseInstance) {
        List<CaseExecution> caseExecutionList = caseService().createCaseExecutionQuery()
                .caseInstanceId(caseInstance.getId()).list();
        System.out.println("------ Current List of Case Executions ------");

        caseExecutionList.stream().filter(caseExecution -> caseExecution.getId() == caseInstance.getId())
                .forEach(caseExecution -> System.out.println("Case Instance : " + caseExecution.getActivityName() + " ["
                        + caseExecution.getActivityType() + ", " + caseExecution.getActivityId() + "]"
                        + " - CaseExecutionId: " + caseExecution.getId()));

        caseExecutionList.stream().filter(caseExecution -> caseExecution.isActive())
                .forEach(caseExecution -> System.out.println("Running ('active'): " + caseExecution.getActivityName()
                        + " [" + caseExecution.getActivityType() + ", " + caseExecution.getActivityId() + "]"
                        + " - CaseExecutionId: " + caseExecution.getId()));

        caseExecutionList.stream().filter(caseExecution -> caseExecution.isEnabled())
                .forEach(caseExecution -> System.out.println("Possible to start ('enabled'): "
                        + caseExecution.getActivityName() + " [" + caseExecution.getActivityType() + ", "
                        + caseExecution.getActivityId() + "]" + " - CaseExecutionId: " + caseExecution.getId()));

        caseExecutionList.stream().filter(c -> c.isAvailable())
                .filter(c -> c.getActivityType().compareTo("milestone") != 0)
                .forEach(caseExecution -> System.out.println("Impossible to start ('available'): "
                        + caseExecution.getActivityName() + " [" + caseExecution.getActivityType() + ", "
                        + caseExecution.getActivityId() + "]" + " - CaseExecutionId: " + caseExecution.getId()));

        caseExecutionList.stream().filter(c -> c.isAvailable())
                .filter(c -> c.getActivityType().compareTo("milestone") == 0)
                .forEach(caseExecution -> System.out.println("Milestone not reached yet: "
                        + caseExecution.getActivityName() + " - CaseExecutionId: " + caseExecution.getId()));

        System.out.println("---------------------------------------------");
        System.out.println();
    }
}
