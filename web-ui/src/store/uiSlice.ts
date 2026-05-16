import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export type GanttZoomLevel = 'day' | 'week' | 'sprint' | 'month';

export type SidebarSection = 'features' | 'teams' | 'conflicts' | 'load';

interface UIState {
  sidebarOpen: boolean;
  sidebarSection: SidebarSection;
  ganttZoom: GanttZoomLevel;
  selectedFeatureId: string | null;
  selectedTeamId: string | null;
  selectedSprintId: string | null;
  showCreateFeatureModal: boolean;
  showCreateTeamModal: boolean;
  showPlanningParamsModal: boolean;
  theme: 'light' | 'dark';
}

const initialState: UIState = {
  sidebarOpen: true,
  sidebarSection: 'features',
  ganttZoom: 'sprint',
  selectedFeatureId: null,
  selectedTeamId: null,
  selectedSprintId: null,
  showCreateFeatureModal: false,
  showCreateTeamModal: false,
  showPlanningParamsModal: false,
  theme: 'light',
};

const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    toggleSidebar: (state) => {
      state.sidebarOpen = !state.sidebarOpen;
    },
    setSidebarSection: (state, action: PayloadAction<SidebarSection>) => {
      state.sidebarSection = action.payload;
    },
    setGanttZoom: (state, action: PayloadAction<GanttZoomLevel>) => {
      state.ganttZoom = action.payload;
    },
    setSelectedFeature: (state, action: PayloadAction<string | null>) => {
      state.selectedFeatureId = action.payload;
    },
    setSelectedTeam: (state, action: PayloadAction<string | null>) => {
      state.selectedTeamId = action.payload;
    },
    setSelectedSprint: (state, action: PayloadAction<string | null>) => {
      state.selectedSprintId = action.payload;
    },
    setShowCreateFeatureModal: (state, action: PayloadAction<boolean>) => {
      state.showCreateFeatureModal = action.payload;
    },
    setShowCreateTeamModal: (state, action: PayloadAction<boolean>) => {
      state.showCreateTeamModal = action.payload;
    },
    setShowPlanningParamsModal: (state, action: PayloadAction<boolean>) => {
      state.showPlanningParamsModal = action.payload;
    },
    setTheme: (state, action: PayloadAction<'light' | 'dark'>) => {
      state.theme = action.payload;
    },
  },
});

export const {
  toggleSidebar,
  setSidebarSection,
  setGanttZoom,
  setSelectedFeature,
  setSelectedTeam,
  setSelectedSprint,
  setShowCreateFeatureModal,
  setShowCreateTeamModal,
  setShowPlanningParamsModal,
  setTheme,
} = uiSlice.actions;

export default uiSlice.reducer;
