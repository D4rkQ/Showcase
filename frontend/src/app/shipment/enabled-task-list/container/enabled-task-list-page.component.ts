import {Component, OnDestroy, OnInit} from "@angular/core";
import {Store} from "@ngrx/store";
import {Observable, Subscription} from "rxjs";
import {State} from "../../../app.reducers";
import {ActivatedRoute, Router} from "@angular/router";
import {EnabledTaskListSlice} from "../../shipment-common/store/enbaled-tasks/enabled-task-list-page.slice";
import {EnabledTaskListModel, EnabledTaskListRowModel} from "./enabled-task-list-page.model";
import {
  InitializeEnabledTaskListAction,
  RequestEnabledTasksForShipmentAction, RequestManuallyStartEnabledTaskAction
} from "../../shipment-common/store/enbaled-tasks/enabled-task-list-page.actions";
import {TaskService} from "../../shipment-common/api/task.service";
import {TaskResource} from "../../shipment-common/api/resources/task.resource";



@Component({
  selector: "educama-enabled-task-list-page",
  templateUrl: `./enabled-task-list-page.component.html`
})
export class EnabledTaskListPageComponent implements OnInit, OnDestroy {

  // relevant slice of store and subscription for this slice
  public enabledTaskListSlice: Observable<EnabledTaskListSlice>;
  public enabledTaskListSliceSubscription: Subscription;

  // model for the page
  public enabledTaskListModel: EnabledTaskListModel = new EnabledTaskListModel();

  constructor(private _router: Router,
              private _store: Store<State>,
              private _activatedRoute: ActivatedRoute,
              private _taskService: TaskService) {

    this.enabledTaskListSlice = this._store.select(state => state.enabledTaskListSlice);

    this.enabledTaskListSliceSubscription = this.enabledTaskListSlice
      .subscribe(enabledTaskListSlice => this.updateEnabledTaskListModel(enabledTaskListSlice));
  }

  public ngOnInit() {

    this._activatedRoute.params.subscribe(params => {
      this._store.dispatch(new RequestEnabledTasksForShipmentAction(params["id"]))
      });

  }

  public ngOnDestroy() {
    this._store.dispatch(new InitializeEnabledTaskListAction());
    this.enabledTaskListSliceSubscription.unsubscribe();
  }

  // ***************************************************
  // Event Handler
  // ***************************************************

  public onTaskSelectedEvent(taskResource: TaskResource) {
    this._store.dispatch(new RequestManuallyStartEnabledTaskAction(taskResource));
    //store und effect verwenden
    this._taskService.manuallyStartEnabledTask(taskResource.trackingId, taskResource.name);
  }

  // ***************************************************
  // Data Retrieval
  // ***************************************************

  private updateEnabledTaskListModel(enabledTaskListSlice: EnabledTaskListSlice) {
    this.enabledTaskListModel.enabledTaskList =
      enabledTaskListSlice.enabledTaskList.map(
        enabledTaskResource => new EnabledTaskListRowModel(
          enabledTaskResource.trackingId,
          enabledTaskResource.type,
          enabledTaskResource.name,
          enabledTaskResource.id,
          enabledTaskResource.description)
      );

  }
}
