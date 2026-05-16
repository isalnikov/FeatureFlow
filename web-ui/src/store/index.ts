import { configureStore } from '@reduxjs/toolkit';
import planningReducer from './planningSlice';
import uiReducer from './uiSlice';

export const store = configureStore({
  reducer: {
    planning: planningReducer,
    ui: uiReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
