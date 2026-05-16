import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { planningApi } from '../api/planning';
import type { PlanningResult, PlanningJob, PlanningRunRequest } from '../types';

interface PlanningState {
  currentJob: PlanningJob | null;
  currentResult: PlanningResult | null;
  isPlanning: boolean;
  error: string | null;
}

const initialState: PlanningState = {
  currentJob: null,
  currentResult: null,
  isPlanning: false,
  error: null,
};

export const runPlanning = createAsyncThunk(
  'planning/run',
  async (request: PlanningRunRequest, { dispatch, rejectWithValue }) => {
    try {
      const job = await planningApi.run(request);

      while (true) {
        await new Promise((resolve) => setTimeout(resolve, 1000));
        const status = await planningApi.getJobStatus(job.jobId);
        dispatch(updateJobStatus(status));

        if (status.status === 'DONE' || status.status === 'FAILED') {
          break;
        }
      }

      if (job.status === 'FAILED') {
        return rejectWithValue(job.errorMessage || 'Planning failed');
      }

      return await planningApi.getResult(job.jobId);
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Planning failed';
      return rejectWithValue(message);
    }
  },
);

const planningSlice = createSlice({
  name: 'planning',
  initialState,
  reducers: {
    updateJobStatus: (state, action: PayloadAction<PlanningJob>) => {
      state.currentJob = action.payload;
    },
    clearPlanning: (state) => {
      state.currentResult = null;
      state.currentJob = null;
      state.isPlanning = false;
      state.error = null;
    },
    setResult: (state, action: PayloadAction<PlanningResult>) => {
      state.currentResult = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(runPlanning.pending, (state) => {
        state.isPlanning = true;
        state.error = null;
      })
      .addCase(runPlanning.fulfilled, (state, action) => {
        state.currentResult = action.payload;
        state.isPlanning = false;
      })
      .addCase(runPlanning.rejected, (state, action) => {
        state.isPlanning = false;
        state.error = (action.payload as string) || 'Planning failed';
      });
  },
});

export const { updateJobStatus, clearPlanning, setResult } = planningSlice.actions;
export default planningSlice.reducer;
