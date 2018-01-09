import {Component, Input, Output, EventEmitter} from "@angular/core";
import {EnabledTaskListRowModel} from "../container/enabled-task-list-page.model";
import {TaskResource} from "../../shipment-common/api/resources/task.resource";
import {State} from "../../../app.reducers";
import {Store} from "@ngrx/store";
import {ActivatedRoute} from "@angular/router";
import {RequestEnabledTasksForShipmentAction} from "../../shipment-common/store/enbaled-tasks/enabled-task-list-page.actions";

@Component({
  selector: "educama-enabled-task-list",
  templateUrl: "./enabled-task-list.component.html"
})
export class EnabledTaskListComponent {

  @Input()
  public enabledTaskList: EnabledTaskListRowModel[];

  @Output()
  public selectedEnabledTask: TaskResource = new TaskResource();

  @Output()
  public taskSelectedEvent: EventEmitter<TaskResource> = new EventEmitter();


  constructor(private _store: Store<State>,
              private _activatedRoute: ActivatedRoute) {

  }

  public onRowSelect(event: Event) {
    this.taskSelectedEvent.emit(this.selectedEnabledTask);
  }

}
