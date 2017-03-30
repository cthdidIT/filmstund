import { applyMiddleware, combineReducers, createStore } from "redux";
import thunk from "redux-thunk";
import throttle from 'lodash/throttle';

import { loadState, saveState } from "./localStorage";
import rest from "./reducers/rest";

const persistedState = loadState();


const createStoreWithMiddleware = applyMiddleware(thunk)(createStore);
const reducer = combineReducers(rest.reducers);
const store = createStoreWithMiddleware(reducer, persistedState);

store.subscribe(throttle(() => {
    saveState(store.getState());
}, 1000));

export default store;