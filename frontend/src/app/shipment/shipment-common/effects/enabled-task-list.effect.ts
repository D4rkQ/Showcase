import {Injectable} from "@angular/core";
import {Actions, Effect, toPayload} from "@ngrx/effects";
import {Store} from "@ngrx/store";
import {State} from "../../../app.reducers";
import {Observable} from "rxjs/Observable";
import {TaskService} from "../api/task.service";
import * as actions from "../store/enbaled-tasks/enabled-task-list-page.actions";
import {EnabledTaskListSlice} from "../store/enbaled-tasks/enabled-task-list-page.slice";
import {
  RequestEnabledTasksFailedAction, RequestEnabledTasksForShipmentAction,
  RequestEnabledTasksSuccessfulAction, RequestManuallyStartEnabledTaskAction
} from "../store/enbaled-tasks/enabled-task-list-page.actions";
import {TaskResource} from "../api/resources/task.resource";
import {TaskListResource} from "../api/resources/task-list.resource";
import {Router} from "@angular/router";
import {timer} from "rxjs/observable/timer";

@Injectable()
export class EnabledTaskListEffect {
  private id: string;
  private taskResource: TaskResource;
  constructor(private _actions: Actions,
              private _taskService: TaskService,
              private _store: Store<State>,
              private _router: Router) {

  }

  @Effect() manuallyStartEnabledTask = this._actions
    .ofType(actions.REQUEST_MANUALLY_START_ENABLED_TASK)
    .switchMap((action: RequestManuallyStartEnabledTaskAction) =>
      this._taskService.manuallyStartEnabledTask(action.taskResource.trackingId, action.taskResource.name))
    .map((TaskListResource: TaskListResource) => new RequestEnabledTasksSuccessfulAction(TaskListResource))
    .catch(() => Observable.of(new RequestEnabledTasksFailedAction()))

    .do(() => this._router.navigate( ["/shipments/"]))
    .delay(1)
    .do(() => this._router.navigate( ["/caseui/"+ this.id]));


  @Effect() loadEnabledTasks = this._actions
    .ofType(actions.REQUEST_ENABLED_TASKS_FOR_SHIPMENT)
    .map((action: actions.RequestEnabledTasksForShipmentAction) => this.id = action.trackingId)
    .withLatestFrom(this._store, (action, state) => state.enabledTaskListSlice)
    .switchMap((enabledTaskListSlice: EnabledTaskListSlice) => {

      return this._taskService.findEnabledTasksToShipment(this.id);
    })
    .map(enabledTaskListSlice => new RequestEnabledTasksSuccessfulAction(enabledTaskListSlice))
    .catch(() => Observable.of(new RequestEnabledTasksFailedAction()));
}
