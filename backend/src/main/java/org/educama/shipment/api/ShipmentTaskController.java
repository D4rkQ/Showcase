package org.educama.shipment.api;

import org.educama.shipment.api.resource.ShipmentTaskListResource;
import org.educama.shipment.api.resource.ShipmentTaskResource;
import org.educama.shipment.boundary.ShipmentTaskBoundaryService;
import org.educama.shipment.api.datastructure.ShipmentTaskDS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * REST-Service to access shipment task resources.
 */
@RestController
@RequestMapping(path = ShipmentTaskController.SHIPMENTTASK_RESOURCE_PATH, produces = {MediaType.APPLICATION_JSON_VALUE})
public class ShipmentTaskController {

    public static final String SHIPMENTTASK_RESOURCE_PATH = "/educama/v1/tasks";

    @Autowired
    private ShipmentTaskBoundaryService shipmentTaskBoundaryService;

    /**
     *
     * @return a Tasklist assigned to user "educama"
     */
    @RequestMapping(method = RequestMethod.GET)
    public ShipmentTaskListResource getTasks() {
        List <ShipmentTaskDS> tasks = shipmentTaskBoundaryService.findAllActive();
        ShipmentTaskListResource taskList = new ShipmentTaskListResource().fromTaskCollection(tasks);
        return taskList;
    }


    /**
     * API call to manually start an enabled task by trackingId and name.
     *
     */
    @RequestMapping(value = "/enabled/start/taskId", method = RequestMethod.POST)
    public ResponseEntity<?> manuallyStartEnabledTask(@Valid @RequestBody ShipmentTaskResource shipmentTaskResource) {
        shipmentTaskBoundaryService.manuallyStartEnabledTask(shipmentTaskResource.trackingId, shipmentTaskResource.name);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}