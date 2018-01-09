import {ActionReducer, Action} from "@ngrx/store";
import * as actions from "./enabled-task-list-page.actions";
import {EnabledTaskListSlice} from "./enabled-task-list-page.slice";
import {ENABLED_TASK_LIST_SLICE_INITIAL_STATE} from "./enabled-task-list-page.initial-state";

export function enabledTaskListPageReducer(state: EnabledTaskListSlice = ENABLED_TASK_LIST_SLICE_INITIAL_STATE,
                                           action: Action): EnabledTaskListSlice {
  switch (action.type) {
    case actions.INITIALIZE_ENABLED_TASK_LIST:
      return ENABLED_TASK_LIST_SLICE_INITIAL_STATE;
    case actions.REQUEST_ENABLED_TASKS_FOR_SHIPMENT:
      const requestEnabledTasks = action as actions.RequestEnabledTasksSuccessfulAction;
      return Object.assign({}, state, {
                loading: true,
      });
    case actions.REQUEST_ENABLED_TASKS_SUCCESSFUL:
      const requestEnabledTasksSuccessfulAction = action as actions.RequestEnabledTasksSuccessfulAction;
      return Object.assign({}, state, {
        enabledTaskList: requestEnabledTasksSuccessfulAction.payload.tasks,
        loading: false
      });
    case actions.REQUEST_ENABLED_TASKS_FAILED:
      return Object.assign({}, state, {
        loading: false
      });
    default:
      return state;
  }
}

export const ENABLED_TASK_LIST_PAGE_REDUCER: ActionReducer<EnabledTaskListSlice> = enabledTaskListPageReducer;
