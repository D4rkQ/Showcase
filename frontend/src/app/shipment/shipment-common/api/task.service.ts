import {Injectable} from "@angular/core";
import {RestClientService} from "../../../shared/http/services/rest-client.service";
import {Observable} from "rxjs/Observable";
import "rxjs/add/operator/catch";
import "rxjs/add/operator/map";
import "rxjs/add/observable/throw";
import {TaskListResource} from "./resources/task-list.resource";

/*
 * Service to communicate with Task Resource
 */
@Injectable()
export class TaskService {

  private TASK_RESOURCE_PATH = "tasks";

  constructor(private _restClientService: RestClientService) {
  }

  /*
   * Find all shipments
   *
   * @return An observable array of shipments
   */
  public findTasks(): Observable<TaskListResource> {
    return this._restClientService.get(this.TASK_RESOURCE_PATH + "/active");
  }


  public findTasksForShipment(trackingId: string): Observable<TaskListResource> {
    return this._restClientService.get(this.TASK_RESOURCE_PATH + "/active/" + trackingId);
  }


  public findAllEnabledTasks(): Observable<TaskListResource> {
    return this._restClientService.get(this.TASK_RESOURCE_PATH + "/enabled");
  }

  public findEnabledTasksToShipment(trackingId: string): Observable<TaskListResource> {
    return this._restClientService.get(this.TASK_RESOURCE_PATH + "/enabled/" + trackingId);
  }

  public manuallyStartEnabledTask(trackingId: string, name: string): Observable<TaskListResource> {
    return this._restClientService.post(this.TASK_RESOURCE_PATH + "/enabled/start/" + trackingId + "/" + name);

  }
}
